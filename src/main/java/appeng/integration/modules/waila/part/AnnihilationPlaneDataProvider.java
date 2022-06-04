package appeng.integration.modules.waila.part;

import net.minecraft.nbt.CompoundTag;

import mcp.mobius.waila.api.ITooltip;

import appeng.api.parts.IPart;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.IdentityAnnihilationPlanePart;

public class AnnihilationPlaneDataProvider implements IPartDataProvider {

    @Override
    public void appendBodyTooltip(IPart part, CompoundTag partTag, ITooltip tooltip) {
        if (part instanceof IdentityAnnihilationPlanePart) {
            tooltip.add(InGameTooltip.IdentityDeprecated.text());
        } else if (part instanceof AnnihilationPlanePart plane) {
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
