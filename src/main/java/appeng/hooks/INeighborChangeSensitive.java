package appeng.hooks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

/**
 * Replicates a Forge hook which allows non-comparator blocks to react to changes in adjacent blocks that would
 * otherwise only be visible to comparators (especially inventory changes).
 */
public interface INeighborChangeSensitive {

    // This is usually a Forge extension. We replace it using a Mixin.
    void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor);

}
