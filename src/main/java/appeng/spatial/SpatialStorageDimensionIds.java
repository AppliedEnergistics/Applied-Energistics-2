package appeng.spatial;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import appeng.core.AppEng;
import appeng.mixins.spatial.DimensionTypeMixin;

/**
 * IDs for the spatial storage world related dimension objects.
 */
public final class SpatialStorageDimensionIds {

    /**
     * ID of the {@link DimensionType} used for the spatial storage world.
     * <p>
     * This is defined in {@link DimensionTypeMixin}.
     */
    public static final RegistryKey<DimensionType> DIMENSION_TYPE_ID = RegistryKey
            .getOrCreateKey(Registry.DIMENSION_TYPE_KEY, AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link net.minecraft.world.gen.ChunkGenerator} used for the spatial storage world.
     */
    public static final ResourceLocation CHUNK_GENERATOR_ID = AppEng.makeId("spatial_storage");

    /**
     * ID of the {@link net.minecraft.world.biome.Biome} used for the spatial storage world.
     */
    public static final RegistryKey<Biome> BIOME_KEY = RegistryKey.getOrCreateKey(Registry.BIOME_KEY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link Dimension} used for the spatial storage dimension.
     * <p>
     * This is defined in {@link appeng.mixins.spatial.DimensionTypeMixin}.
     */
    public static final RegistryKey<Dimension> DIMENSION_ID = RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link World} that is instantiated from the dimension/dimension type.
     */
    public static final RegistryKey<World> WORLD_ID = RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
            AppEng.makeId("spatial_storage"));

    /**
     * ID of the {@link net.minecraft.client.world.DimensionRenderInfo} used for the spatial storage world.
     */
    public static ResourceLocation SKY_PROPERTIES_ID = AppEng.makeId("spatial_storage");

    private SpatialStorageDimensionIds() {
    }

}
