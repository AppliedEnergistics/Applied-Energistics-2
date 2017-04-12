/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.helpers;


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.storage.MEMonitorIInventory;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.me.storage.NullInventory;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.WrapperInvSlot;
import appeng.util.item.AEItemStack;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;


public class DualityInterface
		implements IGridTickable, IStorageMonitorable, IInventoryDestination, IAEAppEngInventory, IConfigManagerHost, ICraftingProvider, IUpgradeableHost, IPriorityHost
{

	public static final int NUMBER_OF_STORAGE_SLOTS = 9;
	public static final int NUMBER_OF_CONFIG_SLOTS = 9;
	public static final int NUMBER_OF_PATTERN_SLOTS = 9;

	private static final Collection<Block> BAD_BLOCKS = new HashSet<Block>( 100 );
	private final int[] sides = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private final IAEItemStack[] requireWork = { null, null, null, null, null, null, null, null, null };
	private final MultiCraftingTracker craftingTracker;
	private final AENetworkProxy gridProxy;
	private final IInterfaceHost iHost;
	private final BaseActionSource mySource;
	private final BaseActionSource interfaceRequestSource;
	private final ConfigManager cm = new ConfigManager( this );
	private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, NUMBER_OF_CONFIG_SLOTS );
	private final AppEngInternalInventory storage = new AppEngInternalInventory( this, NUMBER_OF_STORAGE_SLOTS );
	private final AppEngInternalInventory patterns = new AppEngInternalInventory( this, NUMBER_OF_PATTERN_SLOTS );
	private final WrapperInvSlot slotInv = new WrapperInvSlot( this.storage );
	private final MEMonitorPassThrough<IAEItemStack> items = new MEMonitorPassThrough<IAEItemStack>( new NullInventory<IAEItemStack>(), StorageChannel.ITEMS );
	private final MEMonitorPassThrough<IAEFluidStack> fluids = new MEMonitorPassThrough<IAEFluidStack>( new NullInventory<IAEFluidStack>(), StorageChannel.FLUIDS );
	private final UpgradeInventory upgrades;
	private boolean hasConfig = false;
	private int priority;
	private List<ICraftingPatternDetails> craftingList = null;
	private List<ItemStack> waitingToSend = null;
	private IMEInventory<IAEItemStack> destination;
	private boolean isWorking = false;

	public DualityInterface( final AENetworkProxy networkProxy, final IInterfaceHost ih )
	{
		this.gridProxy = networkProxy;
		this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );

		this.upgrades = new StackUpgradeInventory( this.gridProxy.getMachineRepresentation(), this, 1 );
		this.cm.registerSetting( Settings.BLOCK, YesNo.NO );
		this.cm.registerSetting( Settings.INTERFACE_TERMINAL, YesNo.YES );

		this.iHost = ih;
		this.craftingTracker = new MultiCraftingTracker( this.iHost, 9 );

		final MachineSource actionSource = new MachineSource( this.iHost );
		this.mySource = actionSource;
		this.fluids.setChangeSource( actionSource );
		this.items.setChangeSource( actionSource );

		this.interfaceRequestSource = new InterfaceRequestSource( this.iHost );
	}

	@Override
	public void saveChanges()
	{
		this.iHost.saveChanges();
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( this.isWorking )
		{
			return;
		}

		if( inv == this.config )
		{
			this.readConfig();
		}
		else if( inv == this.patterns && ( removed != null || added != null ) )
		{
			this.updateCraftingList();
		}
		else if( inv == this.storage && slot >= 0 )
		{
			final boolean had = this.hasWorkToDo();

			this.updatePlan( slot );

			final boolean now = this.hasWorkToDo();

			if( had != now )
			{
				try
				{
					if( now )
					{
						this.gridProxy.getTick().alertDevice( this.gridProxy.getNode() );
					}
					else
					{
						this.gridProxy.getTick().sleepDevice( this.gridProxy.getNode() );
					}
				}
				catch( final GridAccessException e )
				{
					// :P
				}
			}
		}
	}

	public void writeToNBT( final NBTTagCompound data )
	{
		this.config.writeToNBT( data, "config" );
		this.patterns.writeToNBT( data, "patterns" );
		this.storage.writeToNBT( data, "storage" );
		this.upgrades.writeToNBT( data, "upgrades" );
		this.cm.writeToNBT( data );
		this.craftingTracker.writeToNBT( data );
		data.setInteger( "priority", this.priority );

		final NBTTagList waitingToSend = new NBTTagList();
		if( this.waitingToSend != null )
		{
			for( final ItemStack is : this.waitingToSend )
			{
				final NBTTagCompound item = new NBTTagCompound();
				is.writeToNBT( item );
				waitingToSend.appendTag( item );
			}
		}
		data.setTag( "waitingToSend", waitingToSend );
	}

	public void readFromNBT( final NBTTagCompound data )
	{
		this.waitingToSend = null;
		final NBTTagList waitingList = data.getTagList( "waitingToSend", 10 );
		if( waitingList != null )
		{
			for( int x = 0; x < waitingList.tagCount(); x++ )
			{
				final NBTTagCompound c = waitingList.getCompoundTagAt( x );
				if( c != null )
				{
					final ItemStack is = ItemStack.loadItemStackFromNBT( c );
					this.addToSendList( is );
				}
			}
		}

		this.craftingTracker.readFromNBT( data );
		this.upgrades.readFromNBT( data, "upgrades" );
		this.config.readFromNBT( data, "config" );
		this.patterns.readFromNBT( data, "patterns" );
		this.storage.readFromNBT( data, "storage" );
		this.priority = data.getInteger( "priority" );
		this.cm.readFromNBT( data );
		this.readConfig();
		this.updateCraftingList();
	}

	private void addToSendList( final ItemStack is )
	{
		if( is == null )
		{
			return;
		}

		if( this.waitingToSend == null )
		{
			this.waitingToSend = new LinkedList<ItemStack>();
		}

		this.waitingToSend.add( is );

		try
		{
			this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	private void readConfig()
	{
		this.hasConfig = false;

		for( final ItemStack p : this.config )
		{
			if( p != null )
			{
				this.hasConfig = true;
				break;
			}
		}

		final boolean had = this.hasWorkToDo();

		for( int x = 0; x < NUMBER_OF_CONFIG_SLOTS; x++ )
		{
			this.updatePlan( x );
		}

		final boolean has = this.hasWorkToDo();

		if( had != has )
		{
			try
			{
				if( has )
				{
					this.gridProxy.getTick().alertDevice( this.gridProxy.getNode() );
				}
				else
				{
					this.gridProxy.getTick().sleepDevice( this.gridProxy.getNode() );
				}
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}

		this.notifyNeighbors();
	}

	private void updateCraftingList()
	{
		final Boolean[] accountedFor = { false, false, false, false, false, false, false, false, false }; // 9...

		assert ( accountedFor.length == this.patterns.getSizeInventory() );

		if( !this.gridProxy.isReady() )
		{
			return;
		}

		if( this.craftingList != null )
		{
			final Iterator<ICraftingPatternDetails> i = this.craftingList.iterator();
			while( i.hasNext() )
			{
				final ICraftingPatternDetails details = i.next();
				boolean found = false;

				for( int x = 0; x < accountedFor.length; x++ )
				{
					final ItemStack is = this.patterns.getStackInSlot( x );
					if( details.getPattern() == is )
					{
						accountedFor[x] = found = true;
					}
				}

				if( !found )
				{
					i.remove();
				}
			}
		}

		for( int x = 0; x < accountedFor.length; x++ )
		{
			if( !accountedFor[x] )
			{
				this.addToCraftingList( this.patterns.getStackInSlot( x ) );
			}
		}

		try
		{
			this.gridProxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, this.gridProxy.getNode() ) );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	private boolean hasWorkToDo()
	{
		if( this.hasItemsToSend() )
		{
			return true;
		}
		else
		{
			for( final IAEItemStack requiredWork : this.requireWork )
			{
				if( requiredWork != null )
				{
					return true;
				}
			}

			return false;
		}
	}

	private void updatePlan( final int slot )
	{
		IAEItemStack req = this.config.getAEStackInSlot( slot );
		if( req != null && req.getStackSize() <= 0 )
		{
			this.config.setInventorySlotContents( slot, null );
			req = null;
		}

		final ItemStack Stored = this.storage.getStackInSlot( slot );

		if( req == null && Stored != null )
		{
			final IAEItemStack work = AEApi.instance().storage().createItemStack( Stored );
			this.requireWork[slot] = work.setStackSize( -work.getStackSize() );
			return;
		}
		else if( req != null )
		{
			if( Stored == null ) // need to add stuff!
			{
				this.requireWork[slot] = req.copy();
				return;
			}
			else if( req.isSameType( Stored ) ) // same type ( qty different? )!
			{
				if( req.getStackSize() != Stored.stackSize )
				{
					this.requireWork[slot] = req.copy();
					this.requireWork[slot].setStackSize( req.getStackSize() - Stored.stackSize );
					return;
				}
			}
			else
			// Stored != null; dispose!
			{
				final IAEItemStack work = AEApi.instance().storage().createItemStack( Stored );
				this.requireWork[slot] = work.setStackSize( -work.getStackSize() );
				return;
			}
		}

		// else

		this.requireWork[slot] = null;
	}

	public void notifyNeighbors()
	{
		if( this.gridProxy.isActive() )
		{
			try
			{
				this.gridProxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, this.gridProxy.getNode() ) );
				this.gridProxy.getTick().wakeDevice( this.gridProxy.getNode() );
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}

		final TileEntity te = this.iHost.getTileEntity();
		if( te != null && te.getWorldObj() != null )
		{
			Platform.notifyBlocksOfNeighbors( te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord );
		}
	}

	private void addToCraftingList( final ItemStack is )
	{
		if( is == null )
		{
			return;
		}

		if( is.getItem() instanceof ICraftingPatternItem )
		{
			final ICraftingPatternItem cpi = (ICraftingPatternItem) is.getItem();
			final ICraftingPatternDetails details = cpi.getPatternForItem( is, this.iHost.getTileEntity().getWorldObj() );

			if( details != null )
			{
				if( this.craftingList == null )
				{
					this.craftingList = new LinkedList<ICraftingPatternDetails>();
				}

				this.craftingList.add( details );
			}
		}
	}

	private boolean hasItemsToSend()
	{
		return this.waitingToSend != null && !this.waitingToSend.isEmpty();
	}

	@Override
	public boolean canInsert( final ItemStack stack )
	{
		final IAEItemStack out = this.destination.injectItems( AEApi.instance().storage().createItemStack( stack ), Actionable.SIMULATE, null );
		if( out == null )
		{
			return true;
		}
		return out.getStackSize() != stack.stackSize;
		// ItemStack after = adaptor.simulateAdd( stack );
		// if ( after == null )
		// return true;
		// return after.stackSize != stack.stackSize;
	}

	public IInventory getConfig()
	{
		return this.config;
	}

	public IInventory getPatterns()
	{
		return this.patterns;
	}

	public void gridChanged()
	{
		try
		{
			this.items.setInternal( this.gridProxy.getStorage().getItemInventory() );
			this.fluids.setInternal( this.gridProxy.getStorage().getFluidInventory() );
		}
		catch( final GridAccessException gae )
		{
			this.items.setInternal( new NullInventory<IAEItemStack>() );
			this.fluids.setInternal( new NullInventory<IAEFluidStack>() );
		}

		this.notifyNeighbors();
	}

	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.SMART;
	}

	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this.iHost.getTileEntity() );
	}

	public IInventory getInternalInventory()
	{
		return this.storage;
	}

	public void markDirty()
	{
		for( int slot = 0; slot < this.storage.getSizeInventory(); slot++ )
		{
			this.onChangeInventory( this.storage, slot, InvOperation.markDirty, null, null );
		}
	}

	public int[] getAccessibleSlotsFromSide( final int side )
	{
		return this.sides;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.Interface.getMin(), TickRates.Interface.getMax(), !this.hasWorkToDo(), true );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		if( !this.gridProxy.isActive() )
		{
			return TickRateModulation.SLEEP;
		}

		if( this.hasItemsToSend() )
		{
			this.pushItemsOut( this.iHost.getTargets() );
		}

		final boolean couldDoWork = this.updateStorage();
		return this.hasWorkToDo() ? ( couldDoWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER ) : TickRateModulation.SLEEP;
	}

	private void pushItemsOut( final EnumSet<ForgeDirection> possibleDirections )
	{
		if( !this.hasItemsToSend() )
		{
			return;
		}

		final TileEntity tile = this.iHost.getTileEntity();
		final World w = tile.getWorldObj();

		final Iterator<ItemStack> i = this.waitingToSend.iterator();
		while( i.hasNext() )
		{
			ItemStack whatToSend = i.next();

			for( final ForgeDirection s : possibleDirections )
			{
				final TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );
				if( te == null )
				{
					continue;
				}

				final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, s.getOpposite() );
				if( ad != null )
				{
					final ItemStack Result = ad.addItems( whatToSend );

					if( Result == null )
					{
						whatToSend = null;
					}
					else
					{
						whatToSend.stackSize -= whatToSend.stackSize - Result.stackSize;
					}

					if( whatToSend == null )
					{
						break;
					}
				}
			}

			if( whatToSend == null )
			{
				i.remove();
			}
		}

		if( this.waitingToSend.isEmpty() )
		{
			this.waitingToSend = null;
		}
	}

	private boolean updateStorage()
	{
		boolean didSomething = false;

		for( int x = 0; x < NUMBER_OF_STORAGE_SLOTS; x++ )
		{
			if( this.requireWork[x] != null )
			{
				didSomething = this.usePlan( x, this.requireWork[x] ) || didSomething;
			}
		}

		return didSomething;
	}

	private boolean usePlan( final int x, final IAEItemStack itemStack )
	{
		final InventoryAdaptor adaptor = this.getAdaptor( x );
		this.isWorking = true;

		boolean changed = false;
		try
		{
			this.destination = this.gridProxy.getStorage().getItemInventory();
			final IEnergySource src = this.gridProxy.getEnergy();

			if( this.craftingTracker.isBusy( x ) )
			{
				changed = this.handleCrafting( x, adaptor, itemStack ) || changed;
			}
			else if( itemStack.getStackSize() > 0 )
			{
				// make sure strange things didn't happen...
				if( adaptor.simulateAdd( itemStack.getItemStack() ) != null )
				{
					changed = true;
					throw new GridAccessException();
				}

				final IAEItemStack acquired = Platform.poweredExtraction( src, this.destination, itemStack, this.interfaceRequestSource );
				if( acquired != null )
				{
					changed = true;
					final ItemStack issue = adaptor.addItems( acquired.getItemStack() );
					if( issue != null )
					{
						throw new IllegalStateException( "bad attempt at managing inventory. ( addItems )" );
					}
				}
				else
				{
					changed = this.handleCrafting( x, adaptor, itemStack ) || changed;
				}
			}
			else if( itemStack.getStackSize() < 0 )
			{
				IAEItemStack toStore = itemStack.copy();
				toStore.setStackSize( -toStore.getStackSize() );

				long diff = toStore.getStackSize();

				// make sure strange things didn't happen...
				final ItemStack canExtract = adaptor.simulateRemove( (int) diff, toStore.getItemStack(), null );
				if( canExtract == null || canExtract.stackSize != diff )
				{
					changed = true;
					throw new GridAccessException();
				}

				toStore = Platform.poweredInsert( src, this.destination, toStore, this.interfaceRequestSource );

				if( toStore != null )
				{
					diff -= toStore.getStackSize();
				}

				if( diff != 0 )
				{
					// extract items!
					changed = true;
					final ItemStack removed = adaptor.removeItems( (int) diff, null, null );
					if( removed == null )
					{
						throw new IllegalStateException( "bad attempt at managing inventory. ( removeItems )" );
					}
					else if( removed.stackSize != diff )
					{
						throw new IllegalStateException( "bad attempt at managing inventory. ( removeItems )" );
					}
				}
			}
			// else wtf?
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		if( changed )
		{
			this.updatePlan( x );
		}

		this.isWorking = false;
		return changed;
	}

	private InventoryAdaptor getAdaptor( final int slot )
	{
		return new AdaptorIInventory( this.slotInv.getWrapper( slot ) );
	}

	private boolean handleCrafting( final int x, final InventoryAdaptor d, final IAEItemStack itemStack )
	{
		try
		{
			if( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 && itemStack != null )
			{
				return this.craftingTracker.handleCrafting( x, itemStack.getStackSize(), itemStack, d, this.iHost.getTileEntity().getWorldObj(), this.gridProxy.getGrid(), this.gridProxy.getCrafting(), this.mySource );
			}
		}
		catch( final GridAccessException e )
		{
			// :P
		}

		return false;
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		if( this.upgrades == null )
		{
			return 0;
		}
		return this.upgrades.getInstalledUpgrades( u );
	}

	@Override
	public TileEntity getTile()
	{
		return (TileEntity) ( this.iHost instanceof TileEntity ? this.iHost : null );
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		if( this.hasConfig() )
		{
			return new InterfaceInventory( this );
		}

		return this.items;
	}

	private boolean hasConfig()
	{
		return this.hasConfig;
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "storage" ) )
		{
			return this.storage;
		}

		if( name.equals( "patterns" ) )
		{
			return this.patterns;
		}

		if( name.equals( "config" ) )
		{
			return this.config;
		}

		if( name.equals( "upgrades" ) )
		{
			return this.upgrades;
		}

		return null;
	}

	public IInventory getStorage()
	{
		return this.storage;
	}

	@Override
	public appeng.api.util.IConfigManager getConfigManager()
	{
		return this.cm;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
		if( this.getInstalledUpgrades( Upgrades.CRAFTING ) == 0 )
		{
			this.cancelCrafting();
		}

		this.markDirty();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		if( this.hasConfig() )
		{
			return null;
		}

		return this.fluids;
	}

	private void cancelCrafting()
	{
		this.craftingTracker.cancel();
	}

	public IStorageMonitorable getMonitorable( final ForgeDirection side, final BaseActionSource src, final IStorageMonitorable myInterface )
	{
		if( Platform.canAccess( this.gridProxy, src ) )
		{
			return myInterface;
		}

		final DualityInterface di = this;

		return new IStorageMonitorable()
		{

			@Override
			public IMEMonitor<IAEItemStack> getItemInventory()
			{
				return new InterfaceInventory( di );
			}

			@Override
			public IMEMonitor<IAEFluidStack> getFluidInventory()
			{
				return null;
			}
		};
	}

	@Override
	public boolean pushPattern( final ICraftingPatternDetails patternDetails, final InventoryCrafting table )
	{
		if( this.hasItemsToSend() || !this.gridProxy.isActive() || !this.craftingList.contains( patternDetails ) )
		{
			return false;
		}

		final TileEntity tile = this.iHost.getTileEntity();
		final World w = tile.getWorldObj();

		final EnumSet<ForgeDirection> possibleDirections = this.iHost.getTargets();
		for( final ForgeDirection s : possibleDirections )
		{
			final TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );
			if( te instanceof IInterfaceHost )
			{
				try
				{
					if( ( (IInterfaceHost) te ).getInterfaceDuality().sameGrid( this.gridProxy.getGrid() ) )
					{
						continue;
					}
				}
				catch( final GridAccessException e )
				{
					continue;
				}
			}

			if( te instanceof ICraftingMachine )
			{
				final ICraftingMachine cm = (ICraftingMachine) te;
				if( cm.acceptsPlans() )
				{
					if( cm.pushPattern( patternDetails, table, s.getOpposite() ) )
					{
						return true;
					}
					continue;
				}
			}

			final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, s.getOpposite() );
			if( ad != null )
			{
				if( this.isBlocking() )
				{
					if( ad.simulateRemove( 1, null, null ) != null )
					{
						continue;
					}
				}

				if( this.acceptsItems( ad, table ) )
				{
					for( int x = 0; x < table.getSizeInventory(); x++ )
					{
						final ItemStack is = table.getStackInSlot( x );
						if( is != null )
						{
							final ItemStack added = ad.addItems( is );
							this.addToSendList( added );
						}
					}
					this.pushItemsOut( possibleDirections );
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isBusy()
	{
		if( this.hasItemsToSend() )
		{
			return true;
		}

		boolean busy = false;

		if( this.isBlocking() )
		{
			final EnumSet<ForgeDirection> possibleDirections = this.iHost.getTargets();
			final TileEntity tile = this.iHost.getTileEntity();
			final World w = tile.getWorldObj();

			boolean allAreBusy = true;

			for( final ForgeDirection s : possibleDirections )
			{
				final TileEntity te = w.getTileEntity( tile.xCoord + s.offsetX, tile.yCoord + s.offsetY, tile.zCoord + s.offsetZ );

				final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, s.getOpposite() );
				if( ad != null )
				{
					if( ad.simulateRemove( 1, null, null ) == null )
					{
						allAreBusy = false;
						break;
					}
				}
			}

			busy = allAreBusy;
		}

		return busy;
	}

	private boolean sameGrid( final IGrid grid ) throws GridAccessException
	{
		return grid == this.gridProxy.getGrid();
	}

	private boolean isBlocking()
	{
		return this.cm.getSetting( Settings.BLOCK ) == YesNo.YES;
	}

	private boolean acceptsItems( final InventoryAdaptor ad, final InventoryCrafting table )
	{
		for( int x = 0; x < table.getSizeInventory(); x++ )
		{
			final ItemStack is = table.getStackInSlot( x );
			if( is == null )
			{
				continue;
			}

			if( ad.simulateAdd( is.copy() ) != null )
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void provideCrafting( final ICraftingProviderHelper craftingTracker )
	{
		if( this.gridProxy.isActive() && this.craftingList != null )
		{
			for( final ICraftingPatternDetails details : this.craftingList )
			{
				details.setPriority( this.priority );
				craftingTracker.addCraftingOption( this, details );
			}
		}
	}

	public void addDrops( final List<ItemStack> drops )
	{
		if( this.waitingToSend != null )
		{
			for( final ItemStack is : this.waitingToSend )
			{
				if( is != null )
				{
					drops.add( is );
				}
			}
		}

		for( final ItemStack is : this.upgrades )
		{
			if( is != null )
			{
				drops.add( is );
			}
		}

		for( final ItemStack is : this.storage )
		{
			if( is != null )
			{
				drops.add( is );
			}
		}

		for( final ItemStack is : this.patterns )
		{
			if( is != null )
			{
				drops.add( is );
			}
		}
	}

	public IUpgradeableHost getHost()
	{
		if( this.getPart() instanceof IUpgradeableHost )
		{
			return (IUpgradeableHost) this.getPart();
		}
		if( this.getTile() instanceof IUpgradeableHost )
		{
			return (IUpgradeableHost) this.getTile();
		}
		return null;
	}

	private IPart getPart()
	{
		return (IPart) ( this.iHost instanceof IPart ? this.iHost : null );
	}

	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return this.craftingTracker.getRequestedJobs();
	}

	public IAEItemStack injectCraftedItems( final ICraftingLink link, final IAEItemStack acquired, final Actionable mode )
	{
		final int slot = this.craftingTracker.getSlot( link );

		if( acquired != null && slot >= 0 && slot <= this.requireWork.length )
		{
			final InventoryAdaptor adaptor = this.getAdaptor( slot );

			if( mode == Actionable.SIMULATE )
			{
				return AEItemStack.create( adaptor.simulateAdd( acquired.getItemStack() ) );
			}
			else
			{
				final IAEItemStack is = AEItemStack.create( adaptor.addItems( acquired.getItemStack() ) );
				this.updatePlan( slot );
				return is;
			}
		}

		return acquired;
	}

	public void jobStateChange( final ICraftingLink link )
	{
		this.craftingTracker.jobStateChange( link );
	}

	public String getTermName()
	{
		final TileEntity hostTile = this.iHost.getTileEntity();
		final World hostWorld = hostTile.getWorldObj();

		if( ( (ICustomNameObject) this.iHost ).hasCustomName() )
		{
			return ( (ICustomNameObject) this.iHost ).getCustomName();
		}

		final EnumSet<ForgeDirection> possibleDirections = this.iHost.getTargets();
		for( final ForgeDirection direction : possibleDirections )
		{
			final int xPos = hostTile.xCoord + direction.offsetX;
			final int yPos = hostTile.yCoord + direction.offsetY;
			final int zPos = hostTile.zCoord + direction.offsetZ;
			final TileEntity directedTile = hostWorld.getTileEntity( xPos, yPos, zPos );

			if( directedTile == null )
			{
				continue;
			}

			if( directedTile instanceof IInterfaceHost )
			{
				try
				{
					if( ( (IInterfaceHost) directedTile ).getInterfaceDuality().sameGrid( this.gridProxy.getGrid() ) )
					{
						continue;
					}
				}
				catch( final GridAccessException e )
				{
					continue;
				}
			}

			final InventoryAdaptor adaptor = InventoryAdaptor.getAdaptor( directedTile, direction.getOpposite() );
			if( directedTile instanceof ICraftingMachine || adaptor != null )
			{
				if( directedTile instanceof IInventory && ( (IInventory) directedTile ).getSizeInventory() == 0 )
				{
					continue;
				}

				if( directedTile instanceof ISidedInventory )
				{
					final int[] sides = ( (ISidedInventory) directedTile ).getAccessibleSlotsFromSide( direction.getOpposite().ordinal() );

					if( sides == null || sides.length == 0 )
					{
						continue;
					}
				}

				final Block directedBlock = hostWorld.getBlock( xPos, yPos, zPos );
				ItemStack what = new ItemStack( directedBlock, 1, directedBlock.getDamageValue( hostWorld, xPos, yPos, zPos ) );
				try
				{
					Vec3 from = Vec3.createVectorHelper( hostTile.xCoord + 0.5, hostTile.yCoord + 0.5, hostTile.zCoord + 0.5 );
					from = from.addVector( direction.offsetX * 0.501, direction.offsetY * 0.501, direction.offsetZ * 0.501 );
					final Vec3 to = from.addVector( direction.offsetX, direction.offsetY, direction.offsetZ );
					final MovingObjectPosition mop = hostWorld.rayTraceBlocks( from, to, true );
					if( mop != null && !BAD_BLOCKS.contains( directedBlock ) )
					{
						if( mop.blockX == directedTile.xCoord && mop.blockY == directedTile.yCoord && mop.blockZ == directedTile.zCoord )
						{
							final ItemStack g = directedBlock.getPickBlock( mop, hostWorld, directedTile.xCoord, directedTile.yCoord, directedTile.zCoord, null );
							if( g != null )
							{
								what = g;
							}
						}
					}
				}
				catch( final Throwable t )
				{
					BAD_BLOCKS.add( directedBlock ); // nope!
				}

				if( what.getItem() != null )
				{
					return what.getUnlocalizedName();
				}

				final Item item = Item.getItemFromBlock( directedBlock );
				if( item == null )
				{
					return directedBlock.getUnlocalizedName();
				}
			}
		}

		return "Nothing";
	}

	public long getSortValue()
	{
		final TileEntity te = this.iHost.getTileEntity();
		return ( te.zCoord << 24 ) ^ ( te.xCoord << 8 ) ^ te.yCoord;
	}

	public void initialize()
	{
		this.updateCraftingList();
	}

	@Override
	public int getPriority()
	{
		return this.priority;
	}

	@Override
	public void setPriority( final int newValue )
	{
		this.priority = newValue;
		this.markDirty();

		try
		{
			this.gridProxy.getGrid().postEvent( new MENetworkCraftingPatternChange( this, this.gridProxy.getNode() ) );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	private class InterfaceRequestSource extends MachineSource
	{

		public InterfaceRequestSource( final IActionHost v )
		{
			super( v );
		}
	}


	private class InterfaceInventory extends MEMonitorIInventory
	{

		public InterfaceInventory( final DualityInterface tileInterface )
		{
			super( new AdaptorIInventory( tileInterface.storage ) );
			this.setActionSource( new MachineSource( DualityInterface.this.iHost ) );
		}

		@Override
		public IAEItemStack injectItems( final IAEItemStack input, final Actionable type, final BaseActionSource src )
		{
			if( src instanceof InterfaceRequestSource )
			{
				return input;
			}

			return super.injectItems( input, type, src );
		}

		@Override
		public IAEItemStack extractItems( final IAEItemStack request, final Actionable type, final BaseActionSource src )
		{
			if( src instanceof InterfaceRequestSource )
			{
				return null;
			}

			return super.extractItems( request, type, src );
		}
	}
}
