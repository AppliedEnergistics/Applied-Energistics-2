package appeng.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.NopeDecoratorConfig;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

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
 * Hooks into the very early bootstrapping phase to register things before the first dynamic registry manager is
 * created.
 */
public final class AppEngBootstrap {

    private static boolean initialized;

    private AppEngBootstrap() {
    }

    public synchronized static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        AEConfig.load(FabricLoader.getInstance().getConfigDirectory());

        CreativeTab.init();
        FacadeCreativeTab.init();// This call has a side-effect (adding it to the creative screen)

        Api.INSTANCE = new Api();

        registerStructures();

        ConfiguredFeature<?, ?> quartzOreFeature = registerQuartzOreFeature();
        ConfiguredFeature<?, ?> chargedQuartzOreFeature = registerChargedQuartzOreFeature();

        Set<String> meteoriteBiomeBlacklist = new HashSet<>(AEConfig.instance().getMeteoriteBiomeBlacklist());
        Set<String> quartzOreBiomeBlacklist = new HashSet<>(AEConfig.instance().getQuartzOreBiomeBlacklist());

        // Add features to all existing biomes
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : BuiltinRegistries.BIOME.getEntries()) {
            Identifier id = entry.getKey().getValue();
            Biome b = entry.getValue();
            addMeteoriteWorldGen(id, b, meteoriteBiomeBlacklist);
            addQuartzWorldGen(id, b, quartzOreFeature, chargedQuartzOreFeature, quartzOreBiomeBlacklist);
        }

        // Listen to added biomes for post-processing
        RegistryEntryAddedCallback.event(BuiltinRegistries.BIOME).register((i, id, biome) -> {
            addMeteoriteWorldGen(id, biome, meteoriteBiomeBlacklist);
            addQuartzWorldGen(id, biome, quartzOreFeature, chargedQuartzOreFeature, quartzOreBiomeBlacklist);
        });

        registerDimension();
    }

    private static void registerStructures() {

        MeteoriteStructurePiece.register();

        // Registering into the registry alone is INSUFFICIENT!
        // There's a bidirectional map in the Structure class itself primarily for the
        // purposes of NBT serialization
        StructureFeatureAccessor.register(MeteoriteStructure.ID.toString(), MeteoriteStructure.INSTANCE,
                GenerationStep.Feature.TOP_LAYER_MODIFICATION);

        ConfiguredStructureFeaturesAccessor.register(MeteoriteStructure.ID.toString(),
                MeteoriteStructure.CONFIGURED_INSTANCE);
    }

    private static void addMeteoriteWorldGen(Identifier id, Biome b, Set<String> biomeBlacklist) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            AELog.debug("Not generating meteorites in %s because the feature is disabled", id);
            return;
        }

        if (isBlacklistedBiomeCategory(b.getCategory())) {
            AELog.debug("Not generating meteorites in %s because it's of category %s", id, b.getCategory());
            return;
        }

        if (biomeBlacklist.contains(id.toString())) {
            AELog.debug("Not generating meteorites in %s because the biome is blacklisted in the config", id);
            return;
        }

        BiomeModifier modifier = new BiomeModifier(b);
        modifier.addStructureFeature(MeteoriteStructure.CONFIGURED_INSTANCE);
    }

    private static void addQuartzWorldGen(Identifier id, Biome b, ConfiguredFeature<?, ?> quartzOre,
            ConfiguredFeature<?, ?> chargedQuartz, Set<String> biomeBlacklist) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.CERTUS_QUARTZ_WORLD_GEN)) {
            AELog.debug("Not generating quartz-ore in %s because the feature is disabled", id);
            return;
        }

        if (isBlacklistedBiomeCategory(b.getCategory())) {
            AELog.debug("Not generating quartz ore in %s because it's of category %s", id, b.getCategory());
            return;
        }

        if (biomeBlacklist.contains(id.toString())) {
            AELog.debug("Not generating quartz-ore in %s because the biome is blacklisted in the config", id);
            return;
        }

        BiomeModifier modifier = new BiomeModifier(b);
        modifier.addFeature(GenerationStep.Feature.UNDERGROUND_ORES, quartzOre);

        if (AEConfig.instance().isFeatureEnabled(AEFeature.CHARGED_CERTUS_ORE)) {
            modifier.addFeature(GenerationStep.Feature.UNDERGROUND_DECORATION, chargedQuartz);
        }
    }

    private static boolean isBlacklistedBiomeCategory(Biome.Category category) {
        return category == Biome.Category.THEEND || category == Biome.Category.NETHER
                || category == Biome.Category.NONE;
    }

    private static ConfiguredFeature<?, ?> registerQuartzOreFeature() {
        // Tell Minecraft about our configured quartz ore feature
        BlockState quartzOreState = Api.instance().definitions().blocks().quartzOre().block().getDefaultState();
        return ConfiguredFeaturesAccessor.register(AppEng.makeId("quartz_ore").toString(),
                Feature.ORE
                        .configure(new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, quartzOreState,
                                AEConfig.instance().getQuartzOresPerCluster()))
                        .decorate(Decorator.RANGE
                                .configure(new RangeDecoratorConfig(YOffset.fixed(12), YOffset.fixed(72))))
                        .spreadHorizontally()
                        .repeat(AEConfig.instance().getQuartzOresClusterAmount()));
    }

    private static ConfiguredFeature<?, ?> registerChargedQuartzOreFeature() {
        // Tell Minecraft about our configured charged quartz ore feature
        Registry.register(Registry.FEATURE, AppEng.makeId("charged_quartz_ore"), ChargedQuartzOreFeature.INSTANCE);

        BlockState quartzOreState = Api.instance().definitions().blocks().quartzOre().block().getDefaultState();
        BlockState chargedQuartzOreState = Api.instance().definitions().blocks().quartzOreCharged().block()
                .getDefaultState();
        return ConfiguredFeaturesAccessor.register(AppEng.makeId("charged_quartz_ore").toString(),
                ChargedQuartzOreFeature.INSTANCE
                        .configure(new ChargedQuartzOreConfig(quartzOreState, chargedQuartzOreState,
                                AEConfig.instance().getSpawnChargedChance()))
                        .decorate(Decorator.NOPE.configure(NopeDecoratorConfig.INSTANCE)));
    }

    private static void registerDimension() {
        Registry.register(Registry.CHUNK_GENERATOR, SpatialStorageDimensionIds.CHUNK_GENERATOR_ID,
                SpatialStorageChunkGenerator.CODEC);
    }

}
