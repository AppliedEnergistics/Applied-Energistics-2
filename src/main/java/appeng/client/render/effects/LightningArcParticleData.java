package appeng.client.render.effects;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Contains the target point of the lightning arc (the source point is infered from the particle starting position).
 */
public class LightningArcParticleData implements IParticleData {

    public final Vector3d target;

    public LightningArcParticleData(Vector3d target) {
        this.target = target;
    }

    public static final IDeserializer<LightningArcParticleData> DESERIALIZER = new IDeserializer<LightningArcParticleData>() {
        @Override
        public LightningArcParticleData deserialize(ParticleType<LightningArcParticleData> particleTypeIn,
                StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float x = reader.readFloat();
            reader.expect(' ');
            float y = reader.readFloat();
            reader.expect(' ');
            float z = reader.readFloat();
            return new LightningArcParticleData(new Vector3d(x, y, z));
        }

        @Override
        public LightningArcParticleData read(ParticleType<LightningArcParticleData> particleTypeIn,
                PacketBuffer buffer) {
            float x = buffer.readFloat();
            float y = buffer.readFloat();
            float z = buffer.readFloat();
            return new LightningArcParticleData(new Vector3d(x, y, z));
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleTypes.LIGHTNING_ARC;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeFloat((float) target.x);
        buffer.writeFloat((float) target.y);
        buffer.writeFloat((float) target.z);
    }

    @Override
    public String getParameters() {
        return String.format(Locale.ROOT, "%.2f %.2f %.2f", target.x, target.y, target.z);
    }

}
