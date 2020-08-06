package appeng.mixins.unlitquad;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;
import appeng.hooks.UnlitQuadHooks;

/**
 * The only job of this mixin is to limit the unlit property to models in the
 * appliedenergistics2 namespace.
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Inject(method = "loadModel", at = @At("HEAD"), allow = 1)
    protected void onBeginLoadModel(ResourceLocation location, CallbackInfoReturnable<BlockModel> cri)
            throws IOException {
        UnlitQuadHooks.setIsDeserializingEnhancedModel(location.getNamespace().equals(AppEng.MOD_ID));
    }

    @Inject(method = "loadModel", at = @At("RETURN"))
    protected void onEndLoadModel(ResourceLocation location, CallbackInfoReturnable<BlockModel> cri) {
        UnlitQuadHooks.setIsDeserializingEnhancedModel(false);
    }

}
