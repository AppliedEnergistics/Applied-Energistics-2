package appeng.api.networking.crafting;

import net.minecraft.nbt.NBTTagCompound;

public interface ICraftingLink
{

	/**
	 * @return true if the job was canceled.
	 */
	boolean isCanceled();

	/**
	 * @return true if the job was completed.
	 */
	boolean isDone();

	/**
	 * cancels the job.
	 */
	void cancel();

	/**
	 * @return true if this link was generated without a requesting machine, such as a player generated request.
	 */
	boolean isStandalone();

	/**
	 * write the link to an NBT Tag
	 * 
	 * @param tag to be written data
	 */
	void writeToNBT(NBTTagCompound tag);

	/**
	 * @return the crafting ID for this link.
	 */
	String getCraftingID();

}
