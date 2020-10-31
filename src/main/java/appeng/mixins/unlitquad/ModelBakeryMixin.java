package appeng.mixins.unlitquad;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.ResourceLocation;

import appeng.hooks.UnlitQuadHooks;

/**
 * The only job of this mixin is to only enable the unlit extensions if the model is whitelisted for it, which is
 * decided in {@link UnlitQuadHooks}.
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Inject(method = "loadModel", at = @At("HEAD"), allow = 1)
    protected void onBeginLoadModel(ResourceLocation location, CallbackInfoReturnable<BlockModel> cri)
            throws IOException {
        UnlitQuadHooks.beginDeserializingModel(location);
    }

    @Inject(method = "loadModel", at = @At("RETURN"))
    protected void onEndLoadModel(ResourceLocation location, CallbackInfoReturnable<BlockModel> cri) {
        UnlitQuadHooks.endDeserializingModel();
    }

}
