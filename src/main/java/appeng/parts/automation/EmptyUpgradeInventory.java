package appeng.parts.automation;

import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;

public final class EmptyUpgradeInventory implements IUpgradeInventory {
    public static final EmptyUpgradeInventory INSTANCE = new EmptyUpgradeInventory();

    @Override
    public int getInstalledUpgrades(Upgrades u) {
        return 0;
    }

    @Override
    public int getMaxInstalled(Upgrades u) {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public IItemHandler toItemHandler() {
        return EmptyHandler.INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
    }

    @Nonnull
    @Override
    public Iterator<ItemStack> iterator() {
        return Collections.emptyIterator();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }
}
