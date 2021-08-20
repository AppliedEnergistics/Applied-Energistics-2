package appeng.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;

import appeng.hooks.ModelsReloadCallback;

@Mixin(ModelBakery.class)
public class ModelsReloadMixin {

    @Inject(method = "getBakedTopLevelModels", at = @At("RETURN"))
    public void onGetBakedModelMap(CallbackInfoReturnable<Map<ResourceLocation, BakedModel>> ci) {
        ModelsReloadCallback.EVENT.invoker().onModelsReloaded(ci.getReturnValue());
    }

}
