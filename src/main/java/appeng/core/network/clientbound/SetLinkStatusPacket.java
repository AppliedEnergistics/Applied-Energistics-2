package appeng.core.network.clientbound;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import appeng.api.storage.ILinkStatus;
import appeng.api.storage.LinkStatus;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.guisync.LinkStatusAwareMenu;

public record SetLinkStatusPacket(ILinkStatus linkStatus) implements ClientboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, SetLinkStatusPacket> STREAM_CODEC = StreamCodec.ofMember(
            SetLinkStatusPacket::write,
            SetLinkStatusPacket::decode);

    public static final Type<SetLinkStatusPacket> TYPE = CustomAppEngPayload.createType("set_link_status");

    @Override
    public Type<SetLinkStatusPacket> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(linkStatus.connected());
        ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC.encode(buffer,
                Optional.ofNullable(linkStatus.statusDescription()));
    }

    public static SetLinkStatusPacket decode(RegistryFriendlyByteBuf buffer) {
        return new SetLinkStatusPacket(new LinkStatus(
                buffer.readBoolean(),
                ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC.decode(buffer).orElse(null)));
    }
}
