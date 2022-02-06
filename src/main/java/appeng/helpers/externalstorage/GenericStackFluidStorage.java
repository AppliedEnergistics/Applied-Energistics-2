package appeng.helpers.externalstorage;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.AEKeyType;
import appeng.util.IVariantConversion;

/**
 * Exposes a {@link GenericStackInv} as the platforms external fluid storage interface.
 */
public class GenericStackFluidStorage extends GenericStackInvStorage<FluidVariant> {
    public GenericStackFluidStorage(GenericInternalInventory inv) {
        super(IVariantConversion.FLUID, AEKeyType.fluids(), inv);
    }
}
