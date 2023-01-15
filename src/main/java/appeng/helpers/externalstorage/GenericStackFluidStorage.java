package appeng.helpers.externalstorage;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;

/**
 * Exposes a {@link GenericInternalInventory} as the platforms external fluid storage interface.
 */
public class GenericStackFluidStorage implements IFluidHandler {
    private final GenericInternalInventory inv;

    public GenericStackFluidStorage(GenericInternalInventory inv) {
        this.inv = inv;
    }

    @Override
    public int getTanks() {
        return inv.size();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        if (inv.getKey(tank) instanceof AEFluidKey what) {
            var amount = Ints.saturatedCast(inv.getAmount(tank));
            return what.toStack(amount);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Ints.saturatedCast(inv.getCapacity(AEKeyType.fluids()));
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        var what = AEFluidKey.of(stack);
        return what == null || inv.isAllowed(what);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        var what = AEFluidKey.of(resource);
        if (what == null) {
            return 0;
        }

        int inserted = 0;
        for (int i = 0; i < inv.size() && inserted < resource.getAmount(); ++i) {
            inserted += (int) inv.insert(i, what, resource.getAmount() - inserted, Actionable.of(action));
        }
        return inserted;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        var what = AEFluidKey.of(resource);
        if (what == null) {
            return FluidStack.EMPTY;
        }

        return extract(what, resource.getAmount(), action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        // Find first fluid in tanks
        for (int i = 0; i < inv.size(); i++) {
            var what = inv.getKey(i);
            if (inv.getKey(i) instanceof AEFluidKey fluidKey) {
                return extract(fluidKey, maxDrain, action);
            }
        }
        return FluidStack.EMPTY;
    }

    private FluidStack extract(AEFluidKey what, int amount, FluidAction action) {
        int extracted = 0;
        for (int i = 0; i < inv.size() && extracted < amount; ++i) {
            extracted += (int) inv.extract(i, what, amount - extracted, Actionable.of(action));
        }

        if (extracted > 0) {
            return what.toStack(extracted);
        }
        return FluidStack.EMPTY;
    }
}
