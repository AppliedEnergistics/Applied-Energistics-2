package appeng.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

import appeng.init.client.InitAutoRotatingModel;

@Mixin(ModelManager.class)
public class ModelsReloadMixin {
    @Inject(method = "loadModels(Lnet/minecraft/util/profiling/ProfilerFiller;Ljava/util/Map;Lnet/minecraft/client/resources/model/ModelBakery;)Lnet/minecraft/client/resources/model/ModelManager$ReloadState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakeModels(Ljava/util/function/BiFunction;)V", shift = At.Shift.AFTER))
    public void onGetBakedModelMap(ProfilerFiller profilerFiller, Map<ResourceLocation, AtlasSet.StitchResult> map,
            ModelBakery modelBakery, CallbackInfoReturnable<ModelManager.ReloadState> callbackInfo) {
        InitAutoRotatingModel.onModelBake(modelBakery.getBakedTopLevelModels());
    }
}
