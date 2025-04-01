
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.particles.ParticleTypes;

public record MatterCannonPacket(double x,
        double y,
        double z,
        double dx,
        double dy,
        double dz,
        byte len) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, MatterCannonPacket> STREAM_CODEC = StreamCodec.ofMember(
            MatterCannonPacket::write,
            MatterCannonPacket::decode);

    public static final Type<MatterCannonPacket> TYPE = CustomAppEngPayload.createType("matter_cannon");

    @Override
    public Type<MatterCannonPacket> type() {
        return TYPE;
    }

    public MatterCannonPacket(double x, double y, double z, double dx, double dy, double dz, byte len) {
        var dl = dx * dx + dy * dy + dz * dz;
        var dlz = (float) Math.sqrt(dl);

        this.x = x;
        this.y = y;
        this.z = z;
        this.dx = dx / dlz;
        this.dy = dy / dlz;
        this.dz = dz / dlz;
        this.len = len;
    }

    public static MatterCannonPacket decode(RegistryFriendlyByteBuf stream) {
        var x = stream.readFloat();
        var y = stream.readFloat();
        var z = stream.readFloat();
        var dx = stream.readFloat();
        var dy = stream.readFloat();
        var dz = stream.readFloat();
        var len = stream.readByte();
        return new MatterCannonPacket(x, y, z, dx, dy, dz, len);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);
        data.writeFloat((float) this.dx);
        data.writeFloat((float) this.dy);
        data.writeFloat((float) this.dz);
        data.writeByte(len);
    }
}
