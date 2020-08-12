package appeng.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;

import appeng.mixins.structure.BiomeAccessor;
import appeng.mixins.structure.GenerationSettingsAccessor;

public final class BiomeModifier {

    private final BiomeAccessor biomeAccessor;

    private final GenerationSettingsAccessor generationSettingsAccessor;

    @SuppressWarnings("ConstantConditions")
    public BiomeModifier(Biome biome) {
        this.biomeAccessor = (BiomeAccessor) (Object) biome;
        this.generationSettingsAccessor = (GenerationSettingsAccessor) biome.getGenerationSettings();
    }

    public void addFeature(GenerationStep.Feature step, ConfiguredFeature<?, ?> feature) {

        int stepIndex = step.ordinal();
        List<List<Supplier<ConfiguredFeature<?, ?>>>> featuresByStep = new ArrayList<>(
                generationSettingsAccessor.getFeatures());

        while (featuresByStep.size() <= stepIndex) {
            featuresByStep.add(Lists.newArrayList());
        }

        List<Supplier<ConfiguredFeature<?, ?>>> features = new ArrayList<>(featuresByStep.get(stepIndex));
        features.add(() -> feature);
        featuresByStep.set(stepIndex, features);

        generationSettingsAccessor.setFeatures(featuresByStep);

    }

    public void addStructureFeature(ConfiguredStructureFeature<?, ?> structure) {
        List<Supplier<ConfiguredStructureFeature<?, ?>>> features = new ArrayList<>(
                generationSettingsAccessor.getStructureFeatures());
        features.add(() -> structure);
        generationSettingsAccessor.setStructureFeatures(features);

        // Add it to the structures that will generate pieces within this biome,
        // this is only half-correct since a structure can start in an adjacent biome
        // and extend into biomes that would usually not start the structure
        Map<Integer, List<StructureFeature<?>>> structuresByStage = biomeAccessor.getField_26634();
        int step = structure.feature.getGenerationStep().ordinal();
        if (!structuresByStage.containsKey(step)) {
            structuresByStage.put(step, new ArrayList<>());
        }
        structuresByStage.get(step).add(structure.feature);
    }

}
