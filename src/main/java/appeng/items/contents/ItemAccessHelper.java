package appeng.items.contents;

import java.util.function.Function;

import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ItemAccessHelper {
    public static void modify(ItemAccess access, Function<ItemResource, ItemResource> transform) {
        try (var tr = Transaction.openRoot()) {
            access.exchange(transform.apply(access.getResource()), access.getAmount(), tr);
            tr.commit();
        }
    }
}
