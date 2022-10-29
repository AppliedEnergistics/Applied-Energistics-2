package appeng.menu.me.interaction;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import appeng.api.behaviors.ContainerItemStrategy;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.util.GenericContainerHelper;
import appeng.util.fluid.FluidSoundHelper;

public class FluidContainerItemStrategy implements ContainerItemStrategy<AEFluidKey, Storage<FluidVariant>> {
    @Override
    public @Nullable GenericStack getContainedStack(ItemStack stack) {
        return GenericContainerHelper.getContainedFluidStack(stack);
    }

    @Override
    public @Nullable Storage<FluidVariant> findCarriedContext(Player player, AbstractContainerMenu menu) {
        return ContainerItemContext.ofPlayerCursor(player, menu).find(FluidStorage.ITEM);
    }

    @Override
    public @Nullable Storage<FluidVariant> findPlayerSlotContext(Player player, int slot) {
        var playerInv = PlayerInventoryStorage.of(player.getInventory());
        return ContainerItemContext.ofPlayerSlot(player, playerInv.getSlots().get(slot)).find(FluidStorage.ITEM);
    }

    @Override
    public long extract(Storage<FluidVariant> context, AEFluidKey what, long amount, Actionable mode) {
        try (var tx = Transaction.openOuter()) {
            var extracted = context.extract(what.toVariant(), amount, tx);
            if (mode == Actionable.MODULATE) {
                tx.commit();
            }
            return extracted;
        }
    }

    @Override
    public long insert(Storage<FluidVariant> context, AEFluidKey what, long amount, Actionable mode) {
        try (var tx = Transaction.openOuter()) {
            var inserted = context.insert(what.toVariant(), amount, tx);
            if (mode == Actionable.MODULATE) {
                tx.commit();
            }
            return inserted;
        }
    }

    @Override
    public void playFillSound(Player player, AEFluidKey what) {
        FluidSoundHelper.playFillSound(player, what);
    }

    @Override
    public void playEmptySound(Player player, AEFluidKey what) {
        FluidSoundHelper.playEmptySound(player, what);
    }

    @Override
    public @Nullable GenericStack getExtractableContent(Storage<FluidVariant> context) {
        var resourceAmount = StorageUtil.findExtractableContent(context, null);
        if (resourceAmount == null) {
            return null;
        }
        return new GenericStack(AEFluidKey.of(resourceAmount.resource()), resourceAmount.amount());
    }
}
