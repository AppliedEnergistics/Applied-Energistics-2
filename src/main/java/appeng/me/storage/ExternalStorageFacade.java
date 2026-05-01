package appeng.me.storage;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.primitives.Ints;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
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

    public static ExternalStorageFacade ofFluidHandler(ResourceHandler<FluidResource> handler) {
        return new FluidHandlerFacade(handler);
    }

    public static ExternalStorageFacade ofItemHandler(ResourceHandler<ItemResource> handler) {
        return new ItemHandlerFacade(handler);
    }

    public void setExtractableOnly(boolean extractableOnly) {
        this.extractableOnly = extractableOnly;
    }

    private static abstract class ResourceHandlerFacade<R extends Resource, K extends AEKey>
            extends ExternalStorageFacade {
        protected final ResourceHandler<R> handler;

        public ResourceHandlerFacade(ResourceHandler<R> handler) {
            this.handler = handler;
        }

        @Override
        public int getSlots() {
            return handler.size();
        }

        @Nullable
        @Override
        public GenericStack getStackInSlot(int slot) {
            K key = toKey(handler.getResource(slot));
            return key == null ? null : new GenericStack(key, handler.getAmountAsLong(slot));
        }

        @Override
        public int insertExternal(AEKey what, int amount, Actionable mode) {
            var resource = toResource(what);
            if (resource == null) {
                return 0;
            }

            try (var tx = Transaction.openRoot()) {
                var inserted = handler.insert(resource, amount, tx);
                if (!mode.isSimulate()) {
                    tx.commit();
                }
                return inserted;
            }
        }

        @Override
        public int extractExternal(AEKey what, int amount, Actionable mode) {
            var resource = toResource(what);
            if (resource == null) {
                return 0;
            }

            try (var tx = Transaction.openRoot()) {
                var extracted = handler.extract(resource, amount, tx);
                if (!mode.isSimulate()) {
                    tx.commit();
                }
                return extracted;
            }
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (int i = 0; i < handler.size(); i++) {
                // Skip resources that cannot be extracted if that filter was enabled
                var stack = handler.getResource(i);
                if (stack.isEmpty()) {
                    continue;
                }

                long amount = handler.getAmountAsLong(i);

                if (extractableOnly) {
                    // Try to determine whether the resource is extractable

                    try (var tx = Transaction.openRoot()) {
                        var extracted = handler.extract(i, stack, 1, tx);
                        // Try again in case the handler only allows extracting the resource in its entirety (i.e.
                        // cauldrons)
                        if (extracted == 0) {
                            extracted = handler.extract(i, stack, 1, tx);
                        }
                        if (extracted == 0) {
                            continue; // Skip unextractable slots
                        }
                    }
                }

                out.add(toKey(stack), amount);
            }
        }

        @Override
        public boolean containsAnyFuzzy(Set<AEKey> keys) {
            for (int i = 0; i < handler.size(); i++) {
                var what = toKey(handler.getResource(i));
                if (what != null) {
                    if (keys.contains(what.dropSecondary())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Nullable
        protected abstract K toKey(R resource);

        @Nullable
        protected abstract R toResource(AEKey key);
    }

    private static class ItemHandlerFacade extends ResourceHandlerFacade<ItemResource, AEItemKey> {
        public ItemHandlerFacade(ResourceHandler<ItemResource> handler) {
            super(handler);
        }

        @Override
        public AEKeyType getKeyType() {
            return AEKeyType.items();
        }

        @Override
        protected @org.jspecify.annotations.Nullable AEItemKey toKey(ItemResource resource) {
            return AEItemKey.of(resource);
        }

        @Override
        protected @org.jspecify.annotations.Nullable ItemResource toResource(AEKey key) {
            return (key instanceof AEItemKey itemKey) ? itemKey.toResource() : null;
        }
    }

    private static class FluidHandlerFacade extends ResourceHandlerFacade<FluidResource, AEFluidKey> {
        public FluidHandlerFacade(ResourceHandler<FluidResource> handler) {
            super(handler);
        }

        @Override
        public AEKeyType getKeyType() {
            return AEKeyType.fluids();
        }

        @Override
        protected @org.jspecify.annotations.Nullable AEFluidKey toKey(FluidResource resource) {
            return AEFluidKey.of(resource);
        }

        @Override
        protected @org.jspecify.annotations.Nullable FluidResource toResource(AEKey key) {
            return (key instanceof AEFluidKey fluidKey) ? fluidKey.toResource() : null;
        }
    }
}
