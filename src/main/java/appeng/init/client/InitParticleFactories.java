package appeng.init.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;

import appeng.client.render.effects.ChargedOreFX;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.MatterCannonFX;
import appeng.client.render.effects.ParticleTypes;
import appeng.client.render.effects.VibrantFX;

public final class InitParticleFactories {

    private InitParticleFactories() {
    }

    public static void init() {
        ParticleManager particles = Minecraft.getInstance().particles;
        particles.registerFactory(ParticleTypes.CHARGED_ORE, ChargedOreFX.Factory::new);
        particles.registerFactory(ParticleTypes.CRAFTING, CraftingFx.Factory::new);
        particles.registerFactory(ParticleTypes.ENERGY, EnergyFx.Factory::new);
        particles.registerFactory(ParticleTypes.LIGHTNING_ARC, LightningArcFX.Factory::new);
        particles.registerFactory(ParticleTypes.LIGHTNING, LightningFX.Factory::new);
        particles.registerFactory(ParticleTypes.MATTER_CANNON, MatterCannonFX.Factory::new);
        particles.registerFactory(ParticleTypes.VIBRANT, VibrantFX.Factory::new);
    }

}
