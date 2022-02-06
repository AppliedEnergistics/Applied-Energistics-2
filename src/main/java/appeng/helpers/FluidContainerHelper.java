package appeng.helpers;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.primitives.Ints;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;

public final class FluidContainerHelper {
    private FluidContainerHelper() {
    }

    @Nullable
    public static GenericStack getContainedStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        var content = FluidUtil.getFluidContained(stack).orElse(null);
        if (content != null) {
            return GenericStack.fromFluidStack(content);
        } else {
            return null;
        }
    }

    /**
     * Tries to extract a specific amount of fluid from the item carried by the player. Validates that the given stack
     * is the actual carried item.
     */
    public static long extractFromCarried(Player player, AEFluidKey what, long amount, ItemStack carried) {
        if (player.containerMenu == null || player.containerMenu.getCarried() != carried) {
            return 0;
        }

        return extractFromStorage(
                what,
                amount,
                carried,
                player.containerMenu::setCarried,
                player.getInventory()::placeItemBackInInventory);
    }

    /**
     * Extract a specific amount of Fluid from an item that is inside the player's inventory or currently held by the
     * player in a menu, and returns the amount that could be extracted.
     */
    public static long extractFromPlayerInventory(Player player, AEFluidKey what, long amount, ItemStack stack) {
        // Find the item inside the inventory and create a context for it
        var inventory = player.getInventory();
        var invIndex = -1;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            if (inventory.getItem(i) == stack) {
                invIndex = i;
                break;
            }
        }

        if (invIndex == -1) {
            return 0; // Item not found
        }

        var storeInvIndex = invIndex;

        return extractFromStorage(
                what,
                amount,
                stack,
                newStack -> inventory.setItem(storeInvIndex, newStack),
                inventory::placeItemBackInInventory);
    }

    private static long extractFromStorage(AEFluidKey what,
            long amount,
            ItemStack stack,
            Consumer<ItemStack> updateContainer,
            Consumer<ItemStack> addOverflow) {

        if (stack.getCount() > 1) {
            // TODO: Do what Fabric does and place overflow into inventory
            return 0;
        }

        var handler = FluidUtil.getFluidHandler(stack).orElse(null);
        if (handler == null) {
            return 0;
        }

        var extracted = handler.drain(what.toStack(Ints.saturatedCast(amount)), IFluidHandler.FluidAction.EXECUTE);
        if (!extracted.isEmpty()) {
            updateContainer.accept(handler.getContainer());
        }

        return extracted.getAmount();
    }
}
