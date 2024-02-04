package appeng.items.contents;

import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A supplier that can cache its resulting value as long as the itemstack returned
 * by an original supplier is the same.
 */
public final class StackDependentSupplier<T> implements Supplier<T> {
    private final Supplier<ItemStack> stackSupplier;
    private final Function<ItemStack, T> transform;

    private ItemStack currentStack;
    private T currentValue;

    public StackDependentSupplier(Supplier<ItemStack> stackSupplier, Function<ItemStack, T> transform) {
        this.stackSupplier = stackSupplier;
        this.transform = transform;
    }

    @Override
    public T get() {
        var stack = stackSupplier.get();
        if (currentStack != stack) {
            currentValue = transform.apply(stack);
            currentStack = stack;
        }
        return currentValue;
    }
}
