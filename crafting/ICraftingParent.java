package appeng.crafting;

import java.util.Collection;

import net.minecraft.nbt.NBTTagCompound;

public interface ICraftingParent
{

	Collection<? extends ICraftingParent> getSubJobs();

	void writeToNBT(NBTTagCompound out);

	void removeChild(CraftingTask craftingTask);

}
