package appeng.mixins.spatial;

import java.util.OptionalLong;

import com.mojang.serialization.Lifecycle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * Adds the storage cell world dimension type as a built-in dimension type. This can be registered as a JSON file as
 * well, but doing so will trigger an experimental feature warning when the world is being loaded.
 */
@Mixin(DimensionType.class)
public class DimensionTypeMixin {

    @Invoker("<init>")
    static DimensionType create(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultrawarm,
            boolean natural, double coordinateScale, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks,
            boolean hasRaids, int logicalHeight, Identifier infiniburn, Identifier skyProperties, float ambientLight) {
        throw new AssertionError();
    }

    @Inject(method = "addRegistryDefaults", at = @At("TAIL"))
    private static void addRegistryDefaults(DynamicRegistryManager.Impl registryTracker,
            CallbackInfoReturnable<?> cir) {
        DimensionType dimensionType = create(OptionalLong.of(12000), false, false, false, false, 1.0, false, false,
                false, false, 256, BlockTags.INFINIBURN_OVERWORLD.getId(), SpatialStorageDimensionIds.SKY_PROPERTIES_ID,
                1.0f);

        Registry.register(registryTracker.get(Registry.DIMENSION_TYPE_KEY),
                SpatialStorageDimensionIds.DIMENSION_TYPE_ID.getValue(),
                dimensionType);
    }

    @Inject(method = "createDefaultDimensionOptions", at = @At("RETURN"))
    private static void buildDimensionRegistry(Registry<DimensionType> dimensionTypes, Registry<Biome> biomes,
            Registry<ChunkGeneratorSettings> chunkGeneratorSettings, long seed,
            CallbackInfoReturnable<SimpleRegistry<DimensionOptions>> cir) {
        SimpleRegistry<DimensionOptions> simpleregistry = cir.getReturnValue();

        simpleregistry.add(SpatialStorageDimensionIds.DIMENSION_ID, new DimensionOptions(() -> {
            return dimensionTypes.get(SpatialStorageDimensionIds.DIMENSION_TYPE_ID);
        }, new SpatialStorageChunkGenerator(biomes)), Lifecycle.stable());

    }

}
