package appeng.blockentity.misc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.data.AEItemKey;
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
        var inv = storageAccessor.getInventory(new BaseActionSource())
                .getInventory();
        assertThat(inv).isNotNull();
        assertThat(inv).isInstanceOf(CondenserInventory.class);
        assertThat(inv.getAvailableStacks()).isEmpty();

        be.getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.SINGULARITY);
        be.getInternalInventory().setItemDirect(2, AEItems.ITEM_64K_CELL_COMPONENT.stack());
        be.addPower(99999999999.0);

        var singularity = AEItemKey.of(AEItems.SINGULARITY.asItem());
        assertThat(inv.getAvailableStacks())
                .extracting(Map.Entry::getKey)
                .containsOnly(singularity);
        assertThat(inv.getAvailableStacks().get(singularity)).isEqualTo(2);
        assertThat(inv.isPreferredStorageFor(singularity, new BaseActionSource())).isFalse();
    }
}
