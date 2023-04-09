package appeng.blockentity.misc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.BaseActionSource;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class CondenserInventoryTest {
    CondenserBlockEntity be = new CondenserBlockEntity(AEBlockEntities.CONDENSER, BlockPos.ZERO,
            AEBlocks.CONDENSER.block().defaultBlockState());
    IStorageMonitorableAccessor storageAccessor = be.getMEHandler();

    @Test
    void testSingularityProductionAndPriority() {
        var inv = storageAccessor.getInventory(new BaseActionSource());
        assertThat(inv).isNotNull();
        assertThat(inv).isInstanceOf(CondenserInventory.class);
        assertThat(inv.getAvailableStacks()).isEmpty();

        be.getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY);
        be.getInternalInventory().setItemDirect(2, AEItems.CELL_COMPONENT_64K.stack());
        be.addPower(99999999999.0);

        var singularity = AEItemKey.of(AEItems.SINGULARITY.asItem());
        assertThat(inv.getAvailableStacks())
                .extracting(Map.Entry::getKey)
                .containsOnly(singularity);
        assertThat(inv.getAvailableStacks().get(singularity)).isEqualTo(2);
        assertThat(inv.isPreferredStorageFor(singularity, new BaseActionSource())).isFalse();
    }

    /**
     * Test if inserting items or fluids results in the right amount of energy This test WILL fail if the amount per
     * operation is changed
     */
    @Test
    void testEnergyPerUnit() {
        var inv = storageAccessor.getInventory(new BaseActionSource());
        assertThat(inv).isNotNull();
        assertThat(inv).isInstanceOf(CondenserInventory.class);
        assertThat(inv.getAvailableStacks()).isEmpty();
        be.getInternalInventory().setItemDirect(2, AEItems.CELL_COMPONENT_64K.stack());
        be.getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY);

        // test Fluid insert via ME (e.g. Storage bus)
        ((IStorageMonitorableAccessor) be.getMEHandler()).getInventory(null).insert(
                AEFluidKey.of(Fluids.WATER.getSource()), AEFluidKey.AMOUNT_BUCKET, Actionable.MODULATE,
                new BaseActionSource());
        assertThat(be.getStoredPower()).isEqualTo(8);

        ((IStorageMonitorableAccessor) be.getMEHandler()).getInventory(null)
                .insert(AEItemKey.of(AEItems.MATTER_BALL.stack()), 1, Actionable.MODULATE, new BaseActionSource());
        assertThat(be.getStoredPower()).isEqualTo(8 + 1);

        // test Fluid insert via transfer API
        try (Transaction transaction = Transaction.openOuter()) {
            be.getFluidHandler().insert(FluidVariant.of(Fluids.WATER.getSource()), AEFluidKey.AMOUNT_BUCKET,
                    transaction);
            transaction.commit();
        }
        assertThat(be.getStoredPower()).isEqualTo(8 + 1 + 8);

        // test item insert via transfer API
        try (Transaction transaction = Transaction.openOuter()) {
            be.getExternalInv().toStorage().insert(ItemVariant.of(AEItems.MATTER_BALL.stack()), 1, transaction);
            transaction.commit();
        }
        assertThat(be.getStoredPower()).isEqualTo(8 + 1 + 8 + 1);
    }
}
