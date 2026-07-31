package appeng.items.contents;

import java.util.function.Function;
import java.util.function.Supplier;

import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.NullMarked;

/**
 * A supplier that can cache its resulting value as long as the Resource in an ItemAccess is the same.
 */
@NullMarked
public class AccessDependentSupplier<T> implements Supplier<T> {
    protected final ItemAccess access;
    protected final Function<ItemAccess, T> transform;
    protected ItemResource currentResource = ItemResource.EMPTY;
    protected T currentValue;

    public AccessDependentSupplier(ItemAccess access, Function<ItemAccess, T> transform) {
        this.access = access;
        this.transform = transform;
        currentValue = transform.apply(access);
    }

    @Override
    public T get() {
        var resource = access.getResource();
        if (!resource.equals(currentResource)) {
            currentValue = transform.apply(access);
            currentResource = resource;
        }
        return currentValue;
    }
}
