package appeng.client.render.effects;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;

public final class ParticleTypes {

    private ParticleTypes() {
    }

    public static final BasicParticleType CHARGED_ORE = FabricParticleTypes.simple(false);
    public static final BasicParticleType CRAFTING = FabricParticleTypes.simple(false);
    public static final ParticleType<EnergyParticleData> ENERGY = FabricParticleTypes.complex(false,
            EnergyParticleData.DESERIALIZER);
    public static final ParticleType<LightningArcParticleData> LIGHTNING_ARC = FabricParticleTypes.complex(false,
            LightningArcParticleData.DESERIALIZER);
    public static final BasicParticleType LIGHTNING = FabricParticleTypes.simple(false);
    public static final BasicParticleType MATTER_CANNON = FabricParticleTypes.simple(false);
    public static final BasicParticleType VIBRANT = FabricParticleTypes.simple(false);

    public static void registerClient() {
    }

}
