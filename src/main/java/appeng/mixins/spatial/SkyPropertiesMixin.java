package appeng.mixins.spatial;

import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStorageSkyProperties;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionRenderInfo.class)
public class SkyPropertiesMixin {

    @Shadow
    private static Object2ObjectMap<ResourceLocation, DimensionRenderInfo> field_239208_a_/*BY_IDENTIFIER*/;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        field_239208_a_.put(SpatialStorageDimensionIds.SKY_PROPERTIES_ID, SpatialStorageSkyProperties.INSTANCE);
    }

}
