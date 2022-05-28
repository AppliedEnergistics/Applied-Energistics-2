package appeng.integration.modules.waila.part;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import appeng.api.parts.IPart;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;
import appeng.parts.automation.IdentityAnnihilationPlanePart;

public class AnnihilationPlaneDataProvider implements IPartDataProvider {

    @Override
    public void appendBody(IPart part, CompoundTag partTag, List<Component> tooltip) {
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
