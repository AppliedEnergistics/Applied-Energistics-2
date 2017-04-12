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

package appeng.parts.p2p;


import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.settings.TickRates;
import appeng.integration.IntegrationType;
import appeng.transformer.annotations.Integration.Interface;
import appeng.util.Platform;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import pneumaticCraft.api.block.BlockSupplier;
import pneumaticCraft.api.tileentity.AirHandlerSupplier;
import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.ISidedPneumaticMachine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


@Interface( iface = "pneumaticCraft.api.tileentity.ISidedPneumaticMachine", iname = IntegrationType.PneumaticCraft )
public final class PartP2PPressure extends PartP2PTunnel<PartP2PPressure> implements ISidedPneumaticMachine, IGridTickable
{
	private static final String PRESSURE_NBT_TAG = "pneumaticCraft";
	private static final String PRESSURE_TYPE_ICON_NAME = "compressedIronBlock";

	/**
	 * The pressure should never exceed 30f, thus preventing the tunnel from exploding.
	 */
	private static final float MAX_PRESSURE = 30f;
	private static final int VOLUME = 1000;

	@Nonnull
	private final IAirHandler handler;
	private boolean isConnected = false;
	private boolean isValid = false;

	public PartP2PPressure( final ItemStack is )
	{
		super( is );
		this.handler = AirHandlerSupplier.getAirHandler( MAX_PRESSURE, MAX_PRESSURE, VOLUME );
	}

	@Override
	protected IIcon getTypeTexture()
	{
		return BlockSupplier.getBlock( PRESSURE_TYPE_ICON_NAME ).getIcon( 0, 0 );
	}

	@Nullable
	@Override
	public IAirHandler getAirHandler( final ForgeDirection side )
	{
		if( side == this.getSide() )
		{
			return this.getInternalHandler();
		}

		return null;
	}

	@Override
	public void onNeighborChanged()
	{
		super.onNeighborChanged();

		if( this.isValid )
		{
			this.getInternalHandler().onNeighborChange();
		}
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();

		this.getInternalHandler().validateI( this.getTile() );
		this.isValid = true;
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();

		this.isValid = false;
		if( this.isOutput() && this.getInput() != null )
		{
			this.getInternalHandler().removeConnection( this.getInput().getInternalHandler() );
			this.isConnected = false;
		}
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.PressureTunnel.getMin(), TickRates.PressureTunnel.getMax(), false, false );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		if( this.getProxy().isPowered() && this.getProxy().isActive() )
		{
			if( !this.isConnected )
			{
				this.updateHandler();
			}

			this.getInternalHandler().updateEntityI();
			return TickRateModulation.URGENT;
		}

		return TickRateModulation.IDLE;
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		final NBTTagCompound pneumaticNBT = new NBTTagCompound();

		this.getInternalHandler().writeToNBTI( pneumaticNBT );
		data.setTag( PRESSURE_NBT_TAG, pneumaticNBT );
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.getInternalHandler().readFromNBTI( data.getCompoundTag( PRESSURE_NBT_TAG ) );
	}

	@Nonnull
	private IAirHandler getInternalHandler()
	{
		return this.handler;
	}

	private void updateHandler()
	{
		if( this.getProxy().isPowered() && this.getProxy().isActive() )
		{

			if( this.isOutput() && this.getInput() != null )
			{
				this.getInternalHandler().createConnection( this.getInput().getInternalHandler() );
				this.isConnected = true;
			}

			final TileEntity te = this.getTile();
			Platform.notifyBlocksOfNeighbors( te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord );
		}
	}

}
