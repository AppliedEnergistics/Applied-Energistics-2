package appeng.me.cluster.implementations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.WorldCoord;
import appeng.container.ContainerNull;
import appeng.core.AELog;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingJob;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingCache;
import appeng.me.cluster.IAECluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.item.AEItemStack;

public class CraftingCPUCluster implements IAECluster
{

	public WorldCoord min;
	public WorldCoord max;
	public boolean isDestroyed = false;

	class TaskProgress
	{

		int value;
	};

	private LinkedList<TileCraftingTile> tiles = new LinkedList();

	int accelerator = 0;
	private LinkedList<TileCraftingTile> storage = new LinkedList<TileCraftingTile>();
	private LinkedList<TileCraftingTile> status = new LinkedList<TileCraftingTile>();

	/**
	 * crafting job info
	 */
	MECraftingInventory inventory = new MECraftingInventory();

	IAEItemStack finalOutput;

	boolean waiting = false;
	Map<ICraftingPatternDetails, TaskProgress> tasks = new HashMap<ICraftingPatternDetails, TaskProgress>();
	IItemList<IAEItemStack> waitingFor = AEApi.instance().storage().createItemList();

	@Override
	public Iterator<IGridHost> getTiles()
	{
		return (Iterator) tiles.iterator();
	}

	public CraftingCPUCluster(WorldCoord _min, WorldCoord _max) {
		min = _min;
		max = _max;
	}

	@Override
	public void updateStatus(boolean updateGrid)
	{
		for (TileCraftingTile r : tiles)
		{
			r.updateMeta();
		}
	}

	@Override
	public void destroy()
	{
		if ( isDestroyed )
			return;
		isDestroyed = true;

		boolean posted = false;

		for (TileCraftingTile r : tiles)
		{
			IGridNode n = r.getActionableNode();
			if ( n != null && posted == false )
			{
				IGrid g = n.getGrid();
				if ( g != null )
				{
					g.postEvent( new MENetworkCraftingCpuChange( n ) );
					posted = true;
				}
			}

			r.updateStatus( null );
		}
	}

	public void addTile(TileCraftingTile te)
	{
		tiles.add( te );

		if ( te.isStorage() )
			storage.add( te );
		else if ( te.isStatus() )
			status.add( te );
		else if ( te.isAccelerator() )
			accelerator++;
	}

	public boolean canAccept(IAEStack input)
	{
		if ( input instanceof IAEItemStack )
		{
			IAEItemStack is = waitingFor.findPrecise( (IAEItemStack) input );
			if ( is != null && is.getStackSize() > 0 )
				return true;
		}
		return false;
	}

	public IAEStack injectItems(IAEStack input, Actionable type, BaseActionSource src)
	{
		if ( input instanceof IAEItemStack )
		{
			IAEItemStack what = (IAEItemStack) input;
			IAEItemStack is = waitingFor.findPrecise( what );

			if ( is != null && is.getStackSize() > 0 )
			{
				waiting = false;

				if ( is.getStackSize() >= input.getStackSize() )
				{
					is.decStackSize( input.getStackSize() );

					if ( finalOutput.equals( input ) )
						return input; // ignore it.

					return inventory.injectItems( what, Actionable.MODULATE, src );
				}

				IAEItemStack insert = what.copy();
				insert.setStackSize( is.getStackSize() );
				what.decStackSize( is.getStackSize() );
				is.setStackSize( 0 );

				if ( finalOutput.equals( input ) )
					return input; // ignore it.

				inventory.injectItems( insert, Actionable.MODULATE, src );

				return what;
			}
		}

		return input;
	}

	private boolean canCraft(IAEItemStack[] condencedInputs)
	{
		for (IAEItemStack is : condencedInputs)
		{
			IAEItemStack avail = inventory.extractItems( is, Actionable.SIMULATE, null );
			if ( avail == null || avail.getStackSize() < is.getStackSize() )
				return false;
		}
		return true;
	}

	public void updateCraftingLogic(IGrid grid, CraftingCache cc)
	{
		if ( waiting || tasks.isEmpty() ) // nothing to do here...
			return;

		int remainingOperations = accelerator + 1;
		boolean didsomething = false;

		for (Entry<ICraftingPatternDetails, TaskProgress> e : tasks.entrySet())
		{
			if ( e.getValue().value <= 0 )
				continue;

			ICraftingPatternDetails details = e.getKey();
			if ( canCraft( details.getCondencedInputs() ) )
			{
				InventoryCrafting ic = null;

				for (ICraftingMedium m : cc.getMediums( e.getKey() ))
				{
					if ( !m.isBusy() )
					{
						if ( ic == null )
						{
							ic = new InventoryCrafting( new ContainerNull(), 3, 3 );

							IAEItemStack[] input = details.getInputs();
							for (int x = 0; x < input.length; x++)
							{
								if ( input[x] != null )
								{
									boolean found = true;

									if ( details.isCraftable() )
									{
										for (IAEItemStack fuzz : inventory.getItemList().findFuzzy( input[x], FuzzyMode.IGNORE_ALL ))
										{
											IAEItemStack ais = inventory.extractItems( fuzz, Actionable.MODULATE, null );
											ItemStack is = ais == null ? null : ais.getItemStack();

											if ( is != null && details.isValidItemForSlot( x, is, getWorld() ) )
											{
												ic.setInventorySlotContents( x, is );
												found = true;
												break;
											}
										}
									}
									else
									{
										IAEItemStack ais = inventory.extractItems( input[x].copy(), Actionable.MODULATE, null );
										ItemStack is = ais == null ? null : ais.getItemStack();

										if ( is != null )
										{
											ic.setInventorySlotContents( x, is );
											found = true;
											continue;
										}
									}

									if ( !found )
										break;
								}
							}
						}

						if ( m.pushPattern( details, ic ) )
						{
							didsomething = true;
							remainingOperations--;

							for (IAEItemStack out : details.getCondencedOutputs())
								waitingFor.add( out.copy() );

							ic = null; // hand off complete!

							e.getValue().value--;
							if ( e.getValue().value <= 0 )
								continue;

							if ( remainingOperations == 0 )
								return;
						}
					}
				}

				if ( ic != null )
				{
					// put stuff back..
					for (int x = 0; x < ic.getSizeInventory(); x++)
					{
						ItemStack is = ic.getStackInSlot( x );
						if ( is != null )
							inventory.injectItems( AEItemStack.create( is ), Actionable.MODULATE, null );
					}
				}
			}
		}

		if ( remainingOperations > 0 && didsomething == false )
			waiting = true;
	}

	private World getWorld()
	{
		return null;
	}

	public boolean submitJob(IGrid g, CraftingJob job, BaseActionSource src)
	{
		Iterator<Entry<ICraftingPatternDetails, TaskProgress>> i = tasks.entrySet().iterator();
		while (i.hasNext())
		{
			if ( i.next().getValue().value <= 0 )
				i.remove();
		}

		if ( !tasks.isEmpty() || !waitingFor.isEmpty() )
			return false;

		IStorageGrid sg = g.getCache( IStorageGrid.class );
		IMEInventory<IAEItemStack> storage = sg.getItemInventory();
		MECraftingInventory ci = new MECraftingInventory( storage, true, false, false );

		try
		{
			job.tree.setJob( ci, this, src );
			ci.commit( src );
			finalOutput = job.getOutput();
			waiting = false;
			return true;
		}
		catch (CraftBranchFailure e)
		{
			tasks.clear();
			inventory.getItemList().resetStatus();
			AELog.error( e );
		}

		return false;
	}

	public void addStorage(IAEItemStack extractItems)
	{
		inventory.injectItems( extractItems, Actionable.MODULATE, null );
	}

	public void addCrafting(ICraftingPatternDetails details, long crafts)
	{
		TaskProgress i = tasks.get( details );

		if ( i == null )
			tasks.put( details, i = new TaskProgress() );

		i.value += crafts;
	}
}
