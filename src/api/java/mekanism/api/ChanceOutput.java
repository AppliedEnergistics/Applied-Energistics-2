package mekanism.api;

import java.util.Random;

import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;

public class ChanceOutput
{
	private static Random rand = new Random();

	public ItemStack primaryOutput;

	public ItemStack secondaryOutput;

	public double secondaryChance;

	public ChanceOutput(ItemStack primary, ItemStack secondary, double chance)
	{
		primaryOutput = primary;
		secondaryOutput = secondary;
		secondaryChance = chance;
	}

	public ChanceOutput(ItemStack primary)
	{
		primaryOutput = primary;
	}

	public boolean checkSecondary()
	{
		return rand.nextDouble() <= secondaryChance;
	}

	public boolean hasPrimary()
	{
		return primaryOutput != null;
	}

	public boolean hasSecondary()
	{
		return secondaryOutput != null;
	}

	public ChanceOutput copy()
	{
		return new ChanceOutput(StackUtils.copy(primaryOutput), StackUtils.copy(secondaryOutput), secondaryChance);
	}
}
