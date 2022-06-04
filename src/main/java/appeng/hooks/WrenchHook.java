package appeng.hooks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

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
        var result = onPlayerUseBlock(event.getPlayer(), event.getWorld(), event.getHand(), event.getHitVec());
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
                        hitResult);
            }
        } else if (!InteractionUtil.isInAlternateUseMode(player) && InteractionUtil.canWrenchRotate(itemStack)) {
            var be = level.getBlockEntity(hitResult.getBlockPos());
            if (be instanceof AEBaseBlockEntity baseBlockEntity) {
                if (!Platform.hasPermissions(new DimensionalBlockPos(level, hitResult.getBlockPos()), player)) {
                    return InteractionResult.FAIL;
                }

                return baseBlockEntity.rotateWithWrench(
                        player,
                        level,
                        hitResult);
            }
        }

        return InteractionResult.PASS;
    }

}
