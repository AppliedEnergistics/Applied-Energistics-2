package mekanism.api.infuse;

import net.minecraft.item.ItemStack;

/**
 * An infusion input, containing the type of and amount of infuse the operation requires, as well as the input ItemStack.
 * @author AidanBrady
 *
 */
public class InfusionInput
{
	/** The type of this infusion */
	public InfuseType infusionType;

	/** How much infuse it takes to perform this operation */
	public int infuseAmount;

	/** The input ItemStack */
	public ItemStack inputStack;

	public InfusionInput(InfuseType infusiontype, int required, ItemStack itemstack)
	{
		infusionType = infusiontype;
		infuseAmount = required;
		inputStack = itemstack;
	}

	public static InfusionInput getInfusion(InfuseType type, int stored, ItemStack itemstack)
	{
		return new InfusionInput(type, stored, itemstack);
	}
}
