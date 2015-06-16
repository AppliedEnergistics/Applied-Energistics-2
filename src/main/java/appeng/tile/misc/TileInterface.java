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

package appeng.tile.misc;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;

import com.google.common.collect.ImmutableSet;


public class TileInterface extends AENetworkInvTile implements IGridTickable, ITileStorageMonitorable, IStorageMonitorable, IInventoryDestination, IInterfaceHost, IPriorityHost
{

	final DualityInterface duality = new DualityInterface( this.gridProxy, this );
	AEPartLocation pointAt = AEPartLocation.INTERNAL;

	@MENetworkEventSubscribe
	public void stateChange( MENetworkChannelsChanged c )
	{
		this.duality.notifyNeighbors();
	}

	@MENetworkEventSubscribe
	public void stateChange( MENetworkPowerStatusChange c )
	{
		this.duality.notifyNeighbors();
	}

	public void setSide( AEPartLocation axis )
	{
		if( Platform.isClient() )
		{
			return;
		}

		if( this.pointAt == axis.getOpposite() )
		{
			this.pointAt = axis;
		}
		else if( this.pointAt == axis || this.pointAt == axis.getOpposite() )
		{
			this.pointAt = AEPartLocation.INTERNAL;
		}
		else if( this.pointAt == AEPartLocation.INTERNAL )
		{
			this.pointAt = axis.getOpposite();
		}
		else
		{
			this.pointAt = Platform.rotateAround( this.pointAt, axis );
		}

		if( AEPartLocation.INTERNAL == this.pointAt )
		{
			this.setOrientation( EnumFacing.UP, EnumFacing.UP );
		}
		else
		{
			this.setOrientation( this.pointAt.yOffset != 0 ? EnumFacing.SOUTH : EnumFacing.UP, this.pointAt.getOpposite().getFacing() );
		}

		configureNodeSides();		
		this.markForUpdate();
		this.markDirty();
	}

	private void configureNodeSides()
	{
		if ( this.pointAt == AEPartLocation.INTERNAL )
			this.gridProxy.setValidSides( EnumSet.allOf(EnumFacing.class) );
		else
			this.gridProxy.setValidSides( EnumSet.complementOf( EnumSet.of( this.pointAt.getFacing() ) ) );
	}

	@Override
	public void markDirty()
	{
		this.duality.markDirty();
	}

	@Override
	public void getDrops(
			World w,
			BlockPos pos,
			List<ItemStack> drops )
	{
		this.duality.addDrops( drops );
	}

	@Override
	public void gridChanged()
	{
		this.duality.gridChanged();
	}

	@Override
	public void onReady()
	{
		configureNodeSides();		
		super.onReady();
		this.duality.initialize();
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileInterface( NBTTagCompound data )
	{
		data.setInteger( "pointAt", this.pointAt.ordinal() );
		this.duality.writeToNBT( data );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileInterface( NBTTagCompound data )
	{
		int val = data.getInteger( "pointAt" );

		if( val >= 0 && val < AEPartLocation.values().length )
		{
			this.pointAt = AEPartLocation.values()[val];
		}
		else
		{
			this.pointAt = AEPartLocation.INTERNAL;
		}

		this.duality.readFromNBT( data );
	}

	@Override
	public AECableType getCableConnectionType( AEPartLocation dir )
	{
		return this.duality.getCableConnectionType( dir );
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return this.duality.getLocation();
	}

	@Override
	public boolean canInsert( ItemStack stack )
	{
		return this.duality.canInsert( stack );
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return this.duality.getItemInventory();
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return this.duality.getFluidInventory();
	}

	@Override
	public IInventory getInventoryByName( String name )
	{
		return this.duality.getInventoryByName( name );
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return this.duality.getTickingRequest( node );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int ticksSinceLastCall )
	{
		return this.duality.tickingRequest( node, ticksSinceLastCall );
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.duality.getInternalInventory();
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added )
	{
		this.duality.onChangeInventory( inv, slot, mc, removed, added );
	}

	@Override
	public int[] getAccessibleSlotsBySide( EnumFacing side )
	{
		return this.duality.getSlotsForFace( side );
	}

	@Override
	public DualityInterface getInterfaceDuality()
	{
		return this.duality;
	}

	@Override
	public EnumSet<EnumFacing> getTargets()
	{
		if( this.pointAt == null || this.pointAt == AEPartLocation.INTERNAL )
		{
			return EnumSet.allOf( EnumFacing.class );
		}
		return EnumSet.of( this.pointAt.getFacing() );
	}

	@Override
	public TileEntity getTileEntity()
	{
		return this;
	}

	@Override
	public IStorageMonitorable getMonitorable( EnumFacing side, BaseActionSource src )
	{
		return this.duality.getMonitorable( side, src, this );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.duality.getConfigManager();
	}

	@Override
	public boolean pushPattern( ICraftingPatternDetails patternDetails, InventoryCrafting table )
	{
		return this.duality.pushPattern( patternDetails, table );
	}

	@Override
	public boolean isBusy()
	{
		return this.duality.isBusy();
	}

	@Override
	public void provideCrafting( ICraftingProviderHelper craftingTracker )
	{
		this.duality.provideCrafting( craftingTracker );
	}

	@Override
	public int getInstalledUpgrades( Upgrades u )
	{
		return this.duality.getInstalledUpgrades( u );
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return this.duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems( ICraftingLink link, IAEItemStack items, Actionable mode )
	{
		return this.duality.injectCraftedItems( link, items, mode );
	}

	@Override
	public void jobStateChange( ICraftingLink link )
	{
		this.duality.jobStateChange( link );
	}

	@Override
	public int getPriority()
	{
		return this.duality.getPriority();
	}

	@Override
	public void setPriority( int newValue )
	{
		this.duality.setPriority( newValue );
	}
}
