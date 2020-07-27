package appeng.hooks;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

/**
 * This hooks allows item-specific behavior to be triggered when an item is used
 * on a block, without shift being held or the block being called first.
 */
public class ToolItemHook {

    public static void install() {
        UseBlockCallback.EVENT.register(ToolItemHook::handleItemUse);
    }

    private static ActionResult handleItemUse(PlayerEntity playerEntity, World world, Hand hand,
            BlockHitResult blockHitResult) {

        if (playerEntity.isSpectator()) {
            return ActionResult.PASS;
        }

        ItemStack itemStack = playerEntity.getStackInHand(hand);
        Item item = itemStack.getItem();
        if (item instanceof AEToolItem) {
            ItemUsageContext context = new ItemUsageContext(playerEntity, hand, blockHitResult);
            AEToolItem toolItem = (AEToolItem) item;
            return toolItem.onItemUseFirst(itemStack, context);
        }

        return ActionResult.PASS;
    }

}
