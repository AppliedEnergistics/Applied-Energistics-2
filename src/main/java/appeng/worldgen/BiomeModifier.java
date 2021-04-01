package appeng.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import net.fabricmc.fabric.mixin.structure.BiomeAccessor;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;

import appeng.mixins.structure.GenerationSettingsAccessor;

public final class BiomeModifier {

    private final BiomeAccessor biomeAccessor;

    private final GenerationSettingsAccessor generationSettingsAccessor;

    @SuppressWarnings("ConstantConditions")
    public BiomeModifier(Biome biome) {
        this.biomeAccessor = (BiomeAccessor) (Object) biome;
        this.generationSettingsAccessor = (GenerationSettingsAccessor) biome.getGenerationSettings();
    }

    public void addFeature(GenerationStage.Decoration step, ConfiguredFeature<?, ?> feature) {

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

    public void addStructureFeature(StructureFeature<?, ?> structure) {
        List<Supplier<StructureFeature<?, ?>>> features = new ArrayList<>(
                generationSettingsAccessor.getStructureFeatures());
        features.add(() -> structure);
        generationSettingsAccessor.setStructureFeatures(features);

        // Add it to the structures that will generate pieces within this biome,
        // this is only half-correct since a structure can start in an adjacent biome
        // and extend into biomes that would usually not start the structure
        Map<Integer, List<Structure<?>>> structuresByStage = biomeAccessor.getStructureLists();
        int step = structure.field_236268_b_.getDecorationStage().ordinal();
        if (!structuresByStage.containsKey(step)) {
            structuresByStage.put(step, new ArrayList<>());
        }
        structuresByStage.get(step).add(structure.field_236268_b_);
    }

}
