package appeng.block.quartz;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;

import appeng.core.definitions.AEBlocks;

public class BuddingCertusQuartzBlock extends CertusQuartzBlock {
    public static final int GROWTH_CHANCE = 5;
    public static final int DECAY_CHANCE = 12;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingCertusQuartzBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(GROWTH_CHANCE) != 0) {
            return;
        }

        // Try to grow cluster
        Direction direction = DIRECTIONS[randomSource.nextInt(DIRECTIONS.length)];
        BlockPos targetPos = pos.relative(direction);
        BlockState targetState = level.getBlockState(targetPos);
        Block newCluster = null;
        if (canClusterGrowAtState(targetState)) {
            newCluster = AEBlocks.SMALL_QUARTZ_BUD.block();
        } else if (targetState.is(AEBlocks.SMALL_QUARTZ_BUD.block())
                && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = AEBlocks.MEDIUM_QUARTZ_BUD.block();
        } else if (targetState.is(AEBlocks.MEDIUM_QUARTZ_BUD.block())
                && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = AEBlocks.LARGE_QUARTZ_BUD.block();
        } else if (targetState.is(AEBlocks.LARGE_QUARTZ_BUD.block())
                && targetState.getValue(AmethystClusterBlock.FACING) == direction) {
            newCluster = AEBlocks.QUARTZ_CLUSTER.block();
        }

        if (newCluster == null) {
            return;
        }

        // Grow certus crystal
        BlockState newClusterState = newCluster.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, targetState.getFluidState().getType() == Fluids.WATER);
        level.setBlockAndUpdate(targetPos, newClusterState);

        // Damage the budding certus block after a successful growth
        if (this == AEBlocks.FLAWLESS_BUDDING_QUARTZ.block() || randomSource.nextInt(DECAY_CHANCE) != 0) {
            return;
        }
        Block newBlock;
        if (this == AEBlocks.FLAWED_BUDDING_QUARTZ.block()) {
            newBlock = AEBlocks.CHIPPED_BUDDING_QUARTZ.block();
        } else if (this == AEBlocks.CHIPPED_BUDDING_QUARTZ.block()) {
            newBlock = AEBlocks.DAMAGED_BUDDING_QUARTZ.block();
        } else if (this == AEBlocks.DAMAGED_BUDDING_QUARTZ.block()) {
            newBlock = AEBlocks.QUARTZ_BLOCK.block();
        } else {
            throw new IllegalStateException("Unexpected block: " + this);
        }
        level.setBlockAndUpdate(pos, newBlock.defaultBlockState());
    }

    public static boolean canClusterGrowAtState(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8;
    }
}
