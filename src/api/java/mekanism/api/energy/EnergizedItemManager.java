package mekanism.api.energy;

import net.minecraft.item.ItemStack;

public class EnergizedItemManager
{
	/**
	 * Discharges an IEnergizedItem with the defined amount of energy.
	 * @param itemStack - ItemStack to discharge
	 * @param amount - amount of energy to discharge from the item, usually the total amount of energy needed in a TileEntity
	 * @return amount of energy discharged
	 */
	public static double discharge(ItemStack itemStack, double amount)
	{
		if(itemStack != null)
		{
			if(itemStack.getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();

				if(energizedItem.canSend(itemStack))
				{
					double energyToUse = Math.min(energizedItem.getMaxTransfer(itemStack), Math.min(energizedItem.getEnergy(itemStack), amount));
					energizedItem.setEnergy(itemStack, energizedItem.getEnergy(itemStack) - energyToUse);

					return energyToUse;
				}
			}
		}

		return 0;
	}

	/**
	 * Charges an IEnergizedItem with the defined amount of energy.
	 * @param itemStack - ItemStack to charge
	 * @param amount - amount of energy to charge the item with, usually the total amount of energy stored in a TileEntity
	 * @return amount of energy charged
	 */
	public static double charge(ItemStack itemStack, double amount)
	{
		if(itemStack != null)
		{
			if(itemStack.getItem() instanceof IEnergizedItem)
			{
				IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();

				if(energizedItem.canReceive(itemStack))
				{
					double energyToSend = Math.min(energizedItem.getMaxTransfer(itemStack), Math.min(energizedItem.getMaxEnergy(itemStack) - energizedItem.getEnergy(itemStack), amount));
					energizedItem.setEnergy(itemStack, energizedItem.getEnergy(itemStack) + energyToSend);

					return energyToSend;
				}
			}
		}

		return 0;
	}
}
