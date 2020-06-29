package appeng.client.render.effects;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;

import appeng.core.AppEng;
import net.minecraft.util.registry.Registry;

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

    public static void register() {
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("charged_ore_fx"), CHARGED_ORE);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("crafting_fx"), CRAFTING);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("energy_fx"), ENERGY);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("lightning_arc_fx"), LIGHTNING_ARC);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("lightning_fx"), LIGHTNING);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("matter_cannon_fx"), MATTER_CANNON);
        Registry.register(Registry.PARTICLE_TYPE, AppEng.makeId("vibrant_fx"), VIBRANT);
    }

}
