package appeng.datagen.providers;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.ParticleDescriptionProvider;

import appeng.core.AppEng;
import appeng.core.particles.ParticleTypes;

public class AE2ParticleDescriptionProvider extends ParticleDescriptionProvider {
    public AE2ParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        this.spriteSet(ParticleTypes.CRAFTING, Identifier.withDefaultNamespace("generic_0"));
        this.spriteSet(ParticleTypes.ENERGY, AppEng.makeId("energy"));
        this.spriteSet(ParticleTypes.LIGHTNING_ARC, Identifier.withDefaultNamespace("generic_0"));
        this.spriteSet(ParticleTypes.LIGHTNING, Identifier.withDefaultNamespace("generic_0"));
        this.spriteSet(ParticleTypes.MATTER_CANNON, AppEng.makeId("matter_cannon"));
        this.spriteSet(ParticleTypes.VIBRANT, Identifier.withDefaultNamespace("generic_0"));

    }
}
