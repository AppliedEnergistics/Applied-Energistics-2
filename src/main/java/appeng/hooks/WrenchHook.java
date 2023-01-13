package appeng.hooks;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.util.DimensionalBlockPos;
import appeng.block.orientation.IOrientationStrategy;
import appeng.block.orientation.RelativeSide;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

/**
 * This hooks listens for items that match a wrench tag being used on our blocks while shift is held to disassemble, and
 * to rotate if shift is not held.
 */
public final class WrenchHook {

    private WrenchHook() {
    }

    public static InteractionResult onPlayerUseBlock(Player player,
            Level level,
            InteractionHand hand,
            BlockHitResult hitResult) {

        // Only handle main hand interactions
        if (player.isSpectator() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        var itemStack = player.getItemInHand(hand);

        if (InteractionUtil.isInAlternateUseMode(player) && InteractionUtil.canWrenchDisassemble(itemStack)) {

            var be = level.getBlockEntity(hitResult.getBlockPos());
            if (be instanceof AEBaseBlockEntity baseBlockEntity) {
                if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitResult.getBlockPos()), player)) {
                    return InteractionResult.FAIL;
                }

                return baseBlockEntity.disassembleWithWrench(
                        player,
                        level,
                        hitResult);
            }
        } else if (!InteractionUtil.isInAlternateUseMode(player) && InteractionUtil.canWrenchRotate(itemStack)) {
            var pos = hitResult.getBlockPos();
            var state = level.getBlockState(pos);
            var strategy = IOrientationStrategy.get(state);

            var clickedFace = hitResult.getDirection();
            var aroundAxis = clickedFace.getAxis();
            var facing = strategy.getFacing(state);
            var up = strategy.getSide(state, RelativeSide.TOP);
            BlockState newState;
            Direction newFacing;
            Direction newUp;
            if (clickedFace.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                newFacing = facing.getClockWise(aroundAxis);
                newUp = up.getClockWise(aroundAxis);
            } else {
                newFacing = facing.getCounterClockWise(aroundAxis);
                newUp = up.getCounterClockWise(aroundAxis);
            }
            newState = strategy.setOrientation(state, newFacing, newUp);

            if (newState != state && newState.canSurvive(level, pos)) {
                if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitResult.getBlockPos()), player)) {
                    return InteractionResult.FAIL;
                }

                level.setBlockAndUpdate(pos, newState);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

}
