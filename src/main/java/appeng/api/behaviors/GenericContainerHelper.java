package appeng.api.behaviors;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.IVariantConversion;

/**
 * Allows custom key types to more easily define and implement custom container item strategies.
 */
@ApiStatus.Experimental
public final class GenericContainerHelper {
    private GenericContainerHelper() {
    }

    @Nullable
    public static <V extends TransferVariant<?>> GenericStack getContainedStack(ItemStack stack,
            ItemApiLookup<Storage<V>, ContainerItemContext> apiLookup, IVariantConversion<V> conversion) {
        if (stack.isEmpty()) {
            return null;
        }

        var content = StorageUtil.findExtractableContent(
                getReadOnlyStorage(stack, apiLookup), null);
        if (content != null) {
            return new GenericStack(
                    conversion.getKey(content.resource()),
                    content.amount());
        } else {
            return null;
        }
    }

    public static <V extends TransferVariant<?>> Storage<V> getReadOnlyStorage(ItemStack stack,
            ItemApiLookup<Storage<V>, ContainerItemContext> apiLookup) {
        if (stack.isEmpty()) {
            return null;
        }

        return ContainerItemContext.withInitial(stack).find(apiLookup);
    }

    /**
     * Tries to extract a specific amount of some transferable resource from an item carried by the player. Validates
     * that the given stack is the actual carried item.
     */
    public static <K extends AEKey, V extends TransferVariant<?>> long extractFromCarried(Player player, K what,
            long amount, ItemStack carried, ItemApiLookup<Storage<V>, ContainerItemContext> apiLookup,
            IVariantConversion<V> conversion) {
        if (player.containerMenu == null || player.containerMenu.getCarried() != carried) {
            return 0;
        }

        var context = ContainerItemContext.ofPlayerCursor(player, player.containerMenu);
        return extractFromStorage(what, amount, context, apiLookup, conversion);
    }

    /**
     * Extract a specific amount of some transferable resource from an item that is inside the player's inventory or
     * currently held by the player in a menu, and returns the amount that could be extracted.
     */
    public static <K extends AEKey, V extends TransferVariant<?>> long extractFromPlayerInventory(Player player, K what,
            long amount, ItemStack stack, ItemApiLookup<Storage<V>, ContainerItemContext> apiLookup,
            IVariantConversion<V> conversion) {
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

        return extractFromStorage(what, amount, context, apiLookup, conversion);
    }

    private static <K extends AEKey, V extends TransferVariant<?>> long extractFromStorage(K what, long amount,
            ContainerItemContext context, ItemApiLookup<Storage<V>, ContainerItemContext> apiLookup,
            IVariantConversion<V> conversion) {
        var storage = context.find(apiLookup);
        if (storage == null) {
            return 0;
        }

        try (var tx = Transaction.openOuter()) {
            var extracted = storage.extract(conversion.getVariant(what), amount, tx);
            if (extracted > 0) {
                tx.commit();
            }
            return extracted;
        }
    }
}
