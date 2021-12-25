package appeng.helpers;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Inventory;
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

    public static boolean extractFromPlayerCursorSlot(Player player, AEFluidKey key, long amount) {
        try (Transaction tx = Transaction.openOuter()) {
            long extracted = ContainerItemContext.ofPlayerCursor(player, player.containerMenu).find(FluidStorage.ITEM)
                    .extract(
                            key.toVariant(), amount, tx);
            if (extracted == amount) {
                tx.commit();
                return true;
            }
            return false;
        }
    }

    public static boolean extractFromPlayerStack(Player player, AEFluidKey key, long amount, ItemStack stack) {
        try (Transaction tx = Transaction.openOuter()) {
            Inventory inventory = player.getInventory();
            ContainerItemContext context = null;

            for (int i = 0; i < inventory.getContainerSize(); ++i) {
                if (inventory.getItem(i) == stack) {
                    InventoryStorage wrapper = PlayerInventoryStorage.of(inventory);
                    context = ContainerItemContext.ofPlayerSlot(player, wrapper.getSlots().get(i));
                    break;
                }
            }

            if (context != null) {
                boolean ok = context.find(FluidStorage.ITEM).extract(
                        key.toVariant(), amount, tx) == amount;
                if (ok) {
                    tx.commit();
                    return true;
                }

            }
            return false;
        }
    }
}
