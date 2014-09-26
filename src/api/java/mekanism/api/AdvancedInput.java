package mekanism.api;

import mekanism.api.gas.Gas;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;

public class AdvancedInput
{
	public ItemStack itemStack;

	public Gas gasType;

	public AdvancedInput(ItemStack item, Gas gas)
	{
		itemStack = item;
		gasType = gas;
	}

	public boolean isValid()
	{
		return itemStack != null && gasType != null;
	}

	public boolean matches(AdvancedInput input)
	{
		return StackUtils.equalsWildcard(itemStack, input.itemStack) && input.itemStack.stackSize >= itemStack.stackSize;
	}
}
