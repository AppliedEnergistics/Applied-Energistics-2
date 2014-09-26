package mekanism.api.gas;

import net.minecraft.item.ItemStack;

/**
 * Implement this in your item class if it can store or transfer certain gasses.
 * @author AidanBrady
 *
 */
public interface IGasItem
{
	/**
	 * Gets the rate of transfer this item can handle.
	 * @return
	 */
	public int getRate(ItemStack itemstack);

	/**
	 * Adds a defined amount of a certain gas to an item.
	 * @param itemstack - the itemstack to add gas to
	 * @param type - the type of gas to add
	 * @param amount - the amount of gas to add
	 * @return the gas that was accepted by the item
	 */
	public int addGas(ItemStack itemstack, GasStack stack);

	/**
	 * Removes the defined amount of a certain gas from the item.
	 * @param itemstack - the itemstack to remove gas from
	 * @param type - the type of gas to remove
	 * @param amount - the amount of gas to remove
	 * @return the gas that was removed by the item
	 */
	public GasStack removeGas(ItemStack itemstack, int amount);

	/**
	 * Whether or not this storage tank be given a specific gas.
	 * @param itemstack - the itemstack to check
	 * @param type - the type of gas the tank can possibly receive
	 * @return if the item be charged
	 */
	public boolean canReceiveGas(ItemStack itemstack, Gas type);

	/**
	 * Whether or not this item can give a gas receiver a certain type of gas.
	 * @param itemstack - the itemstack to check
	 * @param type - the type of gas the tank can provide
	 * @return if the item can provide gas
	 */
	public boolean canProvideGas(ItemStack itemstack, Gas type);

	/**
	 * Get the gas of a declared type.
	 * @param type - type of gas
	 * @param data - ItemStack parameter if necessary
	 * @return gas stored
	 */
	public GasStack getGas(ItemStack itemstack);

	/**
	 * Set the gas of a declared type to a new amount;
	 * @param type - type of gas
	 * @param data - ItemStack parameter if necessary
	 * @param amount - amount to store
	 */
	public void setGas(ItemStack itemstack, GasStack stack);

	/**
	 * Gets the maximum amount of gas this tile entity can store.
	 * @param type - type of gas
	 * @param data - ItemStack parameter if necessary
	 * @return maximum gas
	 */
	public int getMaxGas(ItemStack itemstack);
	
	/**
	 * Returns whether or not this item contains metadata-specific subtypes instead of using metadata for damage display.
	 * @return if the item contains metadata-specific subtypes
	 */
	public boolean isMetadataSpecific(ItemStack itemstack);
}
