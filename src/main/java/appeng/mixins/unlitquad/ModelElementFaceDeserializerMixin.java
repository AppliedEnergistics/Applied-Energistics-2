package appeng.mixins.unlitquad;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.model.json.ModelElementFace;

import appeng.hooks.UnlitQuadHooks;

/**
 * This mixin will call the hook to deserialize the unlit property, but only if
 * we are currently deserializing an AE2 model.
 */
@Mixin(ModelElementFace.Deserializer.class)
public class ModelElementFaceDeserializerMixin {

    @Inject(method = "deserialize", at = @At("RETURN"), cancellable = true, allow = 1, remap = false)
    public void onDeserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext,
            CallbackInfoReturnable<ModelElementFace> cri) {
        if (!UnlitQuadHooks.isUnlitExtensionEnabled()) {
            return; // Not in a model that activated the deserializer
        }

        ModelElementFace modelElement = cri.getReturnValue();
        cri.setReturnValue(UnlitQuadHooks.enhanceModelElementFace(modelElement, jsonElement));
    }

}
