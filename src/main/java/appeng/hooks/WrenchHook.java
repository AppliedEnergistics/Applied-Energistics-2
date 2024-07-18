package appeng.hooks;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.RelativeSide;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

/**
 * This hooks listens for items that match a wrench tag being used on our blocks while shift is held to disassemble, and
 * to rotate if shift is not held.
 */
public final class WrenchHook {

    private static final ThreadLocal<Boolean> IS_DISASSEMBLING = new ThreadLocal<>();

    private WrenchHook() {
    }

    public static boolean isDisassembling() {
        return Boolean.TRUE.equals(IS_DISASSEMBLING.get());
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
                IS_DISASSEMBLING.set(true);
                try {
                    if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitResult.getBlockPos()), player)) {
                        return InteractionResult.FAIL;
                    }

                    var result = baseBlockEntity.disassembleWithWrench(
                            player,
                            level,
                            hitResult,
                            itemStack);
                    if (result.consumesAction()) {
                        SoundEvent soundType = SoundEvents.ITEM_FRAME_REMOVE_ITEM;
                        level.playSound(player, hitResult.getBlockPos(), soundType, SoundSource.BLOCKS, 0.7F, 1.0F);
                    }
                    return result;
                } finally {
                    IS_DISASSEMBLING.remove();
                }
            }
        } else if (!InteractionUtil.isInAlternateUseMode(player) && InteractionUtil.canWrenchRotate(itemStack)) {
            var pos = hitResult.getBlockPos();
            var state = level.getBlockState(pos);
            var strategy = IOrientationStrategy.get(state);
            if (strategy.allowsPlayerRotation()) {
                var clickedFace = hitResult.getDirection();
                var orientation = BlockOrientation.get(strategy, state);
                orientation = orientation.rotateClockwiseAround(clickedFace);
                var newState = strategy.setOrientation(state, orientation.getSide(RelativeSide.FRONT),
                        orientation.getSpin());

                if (newState != state && newState.canSurvive(level, pos)) {
                    if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitResult.getBlockPos()), player)) {
                        return InteractionResult.FAIL;
                    }

                    level.setBlockAndUpdate(pos, newState);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        return InteractionResult.PASS;
    }

}
