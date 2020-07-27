package appeng.client.render.effects;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

/**
 * Contains the target point of the lightning arc (the source point is infered
 * from the particle starting position).
 */
public class LightningArcParticleData implements ParticleEffect {

    public final Vec3d target;

    public LightningArcParticleData(Vec3d target) {
        this.target = target;
    }

    public static final Factory<LightningArcParticleData> DESERIALIZER = new Factory<LightningArcParticleData>() {
        @Override
        public LightningArcParticleData read(ParticleType<LightningArcParticleData> particleTypeIn, StringReader reader)
                throws CommandSyntaxException {
            reader.expect(' ');
            float x = reader.readFloat();
            reader.expect(' ');
            float y = reader.readFloat();
            reader.expect(' ');
            float z = reader.readFloat();
            return new LightningArcParticleData(new Vec3d(x, y, z));
        }

        @Override
        public LightningArcParticleData read(ParticleType<LightningArcParticleData> particleTypeIn,
                PacketByteBuf buffer) {
            float x = buffer.readFloat();
            float y = buffer.readFloat();
            float z = buffer.readFloat();
            return new LightningArcParticleData(new Vec3d(x, y, z));
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleTypes.LIGHTNING_ARC;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeFloat((float) target.x);
        buffer.writeFloat((float) target.y);
        buffer.writeFloat((float) target.z);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%.2f %.2f %.2f", target.x, target.y, target.z);
    }

}
