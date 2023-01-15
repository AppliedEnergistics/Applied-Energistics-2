package appeng.me.storage;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.primitives.Ints;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.AELog;
import appeng.core.localization.GuiText;

/**
 * Adapts external platform storage to behave like an {@link MEStorage}.
 */
public abstract class ExternalStorageFacade implements MEStorage {
    /**
     * Clamp reported values to avoid overflows when amounts get too close to Long.MAX_VALUE.
     */
    private static final long MAX_REPORTED_AMOUNT = 1L << 42;

    @Nullable
    private Runnable changeListener;

    protected boolean extractableOnly;

    public void setChangeListener(@Nullable Runnable listener) {
        this.changeListener = listener;
    }

    public abstract int getSlots();

    @Nullable
    public abstract GenericStack getStackInSlot(int slot);

    public abstract AEKeyType getKeyType();

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        var inserted = insertExternal(what, Ints.saturatedCast(amount), mode);
        if (inserted > 0 && mode == Actionable.MODULATE) {
            if (this.changeListener != null) {
                this.changeListener.run();
            }
        }
        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        var extracted = extractExternal(what, Ints.saturatedCast(amount), mode);
        if (extracted > 0 && mode == Actionable.MODULATE) {
            if (this.changeListener != null) {
                this.changeListener.run();
            }
        }
        return extracted;
    }

    @Override
    public Component getDescription() {
        return GuiText.ExternalStorage.text(AEKeyType.fluids().getDescription());
    }

    protected abstract int insertExternal(AEKey what, int amount, Actionable mode);

    protected abstract int extractExternal(AEKey what, int amount, Actionable mode);

    public abstract boolean containsAnyFuzzy(Set<AEKey> keys);

    public static ExternalStorageFacade of(IFluidHandler handler) {
        return new FluidHandlerFacade(handler);
    }

    public static ExternalStorageFacade of(IItemHandler handler) {
        return new ItemHandlerFacade(handler);
    }

    public void setExtractableOnly(boolean extractableOnly) {
        this.extractableOnly = extractableOnly;
    }

    private static class ItemHandlerFacade extends ExternalStorageFacade {
        private final IItemHandler handler;

        public ItemHandlerFacade(IItemHandler handler) {
            this.handler = handler;
        }

        @Override
        public int getSlots() {
            return handler.getSlots();
        }

        @Nullable
        @Override
        public GenericStack getStackInSlot(int slot) {
            return GenericStack.fromItemStack(handler.getStackInSlot(slot));
        }

        @Override
        public AEKeyType getKeyType() {
            return AEKeyType.items();
        }

        @Override
        public int insertExternal(AEKey what, int amount, Actionable mode) {
            if (!(what instanceof AEItemKey itemKey)) {
                return 0;
            }

            ItemStack orgInput = itemKey.toStack(Ints.saturatedCast(amount));
            ItemStack remaining = orgInput;

            int slotCount = handler.getSlots();
            boolean simulate = mode == Actionable.SIMULATE;

            // This uses a brute force approach and tries to jam it in every slot the inventory exposes.
            for (int i = 0; i < slotCount && !remaining.isEmpty(); i++) {
                remaining = handler.insertItem(i, remaining, simulate);
            }

            // At this point, we still have some items left...
            if (remaining == orgInput) {
                // The stack remained unmodified, target inventory is full
                return 0;
            }

            return amount - remaining.getCount();
        }

        @Override
        public int extractExternal(AEKey what, int amount, Actionable mode) {
            if (!(what instanceof AEItemKey itemKey)) {
                return 0;
            }

            int remainingSize = Ints.saturatedCast(amount);

            // Use this to gather the requested items
            ItemStack gathered = ItemStack.EMPTY;

            final boolean simulate = mode == Actionable.SIMULATE;

            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stackInInventorySlot = handler.getStackInSlot(i);

                if (!itemKey.matches(stackInInventorySlot)) {
                    continue;
                }

                ItemStack extracted;
                int stackSizeCurrentSlot = stackInInventorySlot.getCount();
                int remainingCurrentSlot = Math.min(remainingSize, stackSizeCurrentSlot);

                // We have to loop here because according to the docs, the handler shouldn't return a stack with
                // size > maxSize, even if we request more. So even if it returns a valid stack, it might have more
                // stuff.
                do {
                    extracted = handler.extractItem(i, remainingCurrentSlot, simulate);
                    if (!extracted.isEmpty()) {
                        // In order to guard against broken IItemHandler implementations, we'll try to guess if the
                        // returned
                        // stack (especially in simulate mode) is the same that was returned by getStackInSlot. This is
                        // obviously not a precise science, but it would catch the previous Forge bug:
                        // https://github.com/MinecraftForge/MinecraftForge/pull/6580
                        if (extracted == stackInInventorySlot) {
                            extracted = extracted.copy();
                        }

                        if (extracted.getCount() > remainingCurrentSlot) {
                            // Something broke. It should never return more than we requested...
                            // We're going to silently eat the remainder
                            AELog.warn(
                                    "Mod that provided item handler %s is broken. Returned %s items while only requesting %d.",
                                    handler.getClass().getName(), extracted.toString(), remainingCurrentSlot);
                            extracted.setCount(remainingCurrentSlot);
                        }

                        // Heuristic for simulation: looping in case of simulations is pointless, since the state of the
                        // underlying inventory does not change after a simulated extraction. To still support
                        // inventories
                        // that report stacks that are larger than maxStackSize, we use this heuristic
                        if (simulate && extracted.getCount() == extracted.getMaxStackSize()
                                && remainingCurrentSlot > extracted.getMaxStackSize()) {
                            extracted.setCount(remainingCurrentSlot);
                        }

                        // We're just gonna use the first stack we get our hands on as the template for the rest.
                        if (gathered.isEmpty()) {
                            gathered = extracted;
                        } else {
                            gathered.grow(extracted.getCount());
                        }
                        remainingCurrentSlot -= extracted.getCount();
                    }
                } while (!simulate && !extracted.isEmpty() && remainingCurrentSlot > 0);

                remainingSize -= stackSizeCurrentSlot - remainingCurrentSlot;

                // Done?
                if (remainingSize <= 0) {
                    break;
                }
            }

            if (!gathered.isEmpty()) {
                return gathered.getCount();
            }

            return 0;
        }

        @Override
        public boolean containsAnyFuzzy(Set<AEKey> keys) {
            for (int i = 0; i < handler.getSlots(); i++) {
                var what = AEItemKey.of(handler.getStackInSlot(i));
                if (what != null) {
                    if (keys.contains(what.dropSecondary())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (int i = 0; i < handler.getSlots(); i++) {
                // Skip resources that cannot be extracted if that filter was enabled
                var stack = handler.getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }

                if (extractableOnly) {
                    if (handler.extractItem(i, 1, true).isEmpty()) {
                        if (handler.extractItem(i, stack.getCount(), true).isEmpty()) {
                            continue;
                        }
                    }
                }

                out.add(AEItemKey.of(stack), stack.getCount());
            }
        }
    }

    private static class FluidHandlerFacade extends ExternalStorageFacade {
        private final IFluidHandler handler;

        public FluidHandlerFacade(IFluidHandler handler) {
            this.handler = handler;
        }

        @Override
        public int getSlots() {
            return handler.getTanks();
        }

        @Nullable
        @Override
        public GenericStack getStackInSlot(int slot) {
            return GenericStack.fromFluidStack(handler.getFluidInTank(slot));
        }

        @Override
        public AEKeyType getKeyType() {
            return AEKeyType.fluids();
        }

        @Override
        protected int insertExternal(AEKey what, int amount, Actionable mode) {
            if (!(what instanceof AEFluidKey fluidKey)) {
                return 0;
            }

            return handler.fill(fluidKey.toStack(amount), mode.getFluidAction());
        }

        @Override
        public int extractExternal(AEKey what, int amount, Actionable mode) {
            if (!(what instanceof AEFluidKey fluidKey)) {
                return 0;
            }

            var fluidStack = fluidKey.toStack(Ints.saturatedCast(amount));

            // Drain the fluid from the tank
            FluidStack gathered = handler.drain(fluidStack, mode.getFluidAction());
            if (gathered.isEmpty()) {
                // If nothing was pulled from the tank, return null
                return 0;
            }

            return gathered.getAmount();
        }

        @Override
        public boolean containsAnyFuzzy(Set<AEKey> keys) {
            for (int i = 0; i < handler.getTanks(); i++) {
                var what = AEFluidKey.of(handler.getFluidInTank(i));
                if (what != null) {
                    if (keys.contains(what.dropSecondary())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (int i = 0; i < handler.getTanks(); i++) {
                // Skip resources that cannot be extracted if that filter was enabled
                var stack = handler.getFluidInTank(i);
                if (stack.isEmpty()) {
                    continue;
                }

                if (extractableOnly) {
                    if (handler.drain(stack, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
                        continue;
                    }
                }

                out.add(AEFluidKey.of(stack), stack.getAmount());
            }
        }
    }
}
