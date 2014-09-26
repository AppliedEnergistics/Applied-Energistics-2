package mekanism.api;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

import net.minecraft.item.ItemStack;

public class PressurizedProducts
{
	private ItemStack itemOutput;
	private GasStack gasOutput;

	public PressurizedProducts(ItemStack item, GasStack gas)
	{
		itemOutput = item;
		gasOutput = gas;
	}

	public void fillTank(GasTank tank)
	{
		tank.receive(gasOutput, true);
	}

	public void addProducts(ItemStack[] inventory, int index)
	{
		if(inventory[index] == null)
		{
			inventory[index] = itemOutput.copy();
		}
		else if(inventory[index].isItemEqual(itemOutput))
		{
			inventory[index].stackSize += itemOutput.stackSize;
		}
	}

	public ItemStack getItemOutput()
	{
		return itemOutput;
	}

	public GasStack getGasOutput()
	{
		return gasOutput;
	}

	public PressurizedProducts copy()
	{
		return new PressurizedProducts(itemOutput.copy(), gasOutput.copy());
	}
}
