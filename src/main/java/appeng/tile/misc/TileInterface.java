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


import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import io.netty.buffer.ByteBuf;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.sync.GuiBridge;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;


public class TileInterface extends AENetworkInvTile implements IGridTickable, IInventoryDestination, IInterfaceHost, IPriorityHost
{

	private final DualityInterface duality = new DualityInterface( this.getProxy(), this );

	// Indicates that this interface has no specific direction set
	private boolean omniDirectional = true;

	@MENetworkEventSubscribe
	public void stateChange( final MENetworkChannelsChanged c )
	{
		this.duality.notifyNeighbors();
	}

	@MENetworkEventSubscribe
	public void stateChange( final MENetworkPowerStatusChange c )
	{
		this.duality.notifyNeighbors();
	}

	public void setSide( final EnumFacing facing )
	{
		if( Platform.isClient() )
		{
			return;
		}

		EnumFacing newForward = facing;

		if( !this.omniDirectional && this.getForward() == facing.getOpposite() )
		{
			newForward = facing;
		}
		else if( !this.omniDirectional && ( this.getForward() == facing || this.getForward() == facing.getOpposite() ) )
		{
			this.omniDirectional = true;
		}
		else if( this.omniDirectional )
		{
			newForward = facing.getOpposite();
			this.omniDirectional = false;
		}
		else
		{
			newForward = Platform.rotateAround( this.getForward(), facing );
		}

		if( this.omniDirectional )
		{
			this.setOrientation( EnumFacing.NORTH, EnumFacing.UP );
		}
		else
		{
			EnumFacing newUp = EnumFacing.UP;
			if( newForward == EnumFacing.UP || newForward == EnumFacing.DOWN )
			{
				newUp = EnumFacing.NORTH;
			}
			this.setOrientation( newForward, newUp );
		}

		this.configureNodeSides();
		this.markForUpdate();
		this.saveChanges();
	}

	private void configureNodeSides()
	{
		if( this.omniDirectional )
		{
			this.getProxy().setValidSides( EnumSet.allOf( EnumFacing.class ) );
		}
		else
		{
			this.getProxy().setValidSides( EnumSet.complementOf( EnumSet.of( this.getForward() ) ) );
		}
	}

	@Override
	public void getDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
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
		this.configureNodeSides();

		super.onReady();
		this.duality.initialize();
		this.getProxy().setIdlePowerUsage( Math.pow( 4, ( this.getInstalledUpgrades( Upgrades.PATTERN_EXPANSION ) ) ) );
	}

	@Override
	public NBTTagCompound writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setBoolean( "omniDirectional", this.omniDirectional );
		this.duality.writeToNBT( data );
		return data;
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.omniDirectional = data.getBoolean( "omniDirectional" );

		this.duality.readFromNBT( data );
	}

	@Override
	protected boolean readFromStream( final ByteBuf data ) throws IOException
	{
		final boolean c = super.readFromStream( data );
		boolean oldOmniDirectional = this.omniDirectional;
		this.omniDirectional = data.readBoolean();
		return oldOmniDirectional != this.omniDirectional || c;
	}

	@Override
	protected void writeToStream( final ByteBuf data ) throws IOException
	{
		super.writeToStream( data );
		data.writeBoolean( this.omniDirectional );
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return this.duality.getCableConnectionType( dir );
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return this.duality.getLocation();
	}

	@Override
	public boolean canInsert( final ItemStack stack )
	{
		return this.duality.canInsert( stack );
	}

	@Override
	public IItemHandler getInventoryByName( final String name )
	{
		return this.duality.getInventoryByName( name );
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return this.duality.getTickingRequest( node );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		return this.duality.tickingRequest( node, ticksSinceLastCall );
	}

	@Override
	public IItemHandler getInternalInventory()
	{
		return this.duality.getInternalInventory();
	}

	@Override
	public void onChangeInventory( final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		this.duality.onChangeInventory( inv, slot, mc, removed, added );
	}

	@Override
	public DualityInterface getInterfaceDuality()
	{
		return this.duality;
	}

	@Override
	public EnumSet<EnumFacing> getTargets()
	{
		if( this.omniDirectional )
		{
			return EnumSet.allOf( EnumFacing.class );
		}
		return EnumSet.of( this.getForward() );
	}

	@Override
	public TileEntity getTileEntity()
	{
		return this;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.duality.getConfigManager();
	}

	@Override
	public boolean pushPattern( final ICraftingPatternDetails patternDetails, final InventoryCrafting table )
	{
		return this.duality.pushPattern( patternDetails, table );
	}

	@Override
	public boolean isBusy()
	{
		return this.duality.isBusy();
	}

	@Override
	public void provideCrafting( final ICraftingProviderHelper craftingTracker )
	{
		this.duality.provideCrafting( craftingTracker );
	}

	@Override
	public int getInstalledUpgrades( final Upgrades u )
	{
		return this.duality.getInstalledUpgrades( u );
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		return this.duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems( final ICraftingLink link, final IAEItemStack items, final Actionable mode )
	{
		return this.duality.injectCraftedItems( link, items, mode );
	}

	@Override
	public void jobStateChange( final ICraftingLink link )
	{
		this.duality.jobStateChange( link );
	}

	@Override
	public int getPriority()
	{
		return this.duality.getPriority();
	}

	@Override
	public void setPriority( final int newValue )
	{
		this.duality.setPriority( newValue );
	}

	/**
	 * @return True if this interface is omni-directional.
	 */
	public boolean isOmniDirectional()
	{
		return this.omniDirectional;
	}

	@Override
	public boolean hasCapability( Capability<?> capability, @Nullable EnumFacing facing )
	{
		return this.duality.hasCapability( capability, facing ) || super.hasCapability( capability, facing );
	}

	@Override
	public <T> T getCapability( Capability<T> capability, @Nullable EnumFacing facing )
	{
		T result = this.duality.getCapability( capability, facing );
		if( result != null )
		{
			return result;
		}
		return super.getCapability( capability, facing );
	}

	@Override
	public ItemStack getItemStackRepresentation()
	{
		return AEApi.instance().definitions().blocks().iface().maybeStack( 1 ).orElse( ItemStack.EMPTY );
	}

	@Override
	public GuiBridge getGuiBridge()
	{
		return GuiBridge.GUI_INTERFACE;
	}
}
