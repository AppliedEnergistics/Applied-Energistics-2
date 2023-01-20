package appeng.init.worldgen;

import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalLong;

public final class InitDimensionTypes {
    private InitDimensionTypes() {
    }

    public static void init(BootstapContext<DimensionType> context) {
        DimensionType dimensionType = createSpatialDimensionType();

        context.register(SpatialStorageDimensionIds.DIMENSION_TYPE_ID,
                dimensionType);
    }

    @NotNull
    private static DimensionType createSpatialDimensionType() {
        return new DimensionType(
                OptionalLong.of(12000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                false, // bedWorks
                false, // respawnAnchorWorks
                SpatialStorageChunkGenerator.MIN_Y, // minY
                SpatialStorageChunkGenerator.HEIGHT, // height
                SpatialStorageChunkGenerator.HEIGHT, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                SpatialStorageDimensionIds.SKY_PROPERTIES_ID, // effectsLocation
                1.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        );
    }
}
