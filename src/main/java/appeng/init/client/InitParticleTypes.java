package appeng.init.client;

import net.minecraft.particles.ParticleType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.client.render.effects.ParticleTypes;

public final class InitParticleTypes {

    private InitParticleTypes() {
    }

    public static void init(IForgeRegistry<ParticleType<?>> registry) {
        registry.register(ParticleTypes.CHARGED_ORE);
        registry.register(ParticleTypes.CRAFTING);
        registry.register(ParticleTypes.ENERGY);
        registry.register(ParticleTypes.LIGHTNING_ARC);
        registry.register(ParticleTypes.LIGHTNING);
        registry.register(ParticleTypes.MATTER_CANNON);
        registry.register(ParticleTypes.VIBRANT);
    }

}
