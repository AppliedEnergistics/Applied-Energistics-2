package mekanism.api.infuse;

import net.minecraft.item.ItemStack;

/**
 * An infusion output, containing a reference to it's input as well as the output resource.
 * @author AidanBrady
 *
 */
public class InfusionOutput
{
	/** The input infusion */
	public InfusionInput infusionInput;

	/** The output resource of this infusion */
	public ItemStack resource;

	public InfusionOutput(InfusionInput input, ItemStack itemstack)
	{
		infusionInput = input;
		resource = itemstack;
	}

	public static InfusionOutput getInfusion(InfusionInput input, ItemStack itemstack)
	{
		return new InfusionOutput(input, itemstack);
	}

	public int getInfuseRequired()
	{
		return infusionInput.infuseAmount;
	}

	public InfusionOutput copy()
	{
		return new InfusionOutput(infusionInput, resource);
	}
}
