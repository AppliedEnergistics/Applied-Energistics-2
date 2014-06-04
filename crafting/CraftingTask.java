package appeng.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.item.AEItemStack;

public class CraftingTask implements ICraftingParent
{

	final IAEItemStack task;

	final ICraftingPatternDetails process;

	final List<CraftingTask> subtasks;
	final ICraftingParent parenttask;

	final CraftingJob job;

	public CraftingTask(IAEItemStack myTask, ICraftingPatternDetails pattern, ICraftingParent parent, CraftingJob craftingJob) {
		task = myTask;
		process = pattern;
		parenttask = parent;
		job = craftingJob;
		subtasks = new ArrayList();

		// register as part of the bottom.
		job.bottom.put( task.copy(), this );
	}

	public CraftingTask(NBTTagCompound c, ICraftingParent parent, CraftingJob craftingJob) {
		task = AEItemStack.loadItemStackFromNBT( c.getCompoundTag( "task" ) );
		ItemStack pattern = ItemStack.loadItemStackFromNBT( c.getCompoundTag( "pattern" ) );

		if ( pattern == null || !(pattern.getItem() instanceof ItemEncodedPattern) )
			throw new RuntimeException( "Failed to load crafting job" );

		ItemEncodedPattern iep = (ItemEncodedPattern) pattern.getItem();
		process = iep.getPatternForItem( pattern, craftingJob.getHost().getWorld() );

		subtasks = new ArrayList();
		parenttask = parent;
		job = craftingJob;
	}

	public IAEItemStack addProgress(IAEItemStack progress)
	{
		long amt = progress.getStackSize();
		long mine = task.getStackSize();

		if ( amt >= mine )
		{
			parenttask.removeChild( this );

			task.reset();
			job.bottom.remove( task, this );

			progress = progress.copy();
			progress.decStackSize( mine );
			return progress;
		}
		else
		{
			task.decStackSize( amt );
			return null;
		}
	}

	public void addSubTask(CraftingTask task)
	{
		boolean wasEmpty = subtasks.isEmpty();

		subtasks.add( task );

		if ( wasEmpty )
			job.bottom.remove( task, this );
	}

	public void removeChild(CraftingTask craftingTask)
	{
		subtasks.remove( craftingTask );
		if ( subtasks.isEmpty() )
		{
			job.bottom.put( task.copy(), this );
		}
	}

	@Override
	public Collection<? extends ICraftingParent> getSubJobs()
	{
		return subtasks;
	}

	@Override
	public void writeToNBT(NBTTagCompound out)
	{

	}

}
