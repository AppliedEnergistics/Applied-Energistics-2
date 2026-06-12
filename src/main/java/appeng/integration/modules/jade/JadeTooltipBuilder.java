package appeng.integration.modules.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.ITooltip;

import appeng.api.integrations.igtooltip.TooltipBuilder;

class JadeTooltipBuilder implements TooltipBuilder {
    private final ITooltip tooltip;

    public JadeTooltipBuilder(ITooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void addLine(Component line) {
        tooltip.add(line);
    }

    @Override
    public void addLine(Component line, Identifier id) {
        tooltip.add(line, id);
    }
}
