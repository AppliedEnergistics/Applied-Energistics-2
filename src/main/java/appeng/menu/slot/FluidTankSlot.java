package appeng.menu.slot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEFluidKey;
import appeng.core.localization.GuiText;
import appeng.util.ConfigMenuInventory;
import appeng.util.Platform;

/**
 * Renders a fluid tank instead of a simple slot.
 */
public class FluidTankSlot extends ResizableSlot {
    private final long capacity;
    private final ConfigMenuInventory inv;
    private final Consumer<ItemStack> overflowConsumer;

    public FluidTankSlot(ConfigMenuInventory inv, int invSlot, long capacity, String styleId,
            Consumer<ItemStack> overflowConsumer) {
        super(inv, invSlot, styleId);
        this.capacity = capacity;
        this.inv = inv;
        this.overflowConsumer = overflowConsumer;
    }

    public long getCapacity() {
        return capacity;
    }

    /**
     * Implements emptying fluid containers into slots.
     */
    @Override
    public ItemStack safeInsert(ItemStack stack, int count) {
        var temp = new SimpleContainer(Math.max(stack.getCount(), count) + 1);
        temp.setItem(0, stack);
        var tempStorage = InventoryStorage.of(temp, null);
        var context = new ContainerItemContext() {
            @Override
            public SingleSlotStorage<ItemVariant> getMainSlot() {
                return tempStorage.getSlot(0);
            }

            @Override
            public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
                return tempStorage.insert(itemVariant, maxAmount, transactionContext);
            }

            @Override
            public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
                return tempStorage.getSlots();
            }
        };
        var fluidStorage = FluidStorage.ITEM.find(stack, context);
        if (fluidStorage == null) {
            return stack;
        }

        var realInv = inv.getDelegate();

        try (var tx = Transaction.openOuter()) {
            for (var view : fluidStorage.iterable(tx)) {
                if (!view.isResourceBlank()) {
                    var what = AEFluidKey.of(view.getResource());

                    // Check how much we can insert
                    var amt = realInv.insert(slot, what, view.getAmount(), Actionable.SIMULATE);
                    if (amt <= 0) {
                        continue;
                    }

                    amt = view.extract(view.getResource(), amt, tx);
                    if (amt > 0) {
                        realInv.insert(slot, what, amt, Actionable.MODULATE);
                        tx.commit();
                        break;
                    }
                }
            }
        }

        // Drop anything past slot 0 into the player inventory
        for (int i = 1; i < temp.getContainerSize(); i++) {
            var overflow = temp.getItem(i);
            if (!overflow.isEmpty()) {
                this.overflowConsumer.accept(overflow);
            }
        }

        return temp.getItem(0);
    }

    @Nullable
    @Override
    public List<Component> getCustomTooltip(Function<ItemStack, List<Component>> getItemTooltip,
            ItemStack carriedItem) {
        var current = getItem();
        var itemTooltip = new ArrayList<Component>();
        if (!current.isEmpty()) {
            itemTooltip.addAll(getItemTooltip.apply(current));
            var unwrapped = GenericStack.unwrapItemStack(current);
            if (unwrapped != null) {
                itemTooltip.add(GuiText.TankAmount.text(Platform.formatFluidAmount(unwrapped.amount())));
            }
        }

        itemTooltip.add(GuiText.TankCapacity.text(Platform.formatFluidAmount(capacity)));
        return itemTooltip;
    }
}
