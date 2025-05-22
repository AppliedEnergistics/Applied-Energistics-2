package appeng.menu.guisync;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Implement on classes to signal they can be synchronized to the client using {@link GuiSync}. For this to work fully,
 * the class also needs to have a public constructor that takes a {@link FriendlyByteBuf} argument.
 */
public interface PacketWritable {
    void writeToPacket(FriendlyByteBuf data);
}
