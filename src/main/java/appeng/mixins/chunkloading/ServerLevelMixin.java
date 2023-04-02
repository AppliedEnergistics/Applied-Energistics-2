package appeng.mixins.chunkloading;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerLevel;

import appeng.server.services.ChunkLoadingService;

/**
 * Prevent unforcing a chunk if we are still forcing it (e.g. because another chunk loader got removed).
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(at = @At("HEAD"), method = "setChunkForced", cancellable = true)
    public void preventChunkUnforcing(int chunkX, int chunkZ, boolean add, CallbackInfoReturnable<Boolean> cir) {
        if (!add) {
            // What if You wanted to un-forceload a chunk
            if (ChunkLoadingService.getInstance().isChunkForced((ServerLevel) (Object) this, chunkX, chunkZ)) {
                // But god said no
                cir.setReturnValue(false);
            }
        }
    }
}
