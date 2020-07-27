package appeng.client.render.effects;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

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

    public static void registerClient() {
    }

}
