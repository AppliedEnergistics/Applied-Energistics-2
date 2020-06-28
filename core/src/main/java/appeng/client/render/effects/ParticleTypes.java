package appeng.client.render.effects;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;

import appeng.core.AppEng;

public final class ParticleTypes {

    private ParticleTypes() {
    }

    public static final DefaultParticleType CHARGED_ORE = FabricParticleTypes.simple(false);
    public static final DefaultParticleType CRAFTING = FabricParticleTypes.simple(false);
    public static final ParticleType<EnergyParticleData> ENERGY = FabricParticleTypes.complex(false,
            EnergyParticleData.DESERIALIZER);
    public static final ParticleType<LightningArcParticleData> LIGHTNING_ARC = FabricParticleTypes.complex(false,
            LightningArcParticleData.DESERIALIZER);
    public static final DefaultParticleType LIGHTNING = FabricParticleTypes.simple(false);
    public static final DefaultParticleType MATTER_CANNON = FabricParticleTypes.simple(false);
    public static final DefaultParticleType VIBRANT = FabricParticleTypes.simple(false);

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
