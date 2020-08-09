package appeng.mixins.spatial;

import appeng.spatial.SpatialStorageDimensionIds;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.Dimension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.Map;

/**
 * This Mixin tries to hide the screen that warns users about experimental
 * features being used, but only if AE2's dimension is the only added dimension.
 */
@Mixin(Dimension.class)
public class DimensionOptionMixin {

    /**
     * This injects right after the method makes a local copy of all present
     * dimensions, and modifies the list by removing our mod-provided dimension from
     * it.
     * <p>
     * This means Vanilla will perform it's check whether it should display an
     * experimental warning without considering our dimension.
     */
    @ModifyVariable(method = "func_236060_a_", at = @At(value = "INVOKE_ASSIGN", ordinal = 0, target = "Lcom/google/common/collect/Lists;newArrayList(Ljava/lang/Iterable;)Ljava/util/ArrayList;", remap = false), allow = 1)
    private static List<Map.Entry<RegistryKey<Dimension>, Dimension>> overrideExperimentalCheck(
            List<Map.Entry<RegistryKey<Dimension>, Dimension>> dimensions) {
        // this only removes our dimension from the check
        dimensions.removeIf(e -> e.getKey() == SpatialStorageDimensionIds.DIMENSION_ID);
        return dimensions;
    }

}