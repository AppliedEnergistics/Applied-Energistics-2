
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

/**
 * Clears all data from the pattern access terminal before a full reset.
 */
public record ClearPatternAccessTerminalPacket() implements ClientboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearPatternAccessTerminalPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    ClearPatternAccessTerminalPacket::write,
                    ClearPatternAccessTerminalPacket::decode);

    public static final Type<ClearPatternAccessTerminalPacket> TYPE = CustomAppEngPayload
            .createType("clear_pattern_access_terminal");

    @Override
    public Type<ClearPatternAccessTerminalPacket> type() {
        return TYPE;
    }

    public static ClearPatternAccessTerminalPacket decode(RegistryFriendlyByteBuf data) {
        return new ClearPatternAccessTerminalPacket();
    }

    public void write(RegistryFriendlyByteBuf data) {
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        if (Minecraft.getInstance().screen instanceof PatternAccessTermScreen<?> patternAccessTerminal) {
            patternAccessTerminal.clear();
        }
    }
}
