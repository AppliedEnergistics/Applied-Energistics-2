package appeng.parts.automation;

import javax.annotation.Nullable;

import com.google.common.primitives.Ints;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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

    public static final HandlerStrategy<IItemHandler, ItemStack> ITEMS = new HandlerStrategy<>(AEKeyType.items()) {
        @Override
        public boolean isSupported(AEKey what) {
            return AEItemKey.is(what);
        }

        @Override
        public ExternalStorageFacade getFacade(IItemHandler handler) {
            return ExternalStorageFacade.of(handler);
        }

        @Override
        public long insert(IItemHandler handler, AEKey what, long amount, Actionable mode) {
            if (what instanceof AEItemKey itemKey) {
                var stack = itemKey.toStack(Ints.saturatedCast(amount));

                var remainder = ItemHandlerHelper.insertItem(handler, stack, mode.isSimulate());
                return amount - remainder.getCount();
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

    public static final HandlerStrategy<IFluidHandler, FluidStack> FLUIDS = new HandlerStrategy<>(AEKeyType.fluids()) {
        @Override
        public boolean isSupported(AEKey what) {
            return AEFluidKey.is(what);
        }

        @Override
        public ExternalStorageFacade getFacade(IFluidHandler handler) {
            return ExternalStorageFacade.of(handler);
        }

        @Override
        public long insert(IFluidHandler handler, AEKey what, long amount, Actionable mode) {
            if (what instanceof AEFluidKey itemKey) {
                var stack = itemKey.toStack(Ints.saturatedCast(amount));
                return handler.fill(stack, mode.getFluidAction());
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
