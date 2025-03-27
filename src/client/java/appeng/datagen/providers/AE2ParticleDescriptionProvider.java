package appeng.datagen.providers;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

import appeng.core.AppEng;
import appeng.core.particles.ParticleTypes;

public class AE2ParticleDescriptionProvider extends ParticleDescriptionProvider {
    public AE2ParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        this.sprite(ParticleTypes.CRAFTING, ResourceLocation.withDefaultNamespace("generic_0"));
        this.sprite(ParticleTypes.ENERGY, AppEng.makeId("energy"));
        this.sprite(ParticleTypes.LIGHTNING_ARC, ResourceLocation.withDefaultNamespace("generic_0"));
        this.sprite(ParticleTypes.LIGHTNING, ResourceLocation.withDefaultNamespace("generic_0"));
        this.sprite(ParticleTypes.MATTER_CANNON, AppEng.makeId("matter_cannon"));
        this.sprite(ParticleTypes.VIBRANT, ResourceLocation.withDefaultNamespace("generic_0"));

    }
}
