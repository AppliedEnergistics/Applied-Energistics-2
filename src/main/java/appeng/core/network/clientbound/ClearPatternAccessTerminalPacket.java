
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.network.ClientboundPacket;

/**
 * Clears all data from the pattern access terminal before a full reset.
 */
public record ClearPatternAccessTerminalPacket() implements ClientboundPacket {
    public static ClearPatternAccessTerminalPacket decode(FriendlyByteBuf data) {
        return new ClearPatternAccessTerminalPacket();
    }

    @Override
    public void write(FriendlyByteBuf data) {
    }

    @Override
    public void handleOnClient(Player player) {
        if (Minecraft.getInstance().screen instanceof PatternAccessTermScreen<?>patternAccessTerminal) {
            patternAccessTerminal.clear();
        }
    }
}
