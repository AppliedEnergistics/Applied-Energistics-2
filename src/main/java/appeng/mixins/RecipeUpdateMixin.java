package appeng.mixins;

import appeng.crafting.pattern.ClientPatternCache;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class RecipeUpdateMixin {
    @Inject(method = "handleUpdateRecipes", at = @At("TAIL"))
    private void onHandleUpdateRecipes(CallbackInfo ci) {
        ClientPatternCache.clearCache();
    }
}
