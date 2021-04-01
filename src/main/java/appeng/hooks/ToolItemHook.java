package appeng.hooks;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/**
 * This hooks allows item-specific behavior to be triggered when an item is used on a block, without shift being held or
 * the block being called first.
 */
public class ToolItemHook {

    public static void install() {
        UseBlockCallback.EVENT.register(ToolItemHook::handleItemUse);
    }

    private static ActionResultType handleItemUse(PlayerEntity playerEntity, World world, Hand hand,
            BlockRayTraceResult blockHitResult) {

        if (playerEntity.isSpectator()) {
            return ActionResultType.PASS;
        }

        ItemStack itemStack = playerEntity.getHeldItem(hand);
        Item item = itemStack.getItem();
        if (item instanceof AEToolItem) {
            ItemUseContext context = new ItemUseContext(playerEntity, hand, blockHitResult);
            AEToolItem toolItem = (AEToolItem) item;
            return toolItem.onItemUseFirst(itemStack, context);
        }

        return ActionResultType.PASS;
    }

}
