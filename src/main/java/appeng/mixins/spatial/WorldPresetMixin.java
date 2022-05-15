package appeng.mixins.spatial;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

@Mixin(WorldPreset.class)
public class WorldPresetMixin {

    @Shadow
    @Mutable
    private Map<ResourceKey<LevelStem>, LevelStem> dimensions;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addSpatialDimension(CallbackInfo ci) {
        if (!dimensions.containsKey(SpatialStorageDimensionIds.DIMENSION_ID)) {
            dimensions = new HashMap<>(dimensions);
            dimensions.put(
                    SpatialStorageDimensionIds.DIMENSION_ID,
                    new LevelStem(
                            BuiltinRegistries.DIMENSION_TYPE
                                    .getOrCreateHolder(SpatialStorageDimensionIds.DIMENSION_TYPE_ID),
                            new SpatialStorageChunkGenerator(BuiltinRegistries.STRUCTURE_SETS,
                                    BuiltinRegistries.BIOME)));
            dimensions = ImmutableMap.copyOf(dimensions);
        }
    }

}
