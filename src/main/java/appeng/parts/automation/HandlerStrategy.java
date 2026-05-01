package appeng.parts.automation;

import javax.annotation.Nullable;

import com.google.common.primitives.Ints;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.me.storage.ExternalStorageFacade;

public abstract class HandlerStrategy<C, S> {
    private final AEKeyType keyType;

    public HandlerStrategy(AEKeyType keyType) {
        this.keyType = keyType;
    }

    public boolean isSupported(AEKey what) {
        return what.getType() == keyType;
    }

    public AEKeyType getKeyType() {
        return keyType;
    }

    public abstract ExternalStorageFacade getFacade(C handler);

    @Nullable
    public abstract S getStack(AEKey what, long amount);

    public abstract long insert(C handler, AEKey what, long amount, Actionable mode);

    public static final HandlerStrategy<ResourceHandler<ItemResource>, ItemStack> ITEMS = new HandlerStrategy<>(
            AEKeyType.items()) {
        @Override
        public boolean isSupported(AEKey what) {
            return AEItemKey.is(what);
        }

        @Override
        public ExternalStorageFacade getFacade(ResourceHandler<ItemResource> handler) {
            return ExternalStorageFacade.ofItemHandler(handler);
        }

        @Override
        public long insert(ResourceHandler<ItemResource> handler, AEKey what, long amount, Actionable mode) {
            if (what instanceof AEItemKey itemKey && amount > 0) {
                var insertAmount = Ints.saturatedCast(amount);

                try (var tx = Transaction.open(null)) {
                    var inserted = handler.insert(itemKey.toResource(), insertAmount, tx);
                    if (!mode.isSimulate()) {
                        tx.commit();
                    }
                    return inserted;
                }
            }

            return 0;
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public ItemStack getStack(AEKey what, long amount) {
            if (what instanceof AEItemKey itemKey) {
                return itemKey.toStack(Ints.saturatedCast(amount));
            }
            return null;
        }
    };

    public static final HandlerStrategy<ResourceHandler<FluidResource>, FluidStack> FLUIDS = new HandlerStrategy<>(
            AEKeyType.fluids()) {
        @Override
        public boolean isSupported(AEKey what) {
            return AEFluidKey.is(what);
        }

        @Override
        public ExternalStorageFacade getFacade(ResourceHandler<FluidResource> handler) {
            return ExternalStorageFacade.ofFluidHandler(handler);
        }

        @Override
        public long insert(ResourceHandler<FluidResource> handler, AEKey what, long amount, Actionable mode) {
            if (what instanceof AEFluidKey fluidKey && amount > 0) {
                var insertAmount = Ints.saturatedCast(amount);

                try (var tx = Transaction.open(null)) {
                    var inserted = handler.insert(fluidKey.toResource(), insertAmount, tx);
                    if (!mode.isSimulate()) {
                        tx.commit();
                    }
                    return inserted;
                }
            }

            return 0;
        }

        @Override
        public FluidStack getStack(AEKey what, long amount) {
            if (what instanceof AEFluidKey fluidKey) {
                return fluidKey.toStack(Ints.saturatedCast(amount));
            }
            return null;
        }
    };

}
