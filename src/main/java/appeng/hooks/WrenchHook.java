package appeng.hooks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

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

    private WrenchHook() {
    }

    public static void onPlayerUseBlockEvent(PlayerInteractEvent.RightClickBlock event) {
        if (event.getUseBlock() == Event.Result.DENY) {
            // See https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/6900
            return;
        }
        var result = onPlayerUseBlock(event.getEntity(), event.getLevel(), event.getHand(), event.getHitVec());
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
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
                        hitResult,
                        itemStack);
            }
        } else if (!InteractionUtil.isInAlternateUseMode(player) && InteractionUtil.canWrenchRotate(itemStack)) {
            var pos = hitResult.getBlockPos();
            var state = level.getBlockState(pos);
            var strategy = IOrientationStrategy.get(state);

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

        return InteractionResult.PASS;
    }

}
