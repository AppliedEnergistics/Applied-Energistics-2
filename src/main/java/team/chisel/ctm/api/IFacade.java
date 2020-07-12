package team.chisel.ctm.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * To be implemented on blocks that "hide" another block inside, so connected
 * textures can still be accomplished.
 */
public interface IFacade {

    /**
     * @deprecated Use
     *             {@link #getFacade(IBlockReader, BlockPos, Direction, BlockPos)}
     */
    @Nonnull
    @Deprecated
    BlockState getFacade(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nullable Direction side);

    /**
     * Gets the blockstate this facade appears as.
     *
     * @param world      {@link World}
     * @param pos        The Blocks position
     * @param side       The side being rendered, NOT the side being connected from.
     *                   <p>
     *                   This value can be null if no side is specified. Please
     *                   handle this appropriately.
     * @param connection The position of the block being connected to.
     * @return The blockstate which your block appears as.
     */
    @Nonnull
    default BlockState getFacade(@Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nullable Direction side,
            @Nonnull BlockPos connection) {
        return getFacade(world, pos, side);
    }

}
