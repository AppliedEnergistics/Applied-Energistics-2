package appeng.mixins.unlitquad;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;

import appeng.hooks.UnlitQuadHooks;

/**
 * The only job of this mixin is to only enable the unlit extensions if the model is whitelisted for it, which is
 * decided in {@link UnlitQuadHooks}.
 */
@Mixin(ModelLoader.class)
public class ModelLoaderMixin {

    @Inject(method = "loadModelFromJson", at = @At("HEAD"), allow = 1)
    protected void onBeginLoadModel(Identifier location, CallbackInfoReturnable<JsonUnbakedModel> cri)
            throws IOException {
        UnlitQuadHooks.beginDeserializingModel(location);
    }

    @Inject(method = "loadModelFromJson", at = @At("RETURN"))
    protected void onEndLoadModel(Identifier location, CallbackInfoReturnable<JsonUnbakedModel> cri) {
        UnlitQuadHooks.endDeserializingModel();
    }

}
