
package appeng.core.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.particles.EnergyParticleData;

/**
 * Plays a transition particle effect into the supplied direction. Used primarily by annihilation planes.
 */
public record ItemTransitionEffectPacket(double x,
        double y,
        double z,
        Direction d) implements ClientboundPacket {

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemTransitionEffectPacket> STREAM_CODEC = StreamCodec
            .ofMember(
                    ItemTransitionEffectPacket::write,
                    ItemTransitionEffectPacket::decode);

    public static final Type<ItemTransitionEffectPacket> TYPE = CustomAppEngPayload
            .createType("item_transition_effect");

    @Override
    public Type<ItemTransitionEffectPacket> type() {
        return TYPE;
    }

    public static ItemTransitionEffectPacket decode(RegistryFriendlyByteBuf stream) {
        var x = stream.readFloat();
        var y = stream.readFloat();
        var z = stream.readFloat();
        var d = stream.readEnum(Direction.class);
        return new ItemTransitionEffectPacket(x, y, z, d);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);
        data.writeEnum(d);
    }
}
