package appeng.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;

import appeng.crafting.pattern.ClientPatternCache;

@Mixin(ClientPacketListener.class)
public class RecipeUpdateMixin {
    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void onHandleUpdateRecipes(CallbackInfo ci) {
        ClientPatternCache.clearCache();
    }
}
