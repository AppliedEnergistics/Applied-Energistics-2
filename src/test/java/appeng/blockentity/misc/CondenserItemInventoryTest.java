package appeng.blockentity.misc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.definitions.AEBlockEntities;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.BaseActionSource;
import appeng.util.BootstrapMinecraft;

@BootstrapMinecraft
class CondenserItemInventoryTest {
    CondenserBlockEntity be = new CondenserBlockEntity(AEBlockEntities.CONDENSER, BlockPos.ZERO,
            AEBlocks.CONDENSER.block().defaultBlockState());
    IStorageMonitorableAccessor storageAccessor = be.getMEHandler();

    @Test
    void testSingularityProductionAndPriority() {
        var inv = storageAccessor.getInventory(new BaseActionSource())
                .getInventory(StorageChannels.items());
        assertThat(inv).isNotNull();
        assertThat(inv).isInstanceOf(CondenserItemInventory.class);
        assertThat(inv.getAvailableStacks()).isEmpty();

        be.getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY);
        be.getInternalInventory().setItemDirect(2, AEItems.ITEM_64K_CELL_COMPONENT.stack());
        be.addPower(99999999999.0);

        IAEItemStack singularity = IAEItemStack.of(AEItems.SINGULARITY.stack(2));
        assertThat(inv.getAvailableStacks()).containsOnly(singularity);
        assertThat(inv.isPreferredStorageFor(singularity, new BaseActionSource())).isFalse();
    }
}
