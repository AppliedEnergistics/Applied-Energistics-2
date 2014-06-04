package appeng.crafting;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

import com.google.common.collect.Multimap;

public class CraftingJob implements ICraftingParent
{

	IAEItemStack output;

	IItemList<IAEItemStack> storage;

	HashSet<IAEItemStack> prophecies;

	Multimap<IAEItemStack, CraftingTask> bottom;
	CraftingTask top;

	public Collection<CraftingTask> getBottom()
	{
		return bottom.values();
	}

	public IAEItemStack addProgress(IAEItemStack progress)
	{
		Collection<CraftingTask> task = bottom.get( progress );

		if ( task == null )
			return progress;

		Iterator<CraftingTask> i = task.iterator();

		while (i.hasNext())
		{
			// adding to the first item..
			progress = i.next().addProgress( progress );

			// if nothing remains return
			if ( progress == null )
				return null;

			// if something remains continue..
			i = task.iterator();
		}

		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound out)
	{

	}

	@Override
	public void removeChild(CraftingTask craftingTask)
	{
		// TODO Auto-generated method stub
	}

	public ICraftingHost getHost()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends ICraftingParent> getSubJobs()
	{
		return Collections.singleton( top );
	}

}
