package appeng.integration.modules.wthit;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import mcp.mobius.waila.api.ITooltip;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;

public class WthitTooltipBuilder implements InGameTooltipBuilder {
    private final ITooltip tooltip;

    public WthitTooltipBuilder(ITooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void addLine(Component line) {
        tooltip.addLine(line);
    }

    @Override
    public void addLine(Component line, ResourceLocation id) {
        tooltip.addLine(line);
    }
}
