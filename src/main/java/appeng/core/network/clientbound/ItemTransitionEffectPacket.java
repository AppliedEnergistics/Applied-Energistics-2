
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.client.render.effects.EnergyParticleData;
import appeng.core.AppEngClient;
import appeng.core.network.ClientboundPacket;

/**
 * Plays a transition particle effect into the supplied direction. Used primarily by annihilation planes.
 */
public record ItemTransitionEffectPacket(double x,
        double y,
        double z,
        Direction d) implements ClientboundPacket {

    public static ItemTransitionEffectPacket decode(FriendlyByteBuf stream) {
        var x = stream.readFloat();
        var y = stream.readFloat();
        var z = stream.readFloat();
        var d = stream.readEnum(Direction.class);
        return new ItemTransitionEffectPacket(x, y, z, d);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);
        data.writeEnum(d);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        EnergyParticleData data = new EnergyParticleData(true, this.d);
        for (int zz = 0; zz < 8; zz++) {
            if (AppEngClient.instance().shouldAddParticles(player.level().getRandom())) {
                // Distribute the spawn point around the item's position
                double x = this.x + player.level().getRandom().nextFloat() * 0.5 - 0.25;
                double y = this.y + player.level().getRandom().nextFloat() * 0.5 - 0.25;
                double z = this.z + player.level().getRandom().nextFloat() * 0.5 - 0.25;
                double speedX = 0.1f * this.d.getStepX();
                double speedY = 0.1f * this.d.getStepY();
                double speedZ = 0.1f * this.d.getStepZ();
                Minecraft.getInstance().particleEngine.createParticle(data, x, y, z, speedX, speedY, speedZ);
            }
        }
    }

}
