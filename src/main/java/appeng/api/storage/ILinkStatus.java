package appeng.api.storage;

import appeng.api.networking.IManagedGridNode;
import appeng.core.localization.GuiText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Describes the current connection status of a {@link ITerminalHost} to the grid.
 */
public interface ILinkStatus {
    /**
     * @return A constant link status indicating the host is connected.
     */
    static ILinkStatus ofConnected() {
        return LinkStatus.CONNECTED;
    }

    /**
     * @return A link status indicating the host is disconnected from the grid without a particular reason.
     */
    static ILinkStatus ofDisconnected() {
        return new LinkStatus(false, null);
    }

    /**
     * @return A link status indicating the host is disconnected from the grid with the given reason.
     */
    static ILinkStatus ofDisconnected(@Nullable Component statusDescription) {
        return new LinkStatus(false, statusDescription);
    }

    /**
     * Calculates a link status from a given managed grid node.
     */
    static ILinkStatus ofManagedNode(IManagedGridNode node) {
        if (node.isOnline()) {
            return ofConnected();
        } else if (!node.isPowered()) {
            return ofDisconnected(GuiText.NoPower.text().withStyle(ChatFormatting.DARK_RED));
        } else if (node.getNode() != null && !node.getNode().meetsChannelRequirements()) {
            return ofDisconnected(GuiText.NoChannel.text().withStyle(ChatFormatting.DARK_RED));
        } else {
            return ofDisconnected(null);
        }
    }

    /**
     * @return True if the host is currently connected to a grid.
     */
    boolean connected();

    /**
     * Gives a reason for why the {@linkplain  ITerminalHost host} is disconnected. Can always be null,
     * but is genereally only not-null if {@link #connected()} returns false.
     */
    @Nullable
    Component statusDescription();
}
