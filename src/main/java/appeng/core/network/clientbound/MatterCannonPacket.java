
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.network.ClientboundPacket;

public record MatterCannonPacket(double x,
        double y,
        double z,
        double dx,
        double dy,
        double dz,
        byte len) implements ClientboundPacket {

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

    public static MatterCannonPacket decode(FriendlyByteBuf stream) {
        var x = stream.readFloat();
        var y = stream.readFloat();
        var z = stream.readFloat();
        var dx = stream.readFloat();
        var dy = stream.readFloat();
        var dz = stream.readFloat();
        var len = stream.readByte();
        return new MatterCannonPacket(x, y, z, dx, dy, dz, len);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);
        data.writeFloat((float) this.dx);
        data.writeFloat((float) this.dy);
        data.writeFloat((float) this.dz);
        data.writeByte(len);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        try {
            for (int a = 1; a < this.len; a++) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.MATTER_CANNON, this.x + this.dx * a,
                        this.y + this.dy * a, this.z + this.dz * a, 0, 0, 0);
            }
        } catch (Exception ignored) {
        }
    }
}
