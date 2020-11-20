package appeng.spatial;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;

import appeng.core.AppEng;

/**
 * IDs for the spatial storage world related dimension objects.
 */
public final class SpatialStorageDimensionIds {

    /**
     * ID of the {@link DimensionType} used for the spatial storage world.
     * <p>
     * This is defined in {@link appeng.mixins.spatial.DimensionTypeMixin}.
     */
    public static final RegistryKey<DimensionType> DIMENSION_TYPE_ID = RegistryKey.of(Registry.DIMENSION_TYPE_KEY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link net.minecraft.world.gen.chunk.ChunkGenerator} used for the spatial storage world.
     */
    public static final Identifier CHUNK_GENERATOR_ID = AppEng.makeId("spatial_storage");

    /**
     * ID of the {@link net.minecraft.world.biome.Biome} used for the spatial storage world.
     */
    public static final RegistryKey<Biome> BIOME_KEY = RegistryKey.of(Registry.BIOME_KEY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link DimensionOptions} used for the spatial storage dimension.
     * <p>
     * This is defined in {@link appeng.mixins.spatial.DimensionTypeMixin}.
     */
    public static final RegistryKey<DimensionOptions> DIMENSION_ID = RegistryKey.of(Registry.DIMENSION_OPTIONS,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link World} that is instantiated from the dimension/dimension type.
     */
    public static final RegistryKey<World> WORLD_ID = RegistryKey.of(Registry.DIMENSION,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link net.minecraft.client.render.SkyProperties} used for the spatial storage world.
     */
    public static Identifier SKY_PROPERTIES_ID = AppEng.makeId("spatial_storage");

    private SpatialStorageDimensionIds() {
    }

}
