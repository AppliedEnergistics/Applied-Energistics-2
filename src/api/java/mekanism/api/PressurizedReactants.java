package mekanism.api;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * An input of a gas, a fluid and an item for the pressurized reaction chamber
 */
public class PressurizedReactants
{
	private ItemStack theSolid;
	private FluidStack theFluid;
	private GasStack theGas;

	public PressurizedReactants(ItemStack solid, FluidStack fluid, GasStack gas)
	{
		theSolid = solid;
		theFluid = fluid;
		theGas = gas;
	}

	/**
	 * If this is a valid PressurizedReactants
	 */
	public boolean isValid()
	{
		return theSolid != null && theFluid != null && theGas != null;
	}

	/**
	 * Draws the needed amount of gas from each tank.
	 * @param item - ItemStack to draw from
	 * @param fluidTank - fluid tank to draw from
	 * @param gasTank - gas tank to draw from
	 */
	public void use(ItemStack item, FluidTank fluidTank, GasTank gasTank)
	{
		if(meets(new PressurizedReactants(item, fluidTank.getFluid(), gasTank.getGas())))
		{
			item.stackSize -= theSolid.stackSize;
			fluidTank.drain(theFluid.amount, true);
			gasTank.draw(theGas.amount, true);
		}
	}

	/**
	 * Whether or not this PressurizedReactants's ItemStack entry's item type is equal to the item type of the given item.
	 * @param stack - stack to check
	 * @return if the stack's item type is contained in this PressurizedReactants
	 */
	public boolean containsType(ItemStack stack)
	{
		if(stack == null || stack.stackSize == 0)
		{
			return false;
		}

		return StackUtils.equalsWildcard(stack, theSolid);
	}

	/**
	 * Whether or not this PressurizedReactants's FluidStack entry's fluid type is equal to the fluid type of the given fluid.
	 * @param stack - stack to check
	 * @return if the stack's fluid type is contained in this PressurizedReactants
	 */
	public boolean containsType(FluidStack stack)
	{
		if(stack == null || stack.amount == 0)
		{
			return false;
		}

		return stack.isFluidEqual(theFluid);
	}

	/**
	 * Whether or not this PressurizedReactants's GasStack entry's gas type is equal to the gas type of the given gas.
	 * @param stack - stack to check
	 * @return if the stack's gas type is contained in this PressurizedReactants
	 */
	public boolean containsType(GasStack stack)
	{
		if(stack == null || stack.amount == 0)
		{
			return false;
		}

		return stack.isGasEqual(theGas);
	}

	public boolean meetsInput(ItemStack itemStack, FluidStack fluidStack, GasStack gasStack)
	{
		return meets(new PressurizedReactants(itemStack, fluidStack, gasStack));
	}

	/**
	 * Actual implementation of meetsInput(), performs the checks.
	 * @param input - input to check
	 * @return if the input meets this input's requirements
	 */
	private boolean meets(PressurizedReactants input)
	{
		if(input == null || !input.isValid())
		{
			return false;
		}

		if(!(StackUtils.equalsWildcard(input.theSolid, theSolid) && input.theFluid.isFluidEqual(theFluid) && input.theGas.isGasEqual(theGas)))
		{
			return false;
		}

		return input.theSolid.stackSize >= theSolid.stackSize && input.theFluid.amount >= theFluid.amount && input.theGas.amount >= theGas.amount;
	}

	public PressurizedReactants copy()
	{
		return new PressurizedReactants(theSolid.copy(), theFluid.copy(), theGas.copy());
	}
	
	public ItemStack getSolid()
	{
		return theSolid;
	}
	
	public FluidStack getFluid()
	{
		return theFluid;
	}
	
	public GasStack getGas()
	{
		return theGas;
	}
}
