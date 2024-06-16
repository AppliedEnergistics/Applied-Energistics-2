package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import appeng.hooks.BuiltInModelHooks;

/**
 * Replicates the part of the Fabric API for adding built-in models that we actually use.
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {
    @Inject(at = @At("HEAD"), method = "getModel", cancellable = true)
    private void getModelHook(ResourceLocation id, CallbackInfoReturnable<UnbakedModel> cir) {
        var model = BuiltInModelHooks.getBuiltInModel(id);

        if (model != null) {
            cir.setReturnValue(model);
        }
    }
}
