package appeng.integration.modules.igtooltip.part;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;
import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;

public class AnnihilationPlaneDataProvider implements InGameTooltipProvider<AnnihilationPlanePart> {
    @Override
    public void buildTooltip(AnnihilationPlanePart plane, InGameTooltipContext context, InGameTooltipBuilder tooltip) {
        var enchantments = plane.getEnchantments();
        if (enchantments != null) {
            tooltip.addLine(InGameTooltip.EnchantedWith.text());
            for (var enchantment : enchantments.keySet()) {
                tooltip.addLine(enchantment.getFullname(enchantments.get(enchantment)));
            }
        }
    }
}
