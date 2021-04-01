package appeng.mixins.feature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.lighting.WorldLightManager;

/**
 * This fixes a bug in the Minecraft light-update code that runs after world-generation for
 * {@link net.minecraft.world.chunk.ChunkPrimer}. If a chunk-section contains a light-emitting block, and we clear the
 * entire chunk-section (i.e. as part of meteorite worldgen), the lighting-update will assume that the chunk section
 * exists when it runs through {@link WorldLightManager#onBlockEmissionIncrease(net.minecraft.util.math.BlockPos, int)}, even
 * though the light-level is now 0 for the block.
 * <p/>
 * This mixin will cancel the now useless block-update and prevent the crash from occurring.
 * <p/>
 * See: https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/4891
 */
@Mixin(WorldLightManager.class)
public class WorldLightManagerMixin {
    @Inject(method = "onBlockEmissionIncrease", at = @At("HEAD"), cancellable = true)
    public void onBlockEmissionIncrease(BlockPos blockPos, int lightLevel, CallbackInfo ci) {
        if (lightLevel == 0) {
            ci.cancel();
        }
    }
}
