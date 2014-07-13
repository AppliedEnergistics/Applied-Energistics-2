package appeng.me.cluster.implementations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.WorldCoord;
import appeng.container.ContainerNull;
import appeng.core.AELog;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingLink;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.IAECluster;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;

public class CraftingCPUCluster implements IAECluster, ICraftingCPU
{

	class TaskProgress
	{

		long value;

	};

	/**
	 * crafting job info
	 */
	MECraftingInventory inventory = new MECraftingInventory();
	IAEItemStack finalOutput;

	boolean waiting = false;
	private boolean isComplete = true;
	Map<ICraftingPatternDetails, TaskProgress> tasks = new HashMap<ICraftingPatternDetails, TaskProgress>();
	IItemList<IAEItemStack> waitingFor = AEApi.instance().storage().createItemList();

	// instance sate
	private LinkedList<TileCraftingTile> tiles = new LinkedList();
	private LinkedList<TileCraftingTile> storage = new LinkedList<TileCraftingTile>();
	private LinkedList<TileCraftingMonitorTile> status = new LinkedList<TileCraftingMonitorTile>();

	long availableStorage = 0;
	public ICraftingLink myLastLink;

	MachineSource machineSrc = null;

	int accelerator = 0;
	public WorldCoord min;
	public WorldCoord max;
	public boolean isDestroyed = false;

	private final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object>();

	protected Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> getListeners()
	{
		return listeners.entrySet().iterator();
	}

	protected void postChange(IAEItemStack diff, BaseActionSource src)
	{
		Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = getListeners();

		// protect integrity
		if ( i.hasNext() )
			diff = diff.copy();

		while (i.hasNext())
		{
			Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> o = i.next();
			IMEMonitorHandlerReceiver<IAEItemStack> recv = o.getKey();
			if ( recv.isValid( o.getValue() ) )
				recv.postChange( null, diff, src );
			else
				i.remove();
		}
	}

	/**
	 * add a new Listener to the monitor, be sure to properly remove yourself when your done.
	 */
	@Override
	public void addListener(IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken)
	{
		listeners.put( l, verificationToken );
	}

	/**
	 * remove a Listener to the monitor.
	 */
	@Override
	public void removeListener(IMEMonitorHandlerReceiver<IAEItemStack> l)
	{
		listeners.remove( l );
	}

	public void getListOfItem(IItemList<IAEItemStack> list, CraftingItemList whichList)
	{
		switch (whichList)
		{
		case ACTIVE:
			for (IAEItemStack ais : waitingFor)
				list.add( ais );
			break;
		case PENDING:
			for (Entry<ICraftingPatternDetails, TaskProgress> t : tasks.entrySet())
			{
				for (IAEItemStack ais : t.getKey().getCondencedOutputs())
				{
					ais = ais.copy();
					ais.setStackSize( ais.getStackSize() * t.getValue().value );
					list.add( ais );
				}
			}
			break;
		case STORAGE:
			inventory.getAvailableItems( list );
			break;
		default:
		case ALL:
			inventory.getAvailableItems( list );

			for (IAEItemStack ais : waitingFor)
				list.add( ais );

			for (Entry<ICraftingPatternDetails, TaskProgress> t : tasks.entrySet())
			{
				for (IAEItemStack ais : t.getKey().getCondencedOutputs())
				{
					ais = ais.copy();
					ais.setStackSize( ais.getStackSize() * t.getValue().value );
					list.add( ais );
				}
			}
			break;

		}
	}

	@Override
	public Iterator<IGridHost> getTiles()
	{
		return (Iterator) tiles.iterator();
	}

	public IMEInventory<IAEItemStack> getInventory()
	{
		return inventory;
	}

	public CraftingCPUCluster(WorldCoord _min, WorldCoord _max) {
		min = _min;
		max = _max;
	}

	@Override
	public void updateStatus(boolean updateGrid)
	{
		for (TileCraftingTile r : tiles)
			r.updateMeta( true );
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

	@Override
	public long getAvailableStorage()
	{
		return availableStorage;
	}

	@Override
	public int getCoProcessors()
	{
		return accelerator;
	}

	public void addTile(TileCraftingTile te)
	{
		if ( machineSrc == null || te.isCoreBlock )
			machineSrc = new MachineSource( te );

		te.isCoreBlock = false;
		te.markDirty();
		tiles.add( te );

		if ( te.isStorage() )
		{
			availableStorage += te.getStorageBytes();
			storage.add( te );
		}
		else if ( te.isStatus() )
			status.add( (TileCraftingMonitorTile) te );
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
		if ( input instanceof IAEItemStack && type == Actionable.SIMULATE )// causes crafting to lock up?
		{
			IAEItemStack what = (IAEItemStack) input.copy();
			IAEItemStack is = waitingFor.findPrecise( what );

			if ( is != null && is.getStackSize() > 0 )
			{
				if ( is.getStackSize() >= what.getStackSize() )
				{
					if ( finalOutput.equals( what ) )
					{
						if ( myLastLink != null )
							return ((CraftingLink) myLastLink).injectItems( (IAEItemStack) what.copy(), type );

						return what; // ignore it.
					}

					return null;
				}

				IAEItemStack leftOver = what.copy();
				leftOver.decStackSize( is.getStackSize() );

				IAEItemStack used = what.copy();
				used.setStackSize( is.getStackSize() );

				if ( finalOutput.equals( what ) )
				{
					if ( myLastLink != null )
					{
						leftOver.add( ((CraftingLink) myLastLink).injectItems( (IAEItemStack) used.copy(), type ) );
						return leftOver;
					}

					return what; // ignore it.
				}

				return leftOver;
			}
		}
		else if ( input instanceof IAEItemStack && type == Actionable.MODULATE )
		{
			IAEItemStack what = (IAEItemStack) input;
			IAEItemStack is = waitingFor.findPrecise( what );

			if ( is != null && is.getStackSize() > 0 )
			{
				waiting = false;
				postChange( (IAEItemStack) input, src );

				if ( is.getStackSize() >= input.getStackSize() )
				{
					is.decStackSize( input.getStackSize() );
					markDirty();

					// AELog.info( "Task: " + is.getStackSize() + " remaining : " + getRemainingTasks() +
					// " remaining : "
					// + (is.getStackSize() + getRemainingTasks()) + " total left : waiting: " + (waiting ? "yes" :
					// "no") );

					if ( finalOutput.equals( input ) )
					{
						finalOutput.decStackSize( input.getStackSize() );
						if ( finalOutput.getStackSize() <= 0 )
							completeJob();

						updateCPU();

						if ( myLastLink != null )
							return ((CraftingLink) myLastLink).injectItems( (IAEItemStack) input, type );

						return input; // ignore it.
					}

					// 2000
					return inventory.injectItems( what, type, src );
				}

				IAEItemStack insert = what.copy();
				insert.setStackSize( is.getStackSize() );
				what.decStackSize( is.getStackSize() );

				is.setStackSize( 0 );

				if ( finalOutput.equals( insert ) )
				{
					finalOutput.decStackSize( insert.getStackSize() );
					if ( finalOutput.getStackSize() <= 0 )
						completeJob();

					updateCPU();

					if ( myLastLink != null )
					{
						what.add( ((CraftingLink) myLastLink).injectItems( (IAEItemStack) insert.copy(), type ) );
						return what;
					}

					if ( myLastLink != null )
						return ((CraftingLink) myLastLink).injectItems( (IAEItemStack) input, type );

					return input; // ignore it.
				}

				inventory.injectItems( insert, type, src );
				markDirty();

				return what;
			}
		}

		return input;
	}

	private void updateCPU()
	{
		IAEItemStack send = finalOutput;

		if ( finalOutput != null && finalOutput.getStackSize() <= 0 )
			send = null;

		for (TileCraftingMonitorTile t : status)
			t.setJob( send );
	}

	public IGrid getGrid()
	{
		for (TileCraftingTile r : tiles)
		{
			IGridNode gn = r.getActionableNode();
			if ( gn != null )
			{
				IGrid g = gn.getGrid();
				if ( g != null )
					return r.getActionableNode().getGrid();
			}
		}

		return null;
	}

	private void completeJob()
	{
		if ( myLastLink != null )
			((CraftingLink) myLastLink).markDone();

		AELog.crafting( "marking job as complete" );
		isComplete = true;
	}

	private boolean canCraft(ICraftingPatternDetails details, IAEItemStack[] condencedInputs)
	{
		for (IAEItemStack g : condencedInputs)
		{

			if ( details.isCraftable() )
			{
				boolean found = false;

				for (IAEItemStack fuzz : inventory.getItemList().findFuzzy( g, FuzzyMode.IGNORE_ALL ))
				{
					fuzz = fuzz.copy();
					fuzz.setStackSize( g.getStackSize() );
					IAEItemStack ais = inventory.extractItems( fuzz, Actionable.SIMULATE, machineSrc );
					ItemStack is = ais == null ? null : ais.getItemStack();

					if ( is != null && is.stackSize == g.getStackSize() )
					{
						found = true;
						break;
					}
				}

				if ( !found )
					return false;
			}
			else
			{
				IAEItemStack ais = inventory.extractItems( g.copy(), Actionable.SIMULATE, machineSrc );
				ItemStack is = ais == null ? null : ais.getItemStack();

				if ( is == null || is.stackSize < g.getStackSize() )
					return false;
			}
		}

		return true;
	}

	public void cancel()
	{
		if ( myLastLink != null )
			myLastLink.cancel();

		IItemList<IAEItemStack> list;
		getListOfItem( list = AEApi.instance().storage().createItemList(), CraftingItemList.ALL );
		for (IAEItemStack g : list)
			postChange( g, machineSrc );

		isComplete = true;
		myLastLink = null;
		tasks.clear();
		waitingFor.resetStatus();

		finalOutput = null;
		updateCPU();

		storeItems(); // marks dirty
	}

	public void updateCraftingLogic(IGrid grid, IEnergyGrid eg, CraftingGridCache cc)
	{
		if ( !getCore().isActive() )
			return;

		if ( myLastLink != null )
		{
			if ( myLastLink.isCanceled() )
			{
				myLastLink = null;
				cancel();
			}
		}

		if ( isComplete )
		{
			if ( inventory.getItemList().isEmpty() )
				return;

			storeItems();
			return;
		}

		waiting = false;
		if ( waiting || tasks.isEmpty() ) // nothing to do here...
			return;

		int remainingOperations = accelerator + 1;
		boolean didsomething = false;

		Iterator<Entry<ICraftingPatternDetails, TaskProgress>> i = tasks.entrySet().iterator();
		while (i.hasNext())
		{
			Entry<ICraftingPatternDetails, TaskProgress> e = i.next();
			if ( e.getValue().value <= 0 )
			{
				i.remove();
				continue;
			}

			ICraftingPatternDetails details = e.getKey();
			if ( canCraft( details, details.getCondencedInputs() ) )
			{
				InventoryCrafting ic = null;

				for (ICraftingMedium m : cc.getMediums( e.getKey() ))
				{
					if ( e.getValue().value <= 0 )
						continue;

					if ( !m.isBusy() )
					{
						if ( ic == null )
						{
							IAEItemStack[] input = details.getInputs();

							double sum = 0;
							for (int x = 0; x < input.length; x++)
							{
								if ( input[x] != null )
									sum += input[x].getStackSize();
							}

							// power...
							if ( eg.extractAEPower( sum, Actionable.MODULATE, PowerMultiplier.CONFIG ) < sum - 0.01 )
								continue;

							ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
							boolean found = false;

							for (int x = 0; x < input.length; x++)
							{

								if ( input[x] != null )
								{
									found = false;
									if ( details.isCraftable() )
									{
										for (IAEItemStack fuzz : inventory.getItemList().findFuzzy( input[x], FuzzyMode.IGNORE_ALL ))
										{
											fuzz = fuzz.copy();
											fuzz.setStackSize( input[x].getStackSize() );
											IAEItemStack ais = inventory.extractItems( fuzz, Actionable.MODULATE, machineSrc );
											ItemStack is = ais == null ? null : ais.getItemStack();

											if ( is != null && details.isValidItemForSlot( x, is, getWorld() ) )
											{
												postChange( AEItemStack.create( is ), machineSrc );
												ic.setInventorySlotContents( x, is );
												found = true;
												break;
											}
										}
									}
									else
									{
										IAEItemStack ais = inventory.extractItems( input[x].copy(), Actionable.MODULATE, machineSrc );
										ItemStack is = ais == null ? null : ais.getItemStack();

										if ( is != null )
										{
											postChange( input[x], machineSrc );
											ic.setInventorySlotContents( x, is );
											found = true;
											continue;
										}
									}

									if ( !found )
										break;
								}

							}

							if ( !found )
							{
								// put stuff back..
								for (int x = 0; x < ic.getSizeInventory(); x++)
								{
									ItemStack is = ic.getStackInSlot( x );
									if ( is != null )
										inventory.injectItems( AEItemStack.create( is ), Actionable.MODULATE, machineSrc );
								}
								ic = null;
								break;
							}
						}

						if ( m.pushPattern( details, ic ) )
						{
							didsomething = true;
							remainingOperations--;

							for (IAEItemStack out : details.getCondencedOutputs())
							{
								postChange( out, machineSrc );
								waitingFor.add( out.copy() );
							}

							if ( details.isCraftable() )
							{
								FMLCommonHandler.instance().firePlayerCraftingEvent( Platform.getPlayer( (WorldServer) getWorld() ),
										details.getOutput( ic, getWorld() ), ic );

								for (int x = 0; x < ic.getSizeInventory(); x++)
								{
									ItemStack output = Platform.getContainerItem( ic.getStackInSlot( x ) );
									if ( output != null )
									{
										IAEItemStack cItem = AEItemStack.create( output );
										postChange( cItem, machineSrc );
										waitingFor.add( cItem );
									}
								}
							}

							ic = null; // hand off complete!
							markDirty();

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
						{
							inventory.injectItems( AEItemStack.create( is ), Actionable.MODULATE, machineSrc );
						}
					}
				}
			}
		}

		if ( remainingOperations > 0 && didsomething == false )
			waiting = true;
	}

	private void storeItems()
	{
		IGrid g = getGrid();
		if ( g == null )
			return;

		IStorageGrid sg = g.getCache( IStorageGrid.class );
		IMEInventory<IAEItemStack> ii = sg.getItemInventory();

		for (IAEItemStack is : inventory.getItemList())
		{
			is = inventory.extractItems( is.copy(), Actionable.MODULATE, machineSrc );

			if ( is != null )
			{
				postChange( is, machineSrc );
				is = ii.injectItems( is, Actionable.MODULATE, machineSrc );
			}

			if ( is != null )
				inventory.injectItems( is, Actionable.MODULATE, machineSrc );
		}

		if ( inventory.getItemList().isEmpty() )
			inventory = new MECraftingInventory();

		markDirty();
	}

	private World getWorld()
	{
		return getCore().getWorldObj();
	}

	public ICraftingLink submitJob(IGrid g, ICraftingJob job, BaseActionSource src, ICraftingRequester requestingMachine)
	{
		if ( !tasks.isEmpty() || !waitingFor.isEmpty() )
			return null;

		if ( !(job instanceof CraftingJob) )
			return null;

		if ( isBusy() || getAvailableStorage() < job.getByteTotal() )
			return null;

		IStorageGrid sg = g.getCache( IStorageGrid.class );
		IMEInventory<IAEItemStack> storage = sg.getItemInventory();
		MECraftingInventory ci = new MECraftingInventory( storage, true, false, false );

		try
		{
			waitingFor.resetStatus();
			((CraftingJob) job).tree.setJob( ci, this, src );
			ci.commit( src );
			finalOutput = job.getOutput();
			waiting = false;
			isComplete = false;
			markDirty();

			updateCPU();
			String craftID = generateCraftingID();

			myLastLink = new CraftingLink( generateLinkData( craftID, requestingMachine == null, false ), this );

			if ( requestingMachine == null )
				return myLastLink;

			ICraftingLink whatLink = new CraftingLink( generateLinkData( craftID, requestingMachine == null, true ), requestingMachine );

			submitLink( myLastLink );
			submitLink( whatLink );

			IItemList<IAEItemStack> list;
			getListOfItem( list = AEApi.instance().storage().createItemList(), CraftingItemList.ALL );
			for (IAEItemStack ge : list)
				postChange( ge, machineSrc );

			return whatLink;
		}
		catch (CraftBranchFailure e)
		{
			tasks.clear();
			inventory.getItemList().resetStatus();
			// AELog.error( e );
		}

		return null;
	}

	private void submitLink(ICraftingLink myLastLink2)
	{
		if ( getGrid() != null )
		{
			CraftingGridCache cc = getGrid().getCache( ICraftingGrid.class );
			cc.addLink( (CraftingLink) myLastLink2 );
		}
	}

	private String generateCraftingID()
	{
		long now = System.currentTimeMillis();
		int hash = System.identityHashCode( this );
		int hmm = finalOutput == null ? 0 : finalOutput.hashCode();

		return Long.toString( now, Character.MAX_RADIX ) + "-" + Integer.toString( hash, Character.MAX_RADIX ) + "-"
				+ Integer.toString( hmm, Character.MAX_RADIX );
	}

	private NBTTagCompound generateLinkData(String craftingID, boolean standalone, boolean req)
	{
		NBTTagCompound tag = new NBTTagCompound();

		tag.setString( "CraftID", craftingID );
		tag.setBoolean( "canceled", false );
		tag.setBoolean( "done", false );
		tag.setBoolean( "standalone", standalone );
		tag.setBoolean( "req", req );

		return tag;
	}

	private void markDirty()
	{
		getCore().markDirty();
	}

	private TileCraftingTile getCore()
	{
		return (TileCraftingTile) machineSrc.via;
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

	@Override
	public boolean isBusy()
	{
		Iterator<Entry<ICraftingPatternDetails, TaskProgress>> i = tasks.entrySet().iterator();
		while (i.hasNext())
		{
			if ( i.next().getValue().value <= 0 )
				i.remove();
		}

		return !tasks.isEmpty() || !waitingFor.isEmpty();
	}

	public IAEItemStack getItemStack(IAEItemStack what, CraftingItemList storage2)
	{
		IAEItemStack is = null;
		switch (storage2)
		{
		case STORAGE:
			is = inventory.getItemList().findPrecise( what );
			break;
		case ACTIVE:
			is = waitingFor.findPrecise( what );
			break;
		case PENDING:

			is = what.copy();
			is.setStackSize( 0 );

			for (Entry<ICraftingPatternDetails, TaskProgress> t : tasks.entrySet())
			{
				for (IAEItemStack ais : t.getKey().getCondencedOutputs())
				{
					if ( ais.equals( is ) )
						is.setStackSize( is.getStackSize() + ais.getStackSize() * t.getValue().value );
				}
			}

			break;
		default:
		case ALL:
			throw new RuntimeException( "Invalid Operation" );
		}

		if ( is != null )
			return is.copy();

		is = what.copy();
		is.setStackSize( 0 );
		return is;
	}

	public void readFromNBT(NBTTagCompound data)
	{
		finalOutput = AEItemStack.loadItemStackFromNBT( (NBTTagCompound) data.getTag( "finalOutput" ) );
		for (IAEItemStack ais : readList( (NBTTagList) data.getTag( "inventory" ) ))
			inventory.injectItems( ais, Actionable.MODULATE, machineSrc );

		waiting = data.getBoolean( "waiting" );
		isComplete = data.getBoolean( "isComplete" );

		if ( data.hasKey( "link" ) )
		{
			NBTTagCompound link = data.getCompoundTag( "link" );
			myLastLink = new CraftingLink( link, this );
			submitLink( myLastLink );
		}

		NBTTagList list = data.getTagList( "tasks", 10 );
		for (int x = 0; x < list.tagCount(); x++)
		{
			NBTTagCompound item = list.getCompoundTagAt( x );
			IAEItemStack pattern = AEItemStack.loadItemStackFromNBT( item );
			if ( pattern != null && pattern.getItem() instanceof ICraftingPatternItem )
			{
				ICraftingPatternItem cpi = (ICraftingPatternItem) pattern.getItem();
				ICraftingPatternDetails details = cpi.getPatternForItem( pattern.getItemStack(), getWorld() );
				if ( details != null )
				{
					TaskProgress tp = new TaskProgress();
					tp.value = item.getLong( "craftingProgress" );
					tasks.put( details, tp );
				}
			}
		}

		waitingFor = readList( (NBTTagList) data.getTag( "waitingFor" ) );
	}

	public void writeToNBT(NBTTagCompound data)
	{
		data.setTag( "finalOutput", writeItem( finalOutput ) );
		data.setTag( "inventory", writeList( inventory.getItemList() ) );
		data.setBoolean( "waiting", waiting );
		data.setBoolean( "isComplete", isComplete );

		if ( myLastLink != null )
		{
			NBTTagCompound link = new NBTTagCompound();
			myLastLink.writeToNBT( link );
			data.setTag( "link", link );
		}

		NBTTagList list = new NBTTagList();
		for (Entry<ICraftingPatternDetails, TaskProgress> e : tasks.entrySet())
		{
			NBTTagCompound item = writeItem( AEItemStack.create( e.getKey().getPattern() ) );
			item.setLong( "craftingProgress", e.getValue().value );
			list.appendTag( item );
		}
		data.setTag( "tasks", list );

		data.setTag( "waitingFor", writeList( waitingFor ) );
	}

	private IItemList<IAEItemStack> readList(NBTTagList tag)
	{
		IItemList<IAEItemStack> out = AEApi.instance().storage().createItemList();
		if ( tag == null )
			return out;

		for (int x = 0; x < tag.tagCount(); x++)
		{
			IAEItemStack ais = AEItemStack.loadItemStackFromNBT( tag.getCompoundTagAt( x ) );
			if ( ais != null )
				out.add( ais );
		}

		return out;
	}

	private NBTTagList writeList(IItemList<IAEItemStack> myList)
	{
		NBTTagList out = new NBTTagList();

		for (IAEItemStack ais : myList)
			out.appendTag( writeItem( ais ) );

		return out;
	}

	private NBTTagCompound writeItem(IAEItemStack finalOutput2)
	{
		NBTTagCompound out = new NBTTagCompound();

		if ( finalOutput2 != null )
			finalOutput2.writeToNBT( out );

		return out;
	}

	public void done()
	{
		TileCraftingTile core = getCore();

		core.isCoreBlock = true;

		if ( core.previousState != null )
		{
			readFromNBT( core.previousState );
			core.previousState = null;
		}

		updateCPU();
	}

	@Override
	public BaseActionSource getActionSource()
	{
		return machineSrc;
	}

}
