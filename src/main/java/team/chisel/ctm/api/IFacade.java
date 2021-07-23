package team.chisel.ctm.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * To be implemented on blocks that "hide" another block inside, so connected textures can still be accomplished.
 */
public interface IFacade {

    /**
     * @deprecated Use {@link #getFacade(BlockGetter, BlockPos, net.minecraft.core.Direction, BlockPos)}
     */
    @Nonnull
    @Deprecated
    net.minecraft.world.level.block.state.BlockState getFacade(@Nonnull BlockGetter world, @Nonnull net.minecraft.core.BlockPos pos, @Nullable net.minecraft.core.Direction side);

    /**
     * Gets the blockstate this facade appears as.
     *
     * @param world      {@link Level}
     * @param pos        The Blocks position
     * @param side       The side being rendered, NOT the side being connected from.
     *                   <p>
     *                   This value can be null if no side is specified. Please handle this appropriately.
     * @param connection The position of the block being connected to.
     * @return The blockstate which your block appears as.
     */
    @Nonnull
    default BlockState getFacade(@Nonnull BlockGetter world, @Nonnull net.minecraft.core.BlockPos pos, @Nullable Direction side,
                                 @Nonnull BlockPos connection) {
        return getFacade(world, pos, side);
    }

}
