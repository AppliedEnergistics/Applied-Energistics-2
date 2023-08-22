package appeng.mixins.chunkloading;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import appeng.core.AEConfig;
import appeng.server.services.ChunkLoadingService;

/**
 * Make spawning and random ticks work in force-loaded chunks. Thank you FTB Chunks for mixin inspiration.
 */
@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Shadow
    @Final
    ServerLevel level;

    @Inject(at = @At("RETURN"), method = "anyPlayerCloseEnoughForSpawning", cancellable = true)
    private void spatialAnchorEnableRandomTicks(ChunkPos pos, CallbackInfoReturnable<Boolean> ci) {
        if (AEConfig.instance().isSpatialAnchorEnablesRandomTicks()) {
            if (!ci.getReturnValue() && ChunkLoadingService.getInstance().isChunkForced(level, pos.x, pos.z)) {
                ci.setReturnValue(true);
            }
        }
    }
}
