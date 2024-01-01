
package appeng.core.network.clientbound;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import appeng.core.network.ClientboundPacket;

public record MockExplosionPacket(double x, double y, double z) implements ClientboundPacket {

    public static MockExplosionPacket decode(FriendlyByteBuf data) {
        var x = data.readDouble();
        var y = data.readDouble();
        var z = data.readDouble();
        return new MockExplosionPacket(x, y, z);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeDouble(x);
        data.writeDouble(y);
        data.writeDouble(z);
    }

    @Override
    public void handleOnClient(Player player) {
        final Level level = player.getCommandSenderWorld();
        level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
    }
}
