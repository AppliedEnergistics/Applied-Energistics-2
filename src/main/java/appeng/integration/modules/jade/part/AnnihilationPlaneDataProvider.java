package appeng.integration.modules.jade.part;

import net.minecraft.nbt.CompoundTag;

import snownee.jade.api.ITooltip;

import appeng.api.parts.IPart;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;

public class AnnihilationPlaneDataProvider implements IPartDataProvider {
    @Override
    public void appendBodyTooltip(IPart part, CompoundTag partTag, ITooltip tooltip) {
        if (part instanceof AnnihilationPlanePart plane) {
            var enchantments = plane.getEnchantments();
            if (enchantments != null) {
                tooltip.add(InGameTooltip.EnchantedWith.text());
                for (var enchantment : enchantments.keySet()) {
                    tooltip.add(enchantment.getFullname(enchantments.get(enchantment)));
                }
            }
        }
    }
}
