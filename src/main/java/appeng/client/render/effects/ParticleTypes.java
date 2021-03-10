package appeng.client.render.effects;

import com.mojang.serialization.Codec;

import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;

import appeng.core.AppEng;

public final class ParticleTypes {

    private ParticleTypes() {
    }

    public static final BasicParticleType CHARGED_ORE = new BasicParticleType(false);
    public static final BasicParticleType CRAFTING = new BasicParticleType(false);
    public static final ParticleType<EnergyParticleData> ENERGY = new ParticleType<EnergyParticleData>(false,
            EnergyParticleData.DESERIALIZER) {
        public Codec<EnergyParticleData> codec() {
            return null;
        }
    };
    public static final ParticleType<LightningArcParticleData> LIGHTNING_ARC = new ParticleType<LightningArcParticleData>(
            false, LightningArcParticleData.DESERIALIZER) {
        @Override
        public Codec<LightningArcParticleData> codec() {
            return null;
        }
    };
    public static final BasicParticleType LIGHTNING = new BasicParticleType(false);
    public static final BasicParticleType MATTER_CANNON = new BasicParticleType(false);
    public static final BasicParticleType VIBRANT = new BasicParticleType(false);

    static {
        CHARGED_ORE.setRegistryName(AppEng.MOD_ID, "charged_ore_fx");
        CRAFTING.setRegistryName(AppEng.MOD_ID, "crafting_fx");
        ENERGY.setRegistryName(AppEng.MOD_ID, "energy_fx");
        LIGHTNING_ARC.setRegistryName(AppEng.MOD_ID, "lightning_arc_fx");
        LIGHTNING.setRegistryName(AppEng.MOD_ID, "lightning_fx");
        MATTER_CANNON.setRegistryName(AppEng.MOD_ID, "matter_cannon_fx");
        VIBRANT.setRegistryName(AppEng.MOD_ID, "vibrant_fx");
    }

}
