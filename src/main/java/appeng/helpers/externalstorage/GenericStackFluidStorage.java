package appeng.helpers.externalstorage;

import com.google.common.primitives.Ints;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;
import appeng.me.helpers.BaseActionSource;

/**
 * Exposes a {@link GenericStackInv} as the platforms external fluid storage interface.
 */
public class GenericStackFluidStorage implements IFluidHandler {
    private final GenericStackInv inv;

    public GenericStackFluidStorage(GenericStackInv inv) {
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

        return (int) inv.insert(what, resource.getAmount(), Actionable.of(action), new BaseActionSource());
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
        var extracted = (int) inv.extract(what, amount, Actionable.of(action), new BaseActionSource());

        if (extracted > 0) {
            return what.toStack(extracted);
        }
        return FluidStack.EMPTY;
    }
}
