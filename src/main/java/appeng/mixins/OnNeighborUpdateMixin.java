package appeng.mixins;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import appeng.hooks.INeighborChangeSensitive;

/**
 * Replicates Forge's callback for non-comparators to get neighboring block changes similar to a comparator.
 */
@Mixin(Level.class)
public abstract class OnNeighborUpdateMixin {

    /**
     * This targets the first getBlockState in the method and injects right after. We want to capture the return value,
     * essentially so that we do not have to get the blockstate again.
     */
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE_ASSIGN", ordinal = 0, target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void triggerOnNeighborChange(BlockPos srcPos, Block srcBlock, CallbackInfo ci,
            // Iterator over all directions on the horizontal plane
            Iterator<Direction> it,
            // Current direction of iterator it
            Direction direction,
            // Position derived from srcPos using direction
            BlockPos blockPos,
            // The block state @ blockPos
            BlockState blockState) {
        Level world = (Level) (Object) this;
        Block block = blockState.getBlock();
        if (block instanceof INeighborChangeSensitive) {
            ((INeighborChangeSensitive) block).onNeighborChange(blockState, world, blockPos, srcPos);
        }
    }

}
