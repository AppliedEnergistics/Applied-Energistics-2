package appeng.integration.modules.igtooltip.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;

import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.core.localization.InGameTooltip;
import appeng.util.Platform;

/**
 * Shows the tooltip of the item being charged, which usually includes a charge meter.
 */
public final class ChargerDataProvider implements BodyProvider<ChargerBlockEntity> {
    @Override
    public void buildTooltip(ChargerBlockEntity charger, TooltipContext context, TooltipBuilder tooltip) {
        var chargerInventory = charger.getInternalInventory();
        var chargingItem = chargerInventory.getStackInSlot(0);

        if (!chargingItem.isEmpty()) {
            tooltip.addLine(InGameTooltip.Contains.text(
                    chargingItem.getHoverName().copy().withStyle(ChatFormatting.WHITE)));

            if (chargingItem.getItem() instanceof IAEItemPowerStorage powerStorage
                    && Platform.isChargeable(chargingItem)) {
                var fillRate = Mth.floor(powerStorage.getAECurrentPower(chargingItem) * 100 /
                        powerStorage.getAEMaxPower(chargingItem));
                tooltip.addLine(InGameTooltip.Charged.text(fillRate));
            }
        }
    }
}
