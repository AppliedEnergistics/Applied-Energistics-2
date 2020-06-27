package appeng.mixins;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ModelLoader.class)
public class ModelsReloadMixin {

    @Inject(method = "getBakedModelMap", at = @At("RETURN"))
    public void onGetBakedModelMap(CallbackInfoReturnable<Map<Identifier, BakedModel>> ci) {
        Map<Identifier, BakedModel> models = ci.getReturnValue();

    }

}
