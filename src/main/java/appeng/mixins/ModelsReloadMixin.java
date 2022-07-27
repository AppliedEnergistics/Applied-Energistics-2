package appeng.mixins;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import appeng.hooks.ModelsReloadCallback;

@Mixin(ModelBakery.class)
public abstract class ModelsReloadMixin {

    @Shadow
    @Final
    public static ModelResourceLocation MISSING_MODEL_LOCATION;

    @Shadow
    public abstract @Nullable BakedModel bake(ResourceLocation location, ModelState transform);

    private BakedModel missingModel;

    // Use linked hash map
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", ordinal = 2))
    private <K, V> HashMap<K, V> newHashMap() {
        return new LinkedHashMap<>();
    }

    @Redirect(method = "method_4733", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object onPut(Map<ResourceLocation, BakedModel> instance, Object key, Object value) {
        // Save the missing model
        if (key == MISSING_MODEL_LOCATION)
            missingModel = (BakedModel) value;

        return instance.put((ResourceLocation) key,
                ModelsReloadCallback.EVENT.invoker().onModelLoaded((ResourceLocation) key,
                        (BakedModel) value, missingModel));
    }

}
