package appeng.api.movable;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The default strategy for moving block entities in/out of spatial storage. Can be extended to create custom logic that
 * runs after {@link #completeMove} or prevents moving specific entities in {@link #beginMove} by returning null.
 * <p/>
 * The default strategy uses {@link BlockEntity#save(CompoundTag)} in {@link #beginMove} to persist the block entity
 * data before it is removed, and then creates a new block entity at the target position using
 * {@link BlockEntity#loadStatic(BlockPos, BlockState, CompoundTag)} in {@link #completeMove}.
 */
public abstract class DefaultBlockEntityMoveStrategy implements IBlockEntityMoveStrategy {

    @Nullable
    @Override
    public CompoundTag beginMove(BlockEntity blockEntity) {
        return blockEntity.save(new CompoundTag());
    }

    @Override
    public boolean completeMove(BlockEntity blockEntity, CompoundTag savedData, Level newLevel, BlockPos newPosition) {
        var be = BlockEntity.loadStatic(newPosition, blockEntity.getBlockState(), savedData);
        if (be != null) {
            newLevel.setBlockEntity(be);
            return true;
        } else {
            return false;
        }
    }

}
