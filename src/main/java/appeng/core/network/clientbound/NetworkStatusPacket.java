
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.me.networktool.NetworkStatus;

public record NetworkStatusPacket(NetworkStatus status) implements ClientboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, NetworkStatusPacket> STREAM_CODEC = StreamCodec.ofMember(
            NetworkStatusPacket::write,
            NetworkStatusPacket::decode);

    public static final Type<NetworkStatusPacket> TYPE = CustomAppEngPayload.createType("network_status");

    @Override
    public Type<NetworkStatusPacket> type() {
        return TYPE;
    }

    public static NetworkStatusPacket decode(RegistryFriendlyByteBuf data) {
        var status = NetworkStatus.read(data);
        return new NetworkStatusPacket(status);
    }

    public void write(RegistryFriendlyByteBuf data) {
        status.write(data);
    }

}
