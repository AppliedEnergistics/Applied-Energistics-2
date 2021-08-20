package appeng.hooks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Replicates a Forge hook which allows non-comparator blocks to react to changes in adjacent blocks that would
 * otherwise only be visible to comparators (especially inventory changes).
 */
public interface INeighborChangeSensitive {

    // This is usually a Forge extension. We replace it using a Mixin.
    void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor);

}
