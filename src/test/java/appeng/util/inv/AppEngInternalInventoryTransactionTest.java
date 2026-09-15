package appeng.util.inv;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;

import appeng.util.BootstrapMinecraft;

/**
 * Tests that verify AppEngInternalInventory properly defers side-effects (onChangeInventory callbacks)
 * until transaction commit when accessed via resource handlers (the transactional API).
 */
@BootstrapMinecraft
@ExtendWith(EphemeralTestServerProvider.class)
class AppEngInternalInventoryTransactionTest {

    public AppEngInternalInventoryTransactionTest(MinecraftServer server) {
    }

    private AppEngInternalInventory createInventory(AtomicInteger changeCounter) {
        return new AppEngInternalInventory(new TestInventoryHost(changeCounter), 3, 64);
    }

    @Test
    void testInsertDefersSideEffects() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        var handler = inv.toResourceHandler();
        var resource = ItemResource.of(Items.DIAMOND);

        try (var tx = Transaction.open(null)) {
            var inserted = handler.insert(resource, 1, tx);
            assertThat(inserted).isEqualTo(1);
            assertThat(inv.getStackInSlot(0).getItem()).isEqualTo(Items.DIAMOND);
            assertThat(changeCounter.get()).isEqualTo(0);
            tx.commit();
        }

        assertThat(changeCounter.get()).isEqualTo(1);
    }

    @Test
    void testExtractDefersSideEffects() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        inv.setItemDirect(0, new ItemStack(Items.DIAMOND, 5));
        changeCounter.set(0);

        var handler = inv.toResourceHandler();
        var resource = ItemResource.of(Items.DIAMOND);

        try (var tx = Transaction.open(null)) {
            var extracted = handler.extract(resource, 2, tx);
            assertThat(extracted).isEqualTo(2);
            assertThat(inv.getStackInSlot(0).getCount()).isEqualTo(3);
            assertThat(changeCounter.get()).isEqualTo(0);
            tx.commit();
        }

        assertThat(changeCounter.get()).isEqualTo(1);
    }

    @Test
    void testMultipleChangesInSingleTransaction() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        var handler = inv.toResourceHandler();

        try (var tx = Transaction.open(null)) {
            handler.insert(ItemResource.of(Items.DIAMOND), 1, tx);
            handler.insert(ItemResource.of(Items.EMERALD), 1, tx);
            handler.insert(ItemResource.of(Items.GOLD_INGOT), 1, tx);
            assertThat(changeCounter.get()).isEqualTo(0);
            tx.commit();
        }

        assertThat(changeCounter.get()).isEqualTo(3);
    }

    @Test
    void testNestedTransactionDeferral() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        var handler = inv.toResourceHandler();

        try (var outerTx = Transaction.open(null)) {
            handler.insert(ItemResource.of(Items.DIAMOND), 1, outerTx);
            assertThat(changeCounter.get()).isEqualTo(0);

            try (var innerTx = Transaction.open(outerTx)) {
                handler.insert(ItemResource.of(Items.EMERALD), 1, innerTx);
                assertThat(changeCounter.get()).isEqualTo(0);
                innerTx.commit();
            }

            assertThat(changeCounter.get()).isEqualTo(0);
            outerTx.commit();
        }

        assertThat(changeCounter.get()).isEqualTo(2);
    }

    @Test
    void testRollbackRevertsChanges() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        var handler = inv.toResourceHandler();
        var resource = ItemResource.of(Items.DIAMOND);

        assertThat(inv.getStackInSlot(0).isEmpty()).isTrue();

        try (var tx = Transaction.open(null)) {
            handler.insert(resource, 5, tx);
            assertThat(inv.getStackInSlot(0).getCount()).isEqualTo(5);
        }

        assertThat(inv.getStackInSlot(0).isEmpty()).isTrue();
        assertThat(changeCounter.get()).isEqualTo(0);
    }

    @Test
    void testRollbackAfterMultipleChanges() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        var handler = inv.toResourceHandler();

        try (var tx = Transaction.open(null)) {
            handler.insert(ItemResource.of(Items.DIAMOND), 1, tx);
            handler.insert(ItemResource.of(Items.EMERALD), 2, tx);
            handler.insert(ItemResource.of(Items.GOLD_INGOT), 3, tx);
            assertThat(inv.getStackInSlot(0).getCount()).isEqualTo(1);
            assertThat(inv.getStackInSlot(1).getCount()).isEqualTo(2);
            assertThat(inv.getStackInSlot(2).getCount()).isEqualTo(3);
        }

        assertThat(inv.getStackInSlot(0).isEmpty()).isTrue();
        assertThat(inv.getStackInSlot(1).isEmpty()).isTrue();
        assertThat(inv.getStackInSlot(2).isEmpty()).isTrue();
        assertThat(changeCounter.get()).isEqualTo(0);
    }

    @Test
    void testNestedRollbackDoesntAffectOuter() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        var handler = inv.toResourceHandler();

        try (var outerTx = Transaction.open(null)) {
            handler.insert(ItemResource.of(Items.DIAMOND), 1, outerTx);

            try (var innerTx = Transaction.open(outerTx)) {
                handler.insert(ItemResource.of(Items.EMERALD), 2, innerTx);
            }

            assertThat(inv.getStackInSlot(0).getCount()).isEqualTo(1);
            assertThat(inv.getStackInSlot(1).isEmpty()).isTrue();
            outerTx.commit();
        }

        assertThat(changeCounter.get()).isEqualTo(1);
        assertThat(inv.getStackInSlot(0).getCount()).isEqualTo(1);
        assertThat(inv.getStackInSlot(1).isEmpty()).isTrue();
    }

    @Test
    void testSetItemDirectTriggersImmediateSideEffect() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        inv.setItemDirect(0, new ItemStack(Items.DIAMOND));
        assertThat(changeCounter.get()).isEqualTo(1);
    }

    @Test
    void testInsertItemTriggersImmediateSideEffect() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        inv.insertItem(0, new ItemStack(Items.DIAMOND), false);
        assertThat(changeCounter.get()).isEqualTo(1);
    }

    @Test
    void testExtractItemTriggersImmediateSideEffect() {
        var changeCounter = new AtomicInteger(0);
        var inv = createInventory(changeCounter);
        inv.setItemDirect(0, new ItemStack(Items.DIAMOND, 5));
        changeCounter.set(0);
        inv.extractItem(0, 2, false);
        assertThat(changeCounter.get()).isEqualTo(1);
    }

    /**
     * Test host that counts onChangeInventory calls
     */
    private static class TestInventoryHost implements InternalInventoryHost {
        private final AtomicInteger changeCounter;

        TestInventoryHost(AtomicInteger changeCounter) {
            this.changeCounter = changeCounter;
        }

        @Override
        public void saveChangedInventory(AppEngInternalInventory inv) {
            // Not testing this
        }

        @Override
        public void onChangeInventory(AppEngInternalInventory inv, int slot) {
            // Increment counter - this is the side-effect we're testing
            changeCounter.incrementAndGet();
        }

        @Override
        public boolean isClientSide() {
            return false;
        }
    }
}
