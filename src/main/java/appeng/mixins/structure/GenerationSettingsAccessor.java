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

    @Accessor("field_242484_f")
    List<List<Supplier<ConfiguredFeature<?, ?>>>> getFeatures();

    @Accessor("field_242484_f")
    void setFeatures(List<List<Supplier<ConfiguredFeature<?, ?>>>> features);

    @Accessor("field_242485_g")
    List<Supplier<StructureFeature<?, ?>>> getStructureFeatures();

    @Accessor("field_242485_g")
    void setStructureFeatures(List<Supplier<StructureFeature<?, ?>>> structureFeatures);

}
