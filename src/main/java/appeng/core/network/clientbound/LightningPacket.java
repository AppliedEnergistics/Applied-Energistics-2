
package appeng.core.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.network.ClientboundPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record LightningPacket(
        double x,
        double y,
        double z) implements ClientboundPacket {

    public static LightningPacket decode(FriendlyByteBuf stream) {
        var x = stream.readFloat();
        var y = stream.readFloat();
        var z = stream.readFloat();
        return new LightningPacket(x, y, z);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        try {
            if (AEConfig.instance().isEnableEffects()) {
                player.getCommandSenderWorld().addParticle(ParticleTypes.LIGHTNING, this.x, this.y, this.z, 0.0f, 0.0f,
                        0.0f);
            }
        } catch (Exception ignored) {
        }
    }
}
