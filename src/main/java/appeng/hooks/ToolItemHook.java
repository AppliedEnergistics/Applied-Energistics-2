package appeng.hooks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * This hooks allows item-specific behavior to be triggered when an item is used on a block, without shift being held or
 * the block being called first.
 */
public final class ToolItemHook {

    private ToolItemHook() {
    }

    public static InteractionResult onPlayerUseBlock(Player player, Level level, InteractionHand hand,
            BlockHitResult blockHitResult) {

        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }

        var itemStack = player.getItemInHand(hand);
        var item = itemStack.getItem();
        if (item instanceof AEToolItem toolItem) {
            UseOnContext context = new UseOnContext(player, hand, blockHitResult);
            return toolItem.onItemUseFirst(itemStack, context);
        }

        return InteractionResult.PASS;
    }

}
