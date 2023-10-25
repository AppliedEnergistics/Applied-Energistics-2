package appeng.me.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

import appeng.api.config.Actionable;
import appeng.me.AbstractGridNodeTest;
import appeng.me.GridConnection;
import appeng.me.ManagedGridNode;

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
        var savedNodeData = new CompoundTag();
        mgn2.saveToNBT(savedNodeData);
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
        var savedNodeData = new CompoundTag();
        mgn2.saveToNBT(savedNodeData);

        mgn2.loadFromNBT(savedNodeData);
        // We do need to reconnect the node manually here, since
        // we're not using in-world connections
        GridConnection.create(mgn1.getNode(), mgn2.getNode(), null);

        // Stored energy should not have increased
        energyService.refreshPower();
        assertThat(energyService.getStoredPower()).isCloseTo(40, TOLERANCE);
    }

    @NotNull
    private ManagedGridNode createAndInitNode() {
        return createAndInitNode(null);
    }

    @NotNull
    private ManagedGridNode createAndInitNode(@Nullable CompoundTag tag) {
        var mgn = new ManagedGridNode(owner, listener);
        if (tag != null) {
            mgn.loadFromNBT(tag);
        }
        mgn.create(level, null);
        return mgn;
    }

}
