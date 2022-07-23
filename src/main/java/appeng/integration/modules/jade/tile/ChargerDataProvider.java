package appeng.integration.modules.jade.tile;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.integrations.waila.AEJadeIds;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.core.localization.InGameTooltip;
import appeng.integration.modules.jade.BaseDataProvider;
import appeng.util.Platform;

/**
 * Shows the tooltip of the item being charged, which usually includes a charge meter.
 */
public final class ChargerDataProvider extends BaseDataProvider {
    @Override
    public ResourceLocation getUid() {
        return AEJadeIds.CHARGER_PROVIDER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var blockEntity = accessor.getBlockEntity();
        if (blockEntity instanceof ChargerBlockEntity charger) {
            var chargerInventory = charger.getInternalInventory();
            var chargingItem = chargerInventory.getStackInSlot(0);

            if (!chargingItem.isEmpty()) {
                tooltip.add(InGameTooltip.Contains.text(
                        chargingItem.getHoverName().copy().withStyle(ChatFormatting.WHITE)));

                if (chargingItem.getItem() instanceof IAEItemPowerStorage powerStorage
                        && Platform.isChargeable(chargingItem)) {
                    var fillRate = Mth.floor(powerStorage.getAECurrentPower(chargingItem) * 100 /
                            powerStorage.getAEMaxPower(chargingItem));
                    tooltip.add(InGameTooltip.Charged.text(
                            fillRate));
                }
            }
        }

    }
}
