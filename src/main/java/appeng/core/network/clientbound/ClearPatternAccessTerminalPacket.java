
package appeng.core.network.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

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
}
