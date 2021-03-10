package appeng.mixins.spatial;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import appeng.spatial.SpatialStorageDimensionIds;
import appeng.spatial.SpatialStorageSkyProperties;

@Mixin(DimensionRenderInfo.class)
public class SkyPropertiesMixin {

    @Shadow
    private static Object2ObjectMap<ResourceLocation, DimensionRenderInfo> EFFECTS/* BY_IDENTIFIER */;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void init(CallbackInfo ci) {
        EFFECTS.put(SpatialStorageDimensionIds.SKY_PROPERTIES_ID, SpatialStorageSkyProperties.INSTANCE);
    }

}
