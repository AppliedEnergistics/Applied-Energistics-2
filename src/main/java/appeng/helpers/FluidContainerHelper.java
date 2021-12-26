package appeng.helpers;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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

        var content = StorageUtil.findExtractableContent(
                getReadOnlyStorage(stack), null);
        if (content != null) {
            return new GenericStack(
                    AEFluidKey.of(content.resource()),
                    content.amount());
        } else {
            return null;
        }
    }

    public static Storage<FluidVariant> getReadOnlyStorage(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return ContainerItemContext.withInitial(stack).find(FluidStorage.ITEM);
    }

    /**
     * Tries to extract a specific amount of fluid from the item carried by the player. Validates that the given stack
     * is the actual carried item.
     */
    public static long extractFromCarried(Player player, AEFluidKey what, long amount, ItemStack carried) {
        if (player.containerMenu == null || player.containerMenu.getCarried() != carried) {
            return 0;
        }

        var context = ContainerItemContext.ofPlayerCursor(player, player.containerMenu);
        return extractFromStorage(what, amount, context);
    }

    /**
     * Extract a specific amount of Fluid from an item that is inside the player's inventory or currently held by the
     * player in a menu, and returns the amount that could be extracted.
     */
    public static long extractFromPlayerInventory(Player player, AEFluidKey what, long amount, ItemStack stack) {
        // Find the item inside the inventory and create a context for it
        var inventory = player.getInventory();
        ContainerItemContext context = null;

        var playerInv = PlayerInventoryStorage.of(inventory);
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            if (inventory.getItem(i) == stack) {
                context = ContainerItemContext.ofPlayerSlot(player, playerInv.getSlots().get(i));
                break;
            }
        }

        if (context == null) {
            return 0; // Item not found
        }

        return extractFromStorage(what, amount, context);
    }

    private static long extractFromStorage(AEFluidKey what, long amount, ContainerItemContext context) {
        var storage = context.find(FluidStorage.ITEM);
        if (storage == null) {
            return 0;
        }

        try (var tx = Transaction.openOuter()) {
            var extracted = storage.extract(what.toVariant(), amount, tx);
            if (extracted > 0) {
                tx.commit();
            }
            return extracted;
        }
    }
}
