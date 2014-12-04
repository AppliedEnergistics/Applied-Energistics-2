package appeng.api.networking;

import net.minecraft.nbt.NBTTagCompound;

public interface IGridStorage
{

	/**
	 * @return an NBTTagCompound that can be read, and written too.
	 */
	NBTTagCompound dataObject();

	/**
	 * @return the id for this grid storage object, used internally
	 */
	long getID();

}
