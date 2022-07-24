package appeng.integration.modules.igtooltip;

import javax.annotation.Nullable;

import net.minecraft.network.chat.MutableComponent;

import appeng.api.networking.IGridNode;
import appeng.core.localization.InGameTooltip;

public enum GridNodeState {
    OFFLINE(InGameTooltip.DeviceOffline),
    NETWORK_BOOTING(InGameTooltip.NetworkBooting),
    MISSING_CHANNEL(InGameTooltip.DeviceMissingChannel),
    ONLINE(InGameTooltip.DeviceOnline);

    private final InGameTooltip text;

    GridNodeState(InGameTooltip text) {
        this.text = text;
    }

    public MutableComponent textComponent() {
        return text.text();
    }

    public static GridNodeState fromNode(@Nullable IGridNode gridNode) {
        var state = GridNodeState.OFFLINE;
        if (gridNode != null && gridNode.isPowered()) {
            if (!gridNode.hasGridBooted()) {
                state = GridNodeState.NETWORK_BOOTING;
            } else if (!gridNode.meetsChannelRequirements()) {
                state = GridNodeState.MISSING_CHANNEL;
            } else {
                state = GridNodeState.ONLINE;
            }
        }
        return state;
    }

}
