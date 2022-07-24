package appeng.integration.modules.jade;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ITooltip;

public class JadeTooltipBuilder implements InGameTooltipBuilder {
    private final ITooltip tooltip;

    public JadeTooltipBuilder(ITooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void addLine(Component line) {
        tooltip.add(line);
    }

    @Override
    public void addLine(Component line, ResourceLocation id) {
        tooltip.add(line, id);
    }
}
