package appeng.core;

import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;

import appeng.api.features.AEFeature;
import appeng.mixins.feature.ConfiguredFeaturesAccessor;
import appeng.mixins.structure.ConfiguredStructureFeaturesAccessor;
import appeng.mixins.structure.StructureFeatureAccessor;
import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.worldgen.BiomeModifier;
import appeng.worldgen.ChargedQuartzOreConfig;
import appeng.worldgen.ChargedQuartzOreFeature;
import appeng.worldgen.meteorite.MeteoriteStructure;
import appeng.worldgen.meteorite.MeteoriteStructurePiece;

/**
 * Hooks into the very early bootstrapping phase to register things before the
 * first dynamic registry manager is created.
 */
public final class AppEngBootstrap {

    private static boolean initialized;
    private static ConfiguredFeature<?, ?> quartzOreFeature;
    private static ConfiguredFeature<?, ?> chargedQuartzOreFeature;

    private AppEngBootstrap() {
    }

    public synchronized static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        registerStructures();

        quartzOreFeature = registerQuartzOreFeature();
        chargedQuartzOreFeature = registerChargedQuartzOreFeature();

        registerDimension();
    }

    public synchronized static void enhanceBiomes() {
        // add to all standard biomes
        // TODO: This means we'll not add these things to newly created biomes
        WorldGenRegistries.field_243657_i.forEach(b -> {
            addMeteoriteWorldGen(b);
            addQuartzWorldGen(b, quartzOreFeature, chargedQuartzOreFeature);
        });
    }

    private static void registerStructures() {

        MeteoriteStructurePiece.register();

        // Registering into the registry alone is INSUFFICIENT!
        // There's a bidirectional map in the Structure class itself primarily for the
        // purposes of NBT serialization
        StructureFeatureAccessor.register(MeteoriteStructure.ID.toString(), MeteoriteStructure.INSTANCE,
                GenerationStage.Decoration.TOP_LAYER_MODIFICATION);

        ConfiguredStructureFeaturesAccessor.register(MeteoriteStructure.ID.toString(),
                MeteoriteStructure.CONFIGURED_INSTANCE);
    }

    private static void addMeteoriteWorldGen(Biome b) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            return;
        }

        if (b.getCategory() == Biome.Category.THEEND || b.getCategory() == Biome.Category.NETHER) {
            return;
        }

        BiomeModifier modifier = new BiomeModifier(b);
        modifier.addStructureFeature(MeteoriteStructure.CONFIGURED_INSTANCE);
    }

    private static void addQuartzWorldGen(Biome b, ConfiguredFeature<?, ?> quartzOre,
            ConfiguredFeature<?, ?> chargedQuartz) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            return;
        }

        BiomeModifier modifier = new BiomeModifier(b);

        modifier.addFeature(GenerationStage.Decoration.UNDERGROUND_ORES, quartzOre);

        if (AEConfig.instance().isFeatureEnabled(AEFeature.CHARGED_CERTUS_ORE)) {
            modifier.addFeature(GenerationStage.Decoration.UNDERGROUND_DECORATION, chargedQuartz);
        }
    }

    private static ConfiguredFeature<?, ?> registerQuartzOreFeature() {
        // Tell Minecraft about our configured quartz ore feature
        BlockState quartzOreState = Api.instance().definitions().blocks().quartzOre().block().getDefaultState();
        return ConfiguredFeaturesAccessor.register(AppEng.makeId("quartz_ore").toString(), Feature.ORE
                .withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.field_241882_a, quartzOreState,
                        AEConfig.instance().getQuartzOresPerCluster()))
                .withPlacement(Placement.field_242907_l/* RANGE */.configure(new TopSolidRangeConfig(12, 12, 72)))
                .func_242728_a/* spreadHorizontally */()
                .func_242731_b/* repeat */(AEConfig.instance().getQuartzOresClusterAmount()));
    }

    private static ConfiguredFeature<?, ?> registerChargedQuartzOreFeature() {
        // Tell Minecraft about our configured charged quartz ore feature
        Registry.register(Registry.FEATURE, AppEng.makeId("charged_quartz_ore"), ChargedQuartzOreFeature.INSTANCE);

        BlockState quartzOreState = Api.instance().definitions().blocks().quartzOre().block().getDefaultState();
        BlockState chargedQuartzOreState = Api.instance().definitions().blocks().quartzOreCharged().block()
                .getDefaultState();
        return ConfiguredFeaturesAccessor.register(AppEng.makeId("charged_quartz_ore").toString(),
                ChargedQuartzOreFeature.INSTANCE
                        .withConfiguration(new ChargedQuartzOreConfig(quartzOreState, chargedQuartzOreState,
                                AEConfig.instance().getSpawnChargedChance()))
                        .withPlacement(Placement.NOPE.configure(NoPlacementConfig.field_236556_b_)));
    }

    private static void registerDimension() {
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, SpatialStorageDimensionIds.CHUNK_GENERATOR_ID,
                SpatialStorageChunkGenerator.CODEC);
    }

}
