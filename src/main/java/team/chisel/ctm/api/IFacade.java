package team.chisel.ctm.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * To be implemented on blocks that "hide" another block inside, so connected textures can still be accomplished.
 */
public interface IFacade {

    /**
     * @deprecated Use {@link #getFacade(BlockGetter, BlockPos, Direction, BlockPos)}
     */
    @Nonnull
    @Deprecated
    BlockState getFacade(@Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nullable Direction side);

    /**
     * Gets the blockstate this facade appears as.
     *
     * @param level      {@link Level}
     * @param pos        The Blocks position
     * @param side       The side being rendered, NOT the side being connected from.
     *                   <p>
     *                   This value can be null if no side is specified. Please handle this appropriately.
     * @param connection The position of the block being connected to.
     * @return The blockstate which your block appears as.
     */
    @Nonnull
    default BlockState getFacade(@Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nullable Direction side,
                                 @Nonnull BlockPos connection) {
        return getFacade(level, pos, side);
    }

}
