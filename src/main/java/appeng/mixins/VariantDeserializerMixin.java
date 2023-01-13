package appeng.mixins;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.GsonHelper;

import appeng.hooks.BlockstateDefinitionHook;

@Mixin(Variant.Deserializer.class)
public class VariantDeserializerMixin {
    @Inject(method = "deserialize", at = @At("RETURN"), cancellable = true)
    public void addAdditionalRotationOptions(JsonElement json, Type type, JsonDeserializationContext context,
            CallbackInfoReturnable<Variant> cri) {
        var variant = cri.getReturnValue();

        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("ae2:z")) {
            var xRot = GsonHelper.getAsInt(jsonObject, "x", 0);
            var yRot = GsonHelper.getAsInt(jsonObject, "y", 0);
            var zRot = GsonHelper.getAsInt(jsonObject, "ae2:z", 0);
            cri.setReturnValue(BlockstateDefinitionHook.rotateVariant(variant, xRot, yRot, zRot));
        }
    }
}
