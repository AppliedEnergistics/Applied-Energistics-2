package appeng.init.worldgen;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.dimension.DimensionType;

import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

public final class InitDimensionTypes {
    private InitDimensionTypes() {
    }

    public static void init(BootstrapContext<DimensionType> context) {
        DimensionType dimensionType = createSpatialDimensionType(context);

        context.register(SpatialStorageDimensionIds.DIMENSION_TYPE_ID,
                dimensionType);
    }

    @NotNull
    private static DimensionType createSpatialDimensionType(BootstrapContext<DimensionType> context) {
        var timelines = context.lookup(Registries.TIMELINE);

        return new DimensionType(
                true, // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                1.0, // coordinateScale
                SpatialStorageChunkGenerator.MIN_Y, // minY
                SpatialStorageChunkGenerator.HEIGHT, // height
                SpatialStorageChunkGenerator.HEIGHT, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(ConstantInt.of(0), 0),
                DimensionType.Skybox.OVERWORLD,
                DimensionType.CardinalLightType.DEFAULT,
                EnvironmentAttributeMap.builder()
                        .set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES)
                        .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false)
                        // TODO 1.21.11: environmental effects SpatialStorageDimensionIds.SKY_PROPERTIES_ID
                        .build(),
                timelines.getOrThrow(TimelineTags.UNIVERSAL));
    }
}
