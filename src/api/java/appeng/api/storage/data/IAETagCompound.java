package appeng.api.storage.data;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.features.IItemComparison;

/**
 * Don't cast this... either compare with it, or copy it.
 * 
 * Don't Implement.
 */
public interface IAETagCompound
{

	/**
	 * @return a copy ( the copy will not be a IAETagCompound, it will be a NBTTagCompound )
	 */
	public NBTTagCompound getNBTTagCompoundCopy();

	/**
	 * compare to other NBTTagCompounds or IAETagCompounds
	 * 
	 * @param a compared object
	 * @return true, if they are the same.
	 */
	@Override
	boolean equals(Object a);

	/**
	 * @return the special comparison for this tag
	 */
	IItemComparison getSpecialComparison();

}