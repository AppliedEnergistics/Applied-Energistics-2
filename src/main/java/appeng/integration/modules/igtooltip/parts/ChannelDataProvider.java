package appeng.integration.modules.igtooltip.parts;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.networking.pathing.ControllerState;
import appeng.core.localization.InGameTooltip;
import appeng.me.service.PathingService;
import appeng.parts.networking.IUsedChannelProvider;

/**
 * Shows the used and maximum channel count for a part that implements {@link IUsedChannelProvider}.
 */
public final class ChannelDataProvider
        implements BodyProvider<IUsedChannelProvider>, ServerDataProvider<IUsedChannelProvider> {
    private static final String TAG_MAX_CHANNELS = "maxChannels";
    private static final String TAG_USED_CHANNELS = "usedChannels";
    private static final String TAG_ERROR = "channelError";

    @Override
    public void buildTooltip(IUsedChannelProvider object, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();
        if (serverData.contains(TAG_ERROR, Tag.TAG_STRING)) {
            var error = ChannelError.valueOf(serverData.getString(TAG_ERROR));
            tooltip.addLine(error.text.text().withStyle(ChatFormatting.RED));
            return;
        }

        if (serverData.contains(TAG_MAX_CHANNELS, Tag.TAG_INT)) {
            var usedChannels = serverData.getInt(TAG_USED_CHANNELS);
            var maxChannels = serverData.getInt(TAG_MAX_CHANNELS);
            // Even in the maxChannels=0 case, we'll show as infinite
            if (maxChannels <= 0) {
                tooltip.addLine(InGameTooltip.Channels.text(usedChannels));
            } else {
                tooltip.addLine(InGameTooltip.ChannelsOf.text(usedChannels, maxChannels));
            }
        }
    }

    @Override
    public void provideServerData(ServerPlayer player, IUsedChannelProvider object, CompoundTag serverData) {
        var gridNode = object.getGridNode();
        if (gridNode != null) {
            var pathingService = (PathingService) gridNode.getGrid().getPathingService();
            if (pathingService.getControllerState() == ControllerState.NO_CONTROLLER) {
                var adHocError = pathingService.getAdHocNetworkError();
                if (adHocError != null) {
                    serverData.putString(TAG_ERROR, switch (adHocError) {
                        case NESTED_P2P_TUNNEL -> ChannelError.AD_HOC_NESTED_P2P_TUNNEL.name();
                        case TOO_MANY_CHANNELS -> ChannelError.AD_HOC_TOO_MANY_CHANNELS.name();
                    });
                    return;
                }
            } else if (pathingService.getControllerState() == ControllerState.CONTROLLER_CONFLICT) {
                serverData.putString(TAG_ERROR, ChannelError.CONTROLLER_CONFLICT.name());
            }
        }

        serverData.putInt(TAG_USED_CHANNELS, object.getUsedChannelsInfo());
        serverData.putInt(TAG_MAX_CHANNELS, object.getMaxChannelsInfo());
    }

    enum ChannelError {
        AD_HOC_NESTED_P2P_TUNNEL(InGameTooltip.ErrorNestedP2PTunnel),
        AD_HOC_TOO_MANY_CHANNELS(InGameTooltip.ErrorTooManyChannels),
        CONTROLLER_CONFLICT(InGameTooltip.ErrorControllerConflict);

        final InGameTooltip text;

        ChannelError(InGameTooltip text) {
            this.text = text;
        }
    }
}
