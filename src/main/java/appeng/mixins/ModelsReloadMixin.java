package appeng.mixins;

import java.util.Map;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import appeng.bootstrap.ModelsReloadCallback;

@Mixin(ModelBakery.class)
public class ModelsReloadMixin {

    @Inject(method = "getBakedModelMap", at = @At("RETURN"))
    public void onGetBakedModelMap(CallbackInfoReturnable<Map<ResourceLocation, IBakedModel>> ci) {
        ModelsReloadCallback.EVENT.invoker().onModelsReloaded(ci.getReturnValue());
    }

}
