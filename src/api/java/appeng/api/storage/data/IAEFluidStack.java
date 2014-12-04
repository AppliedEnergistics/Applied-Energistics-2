package appeng.api.storage.data;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/**
 * An alternate version of FluidStack for AE to keep tabs on things easier, and
 * to support larger storage. stackSizes of getFluidStack will be capped.
 * 
 * You may hold on to these if you want, just make sure you let go of them when
 * your not using them.
 *
 * Don't Implement.
 * 
 * Construct with Util.createFluidStack( FluidStack )
 * 
 */
public interface IAEFluidStack extends IAEStack<IAEFluidStack>
{

	/**
	 * creates a standard Forge FluidStack for the fluid.
	 * 
	 * @return new FluidStack
	 */
	FluidStack getFluidStack();

	/**
	 * create a AE Fluid clone.
	 * 
	 * @return the copy.
	 */
	@Override
	public IAEFluidStack copy();

	/**
	 * Combines two IAEItemStacks via addition.
	 * 
	 * @param option
	 *            , to add to the current one.
	 */
	@Override
	void add(IAEFluidStack option);

	/**
	 * quick way to get access to the Forge Fluid Definition.
	 * 
	 * @return fluid definition
	 */
	Fluid getFluid();

}
