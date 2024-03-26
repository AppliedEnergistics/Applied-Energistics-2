
package appeng.core.network.clientbound;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;

public record MockExplosionPacket(double x, double y, double z) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, MockExplosionPacket> STREAM_CODEC = StreamCodec.ofMember(
            MockExplosionPacket::write,
            MockExplosionPacket::decode);

    public static final Type<MockExplosionPacket> TYPE = CustomAppEngPayload.createType("mock_explosion");

    @Override
    public Type<MockExplosionPacket> type() {
        return TYPE;
    }

    public static MockExplosionPacket decode(RegistryFriendlyByteBuf data) {
        var x = data.readDouble();
        var y = data.readDouble();
        var z = data.readDouble();
        return new MockExplosionPacket(x, y, z);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeDouble(x);
        data.writeDouble(y);
        data.writeDouble(z);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        final Level level = player.getCommandSenderWorld();
        level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
    }
}
