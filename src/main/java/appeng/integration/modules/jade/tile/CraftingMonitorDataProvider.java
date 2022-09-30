package appeng.integration.modules.jade.tile;

import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import appeng.api.integrations.waila.AEJadeIds;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.core.localization.InGameTooltip;
import appeng.integration.modules.jade.BaseDataProvider;

/**
 * Shows the name of the item being crafted.
 */
public final class CraftingMonitorDataProvider extends BaseDataProvider {

    @Override
    public ResourceLocation getUid() {
        return AEJadeIds.CRAFTING_MONITOR_PROVIDER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof CraftingMonitorBlockEntity monitor) {
            var displayStack = monitor.getJobProgress();

            if (displayStack != null) {
                tooltip.add(InGameTooltip.Crafting.text(displayStack.what().getDisplayName()));
            }
        }
    }
}
