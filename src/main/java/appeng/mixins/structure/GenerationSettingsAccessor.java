package appeng.mixins.structure;

import java.util.List;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;

/**
 * Allows the settings in a Biome's generation settings to be modified.
 */
@Mixin(BiomeGenerationSettings.class)
public interface GenerationSettingsAccessor {

    @Accessor
    List<List<Supplier<ConfiguredFeature<?, ?>>>> getFeatures();

    @Accessor
    void setFeatures(List<List<Supplier<ConfiguredFeature<?, ?>>>> features);

    @Accessor
    List<Supplier<StructureFeature<?, ?>>> getStructureFeatures();

    @Accessor
    void setStructureFeatures(List<Supplier<StructureFeature<?, ?>>> structureFeatures);

}
