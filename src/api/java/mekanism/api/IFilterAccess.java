package mekanism.api;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Implement this in your TileEntity class if you wish for Mekanism filters to be able to store any of their
 * information.
 * @author aidancbrady
 *
 */
public interface IFilterAccess
{
	/**
	 * Collects the TileEntity's filter card data into the parameterized NBTTagCompound.
	 * @param nbtTags - the NBTTagCompound of the filter card ItemStack
	 * @return the NBTTagCompound that now contains the TileEntity's filter card data
	 */
	public NBTTagCompound getFilterData(NBTTagCompound nbtTags);
	
	/**
	 * Retrieves the TileEntity's data contained in the filter card based on the given NBTTagCompopund.
	 * @param nbtTags - the NBTTagCompound of the filter card ItemStack
	 */
	public void setFilterData(NBTTagCompound nbtTags);
	
	/**
	 * A String name of this TileEntity that will be displayed as the type of data on the filter card.
	 * @return the String name of this TileEntity
	 */
	public String getDataType();
}
