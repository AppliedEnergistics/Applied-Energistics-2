package appeng.integration.modules.igtooltip.blocks;

import net.minecraft.ChatFormatting;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity;
import appeng.core.localization.InGameTooltip;

public final class CrystalResonanceGeneratorProvider implements BodyProvider<CrystalResonanceGeneratorBlockEntity> {
    @Override
    public void buildTooltip(CrystalResonanceGeneratorBlockEntity generator, TooltipContext context,
            TooltipBuilder tooltip) {
        if (generator.isSuppressed()) {
            tooltip.addLine(InGameTooltip.Suppressed.text().withStyle(ChatFormatting.RED));
        }
    }
}
