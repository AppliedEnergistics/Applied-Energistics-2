package appeng.items.contents;

import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;

import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;

final class CellStorageSupplier implements Supplier<MEStorage> {
    private final Supplier<ItemStack> stackSupplier;
    private MEStorage currentStorage;
    private ItemStack currentStack;

    public CellStorageSupplier(Supplier<ItemStack> stackSupplier) {
        this.stackSupplier = stackSupplier;
    }

    @Override
    public MEStorage get() {
        var stack = stackSupplier.get();
        if (stack != currentStack) {
            currentStorage = StorageCells.getCellInventory(stack, null);
            currentStack = stack;
        }
        return currentStorage;
    }
}
