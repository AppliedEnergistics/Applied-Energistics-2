package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.sync.BasePacket;

/**
 * Clears all data from the pattern access terminal before a full reset.
 */
public class ClearPatternAccessTerminalPacket extends BasePacket {

    public ClearPatternAccessTerminalPacket(FriendlyByteBuf stream) {
    }

    // api
    public ClearPatternAccessTerminalPacket() {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer(16));
        data.writeInt(this.getPacketID());
        this.configureWrite(data);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(Player player) {
        if (Minecraft.getInstance().screen instanceof PatternAccessTermScreen<?>patternAccessTerminal) {
            patternAccessTerminal.clear();
        }
    }
}
