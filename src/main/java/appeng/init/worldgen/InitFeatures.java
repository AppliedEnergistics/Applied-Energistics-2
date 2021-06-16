package appeng.init.worldgen;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.NoPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.AEConfig;
import appeng.core.api.definitions.ApiBlocks;
import appeng.mixins.feature.ConfiguredFeaturesAccessor;
import appeng.worldgen.ChargedQuartzOreConfig;
import appeng.worldgen.ChargedQuartzOreFeature;

public final class InitFeatures {

    private InitFeatures() {
    }

    public static void init(IForgeRegistry<Feature<?>> registry) {
        // Tell Minecraft about our charged quartz ore feature
        ChargedQuartzOreFeature.INSTANCE.setRegistryName(WorldgenIds.CHARGED_QUARTZ_ORE);
        registry.register(ChargedQuartzOreFeature.INSTANCE);

        // Register the configured versions of our features
        registerQuartzOreFeature();
        registerChargedQuartzOreFeature();
    }

    private static void registerQuartzOreFeature() {
        // Tell Minecraft about our configured quartz ore feature
        BlockState quartzOreState = ApiBlocks.QUARTZ_ORE.block().getDefaultState();
        ConfiguredFeaturesAccessor.register(WorldgenIds.QUARTZ_ORE.toString(), Feature.ORE
                .withConfiguration(
                        new OreFeatureConfig(OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, quartzOreState,
                                AEConfig.instance().getQuartzOresPerCluster()))
                .withPlacement(Placement.RANGE/* RANGE */.configure(new TopSolidRangeConfig(12, 12, 72)))
                .square/* spreadHorizontally */()
                .count/* repeat */(AEConfig.instance().getQuartzOresClusterAmount()));
    }

    private static void registerChargedQuartzOreFeature() {
        BlockState quartzOreState = ApiBlocks.QUARTZ_ORE.block().getDefaultState();
        BlockState chargedQuartzOreState = ApiBlocks.QUARTZ_ORE_CHARGED.block()
                .getDefaultState();
        ConfiguredFeaturesAccessor.register(WorldgenIds.CHARGED_QUARTZ_ORE.toString(),
                ChargedQuartzOreFeature.INSTANCE
                        .withConfiguration(new ChargedQuartzOreConfig(quartzOreState, chargedQuartzOreState,
                                AEConfig.instance().getSpawnChargedChance()))
                        .withPlacement(Placement.NOPE.configure(NoPlacementConfig.INSTANCE)));
    }

}
