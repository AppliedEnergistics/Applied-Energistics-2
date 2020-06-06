package appeng.client.render.effects;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;

import java.util.Locale;

/**
 * Contains the target point of the lightning arc (the source point is infered from the particle starting position).
 */
public class LightningArcParticleData implements IParticleData {

    public final Vec3f target;

    public LightningArcParticleData(Vec3f target) {
        this.target = target;
    }

    public static final IDeserializer<LightningArcParticleData> DESERIALIZER = new IDeserializer<LightningArcParticleData>() {
        @Override
        public LightningArcParticleData deserialize(ParticleType<LightningArcParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float x = reader.readFloat();
            reader.expect(' ');
            float y = reader.readFloat();
            reader.expect(' ');
            float z = reader.readFloat();
            return new LightningArcParticleData(new Vec3f(x, y, z));
        }

        @Override
        public LightningArcParticleData read(ParticleType<LightningArcParticleData> particleTypeIn, PacketBuffer buffer) {
            float x = buffer.readFloat();
            float y = buffer.readFloat();
            float z = buffer.readFloat();
            return new LightningArcParticleData(new Vec3f(x, y, z));
        }
    };

    @Override
    public ParticleType<?> getType() {
        return LightningArcFX.TYPE;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeFloat(target.x);
        buffer.writeFloat(target.y);
        buffer.writeFloat(target.z);
    }

    @Override
    public String getParameters() {
        return String.format(Locale.ROOT, "%.2f %.2f %.2f", target.x, target.y, target.z);
    }

}
