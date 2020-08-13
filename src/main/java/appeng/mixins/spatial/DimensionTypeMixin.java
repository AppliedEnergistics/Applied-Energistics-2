package appeng.mixins.spatial;

import java.util.OptionalLong;

import com.mojang.serialization.Lifecycle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;

import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

/**
 * Adds the storage cell world dimension type as a built-in dimension type. This
 * can be registered as a JSON file as well, but doing so will trigger an
 * experimental feature warning when the world is being loaded.
 */
@Mixin(value = DimensionType.class)
public class DimensionTypeMixin {

    @Invoker("<init>")
    static DimensionType create(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultrawarm,
            boolean natural, double coordinateScale, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks,
            boolean hasRaids, int logicalHeight, ResourceLocation infiniburn, ResourceLocation skyProperties,
            float ambientLight) {
        throw new AssertionError();
    }

    @Inject(method = "func_236027_a_", at = @At("TAIL"))
    private static void addRegistryDefaults(DynamicRegistries.Impl registryTracker, CallbackInfoReturnable<?> cir) {
        DimensionType dimensionType = create(OptionalLong.of(12000), false, false, false, false, 1.0, false, false,
                false, false, 256, BlockTags.INFINIBURN_OVERWORLD.getName(),
                SpatialStorageDimensionIds.SKY_PROPERTIES_ID, 1.0f);

        Registry.register(registryTracker.func_230520_a_(),
                SpatialStorageDimensionIds.DIMENSION_TYPE_ID.func_240901_a_(), dimensionType);
    }

    /**
     * Insert our custom dimension into the initial registry. <em>This is what will
     * ultimately lead to the creation of a new World.</em>
     */
    @Inject(method = "func_242718_a", at = @At("RETURN"))
    private static void buildDimensionRegistry(Registry<DimensionType> dimensionTypes, Registry<Biome> biomes,
            Registry<DimensionSettings> dimensionSettings, long seed,
            CallbackInfoReturnable<SimpleRegistry<Dimension>> cir) {
        SimpleRegistry<Dimension> simpleregistry = cir.getReturnValue();

        simpleregistry.register(SpatialStorageDimensionIds.DIMENSION_ID, new Dimension(() -> {
            return dimensionTypes.func_243576_d(SpatialStorageDimensionIds.DIMENSION_TYPE_ID);
        }, new SpatialStorageChunkGenerator(biomes)), Lifecycle.stable());

    }

}
