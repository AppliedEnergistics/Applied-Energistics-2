package appeng.me.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;

import org.assertj.core.data.Offset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.me.AbstractGridNodeTest;
import appeng.me.GridConnection;
import appeng.me.InfinitePowerStorage;
import appeng.me.ManagedGridNode;
import appeng.me.energy.StoredEnergyAmount;

class EnergyServiceTest extends AbstractGridNodeTest {
    public static final Offset<Double> TOLERANCE = Offset.offset(0.1);

    @Test
    void testEnergyGridStorageScalesWithNodes() {
        // +1 node
        var mgn1 = createAndInitNode();
        var energyService = mgn1.getGrid().getEnergyService();
        assertThat(energyService.getMaxStoredPower()).isCloseTo(25, TOLERANCE);

        // +1 node
        var mgn2 = createAndInitNode();
        GridConnection.create(mgn1.getNode(), mgn2.getNode(), null);
        assertThat(energyService.getMaxStoredPower()).isCloseTo(50, TOLERANCE);
        assertThat(energyService.injectPower(10000, Actionable.MODULATE)).isCloseTo(9950, TOLERANCE);

        // -1 node
        mgn1.destroy();
        assertThat(energyService.getMaxStoredPower()).isCloseTo(25, TOLERANCE);

        // Ensure the excess power was dropped
        ((EnergyService) energyService).refreshPower();
        assertThat(energyService.getStoredPower()).isCloseTo(25, TOLERANCE);
    }

    @Test
    void testLocalStorageIsDistributedAcrossGridNodes() {
        var mgn1 = createAndInitNode();
        var mgn2 = createAndInitNode();
        GridConnection.create(mgn1.getNode(), mgn2.getNode(), null);

        var energyService = (EnergyService) mgn1.getGrid().getEnergyService();

        energyService.injectPower(40, Actionable.MODULATE);

        // Save Node 2 and make it leave the grid
        var savedNodeData = toTag(mgn2::serialize);
        mgn2.destroy();

        // Half the storage should have left
        energyService.refreshPower();
        assertThat(energyService.getStoredPower()).isCloseTo(20, TOLERANCE);

        // Re-Join the node using it's stored state,
        // The stored energy should have returned
        mgn2 = createAndInitNode(savedNodeData);
        GridConnection.create(mgn1.getNode(), mgn2.getNode(), null);
        energyService.refreshPower();
        assertThat(energyService.getStoredPower()).isCloseTo(40, TOLERANCE);
    }

    @Test
    void testStoredEnergyIsRemovedOnNbtReload() {
        var mgn1 = createAndInitNode();
        var mgn2 = createAndInitNode();
        GridConnection.create(mgn1.getNode(), mgn2.getNode(), null);

        var energyService = (EnergyService) mgn1.getGrid().getEnergyService();
        energyService.injectPower(40, Actionable.MODULATE);

        // Save Node 2 and reload it immediately using the same data
        var savedNodeData = toTag(mgn2::serialize);

        fromTag(savedNodeData, mgn2::deserialize);
        // We do need to reconnect the node manually here, since
        // we're not using in-world connections
        GridConnection.create(mgn1.getNode(), mgn2.getNode(), null);

        // Stored energy should not have increased
        energyService.refreshPower();
        assertThat(energyService.getStoredPower()).isCloseTo(40, TOLERANCE);
    }

    /**
     * Extracting a minuscule amount of power must never cause a power provider to be dropped from the grid, even if the
     * provider is unable to hand out such a small amount (see #9074).
     */
    @Test
    void testExtractingMinusculeAmountDoesNotDropProvider() {
        var storage = new MinAmountPowerStorage(100);
        var node = makeNode();
        node.addService(IAEPowerStorage.class, storage);
        var energyService = (EnergyService) node.getInternalGrid().getEnergyService();
        energyService.refreshPower();
        assertThat(energyService.getStoredPower()).isCloseTo(100, TOLERANCE);

        var minuscule = StoredEnergyAmount.MIN_AMOUNT / 10;
        energyService.extractAEPower(minuscule, Actionable.MODULATE, PowerMultiplier.CONFIG);

        // The provider should still be part of the grid, and be usable
        energyService.refreshPower();
        assertThat(energyService.getStoredPower()).isCloseTo(100, TOLERANCE);
        assertThat(energyService.extractAEPower(10, Actionable.MODULATE, PowerMultiplier.ONE))
                .isCloseTo(10, TOLERANCE);
        energyService.refreshPower();
        assertThat(energyService.getStoredPower()).isCloseTo(90, TOLERANCE);
    }

    /**
     * A power storage that behaves like the energy cell: Extracting less than {@link StoredEnergyAmount#MIN_AMOUNT}
     * yields nothing.
     */
    private static class MinAmountPowerStorage extends InfinitePowerStorage {
        private final StoredEnergyAmount stored;

        MinAmountPowerStorage(double amount) {
            stored = new StoredEnergyAmount(amount, amount, type -> {
            });
        }

        @Override
        public double getAECurrentPower() {
            return stored.getAmount();
        }

        @Override
        public double getAEMaxPower() {
            return stored.getMaximum();
        }

        @Override
        public AccessRestriction getPowerFlow() {
            return AccessRestriction.READ_WRITE;
        }

        @Override
        public double injectAEPower(double amt, Actionable mode) {
            return amt - stored.insert(amt, mode == Actionable.MODULATE);
        }

        @Override
        public double extractAEPower(double amt, Actionable mode, PowerMultiplier pm) {
            return pm.divide(stored.extract(pm.multiply(amt), mode == Actionable.MODULATE));
        }
    }

    @NotNull
    private ManagedGridNode createAndInitNode() {
        return createAndInitNode(null);
    }

    @NotNull
    private ManagedGridNode createAndInitNode(@Nullable CompoundTag tag) {
        var mgn = new ManagedGridNode(owner, listener);
        if (tag != null) {
            fromTag(tag, mgn::deserialize);
        }
        mgn.create(level, null);
        return mgn;
    }

    private CompoundTag toTag(Consumer<ValueOutput> serializer) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, RegistryAccess.EMPTY);
        serializer.accept(output);
        return output.buildResult();
    }

    private void fromTag(CompoundTag tag, Consumer<ValueInput> deserializer) {
        var input = TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, tag);
        deserializer.accept(input);
    }

}
