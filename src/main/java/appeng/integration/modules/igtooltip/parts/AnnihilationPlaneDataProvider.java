package appeng.integration.modules.igtooltip.parts;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;

public class AnnihilationPlaneDataProvider implements BodyProvider<AnnihilationPlanePart> {
    @Override
    public void buildTooltip(AnnihilationPlanePart plane, TooltipContext context, TooltipBuilder tooltip) {
        var enchantments = plane.getEnchantments();
        if (enchantments != null) {
            tooltip.addLine(InGameTooltip.EnchantedWith.text());
            for (var enchantment : enchantments.keySet()) {
                tooltip.addLine(enchantment.getFullname(enchantments.get(enchantment)));
            }
        }
    }
}
