
package appeng.core.network.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;

public record CompassResponsePacket(long attunement,
        int cx,
        int cz,
        int cdy,
        CompassResult cr) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, CompassResponsePacket> STREAM_CODEC = StreamCodec.ofMember(
            CompassResponsePacket::write,
            CompassResponsePacket::decode);

    public static final Type<CompassResponsePacket> TYPE = CustomAppEngPayload.createType("compass_response");

    @Override
    public Type<CompassResponsePacket> type() {
        return TYPE;
    }

    public static CompassResponsePacket decode(RegistryFriendlyByteBuf stream) {
        var attunement = stream.readLong();
        var cx = stream.readInt();
        var cz = stream.readInt();
        var cdy = stream.readInt();

        var cr = new CompassResult(stream.readBoolean(), stream.readBoolean(), stream.readDouble());
        return new CompassResponsePacket(attunement, cx, cz, cdy, cr);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeLong(attunement);
        data.writeInt(cx);
        data.writeInt(cz);
        data.writeInt(cdy);

        data.writeBoolean(cr.isValidResult());
        data.writeBoolean(cr.isSpin());
        data.writeDouble(cr.getRad());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        CompassManager.INSTANCE.postResult(this.attunement, this.cx << 4, this.cdy << 5, this.cz << 4, this.cr);
    }
}
