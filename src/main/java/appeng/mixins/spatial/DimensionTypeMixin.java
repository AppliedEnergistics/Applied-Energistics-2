package appeng.mixins.spatial;

import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalLong;

/**
 * Adds the storage cell world dimension type as a built-in dimension type. This
 * can be registered as a JSON file as well, but doing so will trigger an
 * experimental feature warning when the world is being loaded.
 */
@Mixin(DimensionType.class)
public class DimensionTypeMixin {

    @Invoker("<init>")
    static DimensionType create(OptionalLong fixedTime, boolean hasSkylight, boolean hasCeiling, boolean ultrawarm, boolean natural, double coordinateScale, boolean piglinSafe, boolean bedWorks, boolean respawnAnchorWorks, boolean hasRaids, int logicalHeight, Identifier infiniburn, Identifier skyProperties, float ambientLight) {
        throw new AssertionError();
    }

    @Inject(method = "addRegistryDefaults", at = @At("TAIL"))
    private static void addRegistryDefaults(DynamicRegistryManager.Impl registryTracker, CallbackInfoReturnable<?> cir) {
        DimensionType dimensionType = create(OptionalLong.of(12000), false, false, false, false, 1.0,
                false, false, false, false, 256, BlockTags.INFINIBURN_OVERWORLD.getId(), SpatialStorageDimensionIds.SKY_PROPERTIES_ID, 1.0f);

        Registry.register(registryTracker.getDimensionTypes(),
                SpatialStorageDimensionIds.DIMENSION_TYPE_ID.getValue(), dimensionType);
    }

}
