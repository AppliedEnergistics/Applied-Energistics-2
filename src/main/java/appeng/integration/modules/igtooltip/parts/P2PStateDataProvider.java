package appeng.integration.modules.igtooltip.parts;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.Platform;

/**
 * Provides information about a P2P tunnel to WAILA.
 */
@SuppressWarnings("rawtypes")
public final class P2PStateDataProvider implements BodyProvider<P2PTunnelPart>, ServerDataProvider<P2PTunnelPart> {
    private static final byte STATE_UNLINKED = 0;
    private static final byte STATE_OUTPUT = 1;
    private static final byte STATE_INPUT = 2;
    public static final String TAG_P2P_STATE = "p2pState";
    public static final String TAG_P2P_OUTPUTS = "p2pOutputs";
    public static final String TAG_P2P_FREQUENCY = "p2pFrequency";

    @Override
    public void buildTooltip(P2PTunnelPart object, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();

        if (serverData.contains(TAG_P2P_STATE, Tag.TAG_BYTE)) {
            var state = serverData.getByte(TAG_P2P_STATE);
            var outputs = serverData.getInt(TAG_P2P_OUTPUTS);

            switch (state) {
                case STATE_UNLINKED -> tooltip.addLine(InGameTooltip.P2PUnlinked.text());
                case STATE_OUTPUT -> tooltip.addLine(InGameTooltip.P2POutput.text());
                case STATE_INPUT -> tooltip.addLine(getOutputText(outputs));
            }

            var freq = serverData.getShort(TAG_P2P_FREQUENCY);
            var freqTooltip = Platform.p2p().toHexString(freq);
            tooltip.addLine(new TranslatableComponent("gui.tooltips.ae2.P2PFrequency", freqTooltip));
        }
    }

    @Override
    public void provideServerData(ServerPlayer player, P2PTunnelPart part, CompoundTag serverData) {
        if (!part.isPowered()) {
            return;
        }

        // Frequency
        serverData.putShort(TAG_P2P_FREQUENCY, part.getFrequency());

        // The default state
        byte state = STATE_UNLINKED;
        if (!part.isOutput()) {
            var outputCount = part.getOutputs().size();
            if (outputCount > 0) {
                // Only set it to INPUT if we know there are any outputs
                state = STATE_INPUT;
                serverData.putInt(TAG_P2P_OUTPUTS, outputCount);
            }
        } else {
            if (part.getInput() != null) {
                state = STATE_OUTPUT;
            }
        }

        serverData.putByte(TAG_P2P_STATE, state);
    }

    private static Component getOutputText(int outputs) {
        if (outputs <= 1) {
            return InGameTooltip.P2PInputOneOutput.text();
        } else {
            return InGameTooltip.P2PInputManyOutputs.text(outputs);
        }
    }

}
