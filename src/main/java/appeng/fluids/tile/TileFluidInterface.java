/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.tile;


import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.sync.GuiBridge;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.tile.grid.AENetworkTile;


public class TileFluidInterface extends AENetworkTile implements IGridTickable, IFluidInterfaceHost, IPriorityHost
{
	private final DualityFluidInterface duality = new DualityFluidInterface( this.getProxy(), this );

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
	public DualityFluidInterface getDualityFluidInterface()
	{
		return this.duality;
	}

	@Override
	public TileEntity getTileEntity()
	{
		return this;
	}

	@Override
	public void gridChanged()
	{
		this.duality.gridChanged();
	}

	@Override
	public CompoundNBT writeToNBT( final CompoundNBT data )
	{
		super.writeToNBT( data );
		this.duality.writeToNBT( data );
		return data;
	}

	@Override
	public void readFromNBT( final CompoundNBT data )
	{
		super.readFromNBT( data );
		this.duality.readFromNBT( data );
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
	public EnumSet<Direction> getTargets()
	{
		return EnumSet.allOf( Direction.class );
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

	@Override
	public boolean hasCapability( Capability<?> capability, @Nullable Direction facing )
	{
		return this.duality.hasCapability( capability, facing ) || super.hasCapability( capability, facing );
	}

	@Override
	public <T> T getCapability( Capability<T> capability, @Nullable Direction facing )
	{
		T result = this.duality.getCapability( capability, facing );
		if( result != null )
		{
			return result;
		}
		return super.getCapability( capability, facing );
	}

	@Override
	public int getInstalledUpgrades( Upgrades u )
	{
		return this.duality.getInstalledUpgrades( u );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.duality.getConfigManager();
	}

	@Override
	public IItemHandler getInventoryByName( String name )
	{
		return this.duality.getInventoryByName( name );
	}

	@Override
	public ItemStack getItemStackRepresentation()
	{
		return AEApi.instance().definitions().blocks().fluidIface().maybeStack( 1 ).orElse( ItemStack.EMPTY );
	}

	@Override
	public GuiBridge getGuiBridge()
	{
		return GuiBridge.GUI_FLUID_INTERFACE;
	}
}
