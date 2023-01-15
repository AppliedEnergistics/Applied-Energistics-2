package appeng.integration.modules.theoneprobe;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import mcjty.theoneprobe.api.IProbeInfo;

import appeng.api.integrations.igtooltip.TooltipBuilder;

public class TopTooltipBuilder implements TooltipBuilder {
    private final IProbeInfo probeInfo;

    public TopTooltipBuilder(IProbeInfo probeInfo) {
        this.probeInfo = probeInfo;
    }

    @Override
    public void addLine(Component line) {
        probeInfo.mcText(line);
    }

    @Override
    public void addLine(Component line, ResourceLocation id) {
        probeInfo.mcText(line);
    }
}
