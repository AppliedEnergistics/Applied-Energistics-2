package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import appeng.hooks.BuiltInModelHooks;

/**
 * Replicates the part of the Fabric API for adding built-in models that we actually use.
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {
    @Inject(at = @At("HEAD"), method = "loadModel", cancellable = true)
    private void loadModelHook(ResourceLocation id, CallbackInfo ci) {
        var model = BuiltInModelHooks.getBuiltInModel(id);

        if (model != null) {
            cacheAndQueueDependencies(id, model);
            ci.cancel();
        }
    }

    @Shadow
    protected void cacheAndQueueDependencies(ResourceLocation id, UnbakedModel unbakedModel) {
    }
}
