/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.parts.misc;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import appeng.api.AEApi;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.me.helpers.AENetworkProxy;
import appeng.parts.PartBasicState;
import appeng.parts.PartModel;
import appeng.util.Platform;


public class PartToggleBus extends PartBasicState
{

	@PartModels
	public static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "part/toggle_bus_base" );
	@PartModels
	public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation( AppEng.MOD_ID, "part/toggle_bus_status_off" );
	@PartModels
	public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation( AppEng.MOD_ID, "part/toggle_bus_status_on" );
	@PartModels
	public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation( AppEng.MOD_ID, "part/toggle_bus_status_has_channel" );

	public static final IPartModel MODELS_OFF = new PartModel( MODEL_BASE, MODEL_STATUS_OFF );
	public static final IPartModel MODELS_ON = new PartModel( MODEL_BASE, MODEL_STATUS_ON );
	public static final IPartModel MODELS_HAS_CHANNEL = new PartModel( MODEL_BASE, MODEL_STATUS_HAS_CHANNEL );

	private static final int REDSTONE_FLAG = 4;
	private final AENetworkProxy outerProxy = new AENetworkProxy( this, "outer", null, true );
	private IGridConnection connection;
	private boolean hasRedstone = false;

	@Reflected
	public PartToggleBus( final ItemStack is )
	{
		super( is );

		this.getProxy().setIdlePowerUsage( 0.0 );
		this.getOuterProxy().setIdlePowerUsage( 0.0 );
		this.getProxy().setFlags();
		this.getOuterProxy().setFlags();
	}

	@Override
	protected int populateFlags( final int cf )
	{
		return cf | ( this.getIntention() ? REDSTONE_FLAG : 0 );
	}

	public boolean hasRedstoneFlag()
	{
		return ( getClientFlags() & REDSTONE_FLAG ) == REDSTONE_FLAG;
	}

	protected boolean getIntention()
	{
		return this.getHost().hasRedstone( this.getSide() );
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.GLASS;
	}

	@Override
	public void securityBreak()
	{
		if( this.getItemStack().stackSize > 0 )
		{
			final List<ItemStack> items = new ArrayList<ItemStack>();
			items.add( this.getItemStack().copy() );
			this.getHost().removePart( this.getSide(), false );
			Platform.spawnDrops( this.getTile().getWorld(), this.getTile().getPos(), items );
			this.getItemStack().stackSize = 0;
		}
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 6, 6, 11, 10, 10, 16 );
	}

	@Override
	public void onNeighborChanged()
	{
		final boolean oldHasRedstone = this.hasRedstone;
		this.hasRedstone = this.getHost().hasRedstone( this.getSide() );

		if( this.hasRedstone != oldHasRedstone )
		{
			this.updateInternalState();
			this.getHost().markForUpdate();
		}
	}

	@Override
	public void readFromNBT( final NBTTagCompound extra )
	{
		super.readFromNBT( extra );
		this.getOuterProxy().readFromNBT( extra );
	}

	@Override
	public void writeToNBT( final NBTTagCompound extra )
	{
		super.writeToNBT( extra );
		this.getOuterProxy().writeToNBT( extra );
	}

	@Override
	public void removeFromWorld()
	{
		super.removeFromWorld();
		this.getOuterProxy().invalidate();
	}

	@Override
	public void addToWorld()
	{
		super.addToWorld();
		this.getOuterProxy().onReady();
		this.hasRedstone = this.getHost().hasRedstone( this.getSide() );
		this.updateInternalState();
	}

	@Override
	public void setPartHostInfo( final AEPartLocation side, final IPartHost host, final TileEntity tile )
	{
		super.setPartHostInfo( side, host, tile );
		this.outerProxy.setValidSides( EnumSet.of( side.getFacing() ) );
	}

	@Override
	public IGridNode getExternalFacingNode()
	{
		return this.getOuterProxy().getNode();
	}

	@Override
	public float getCableConnectionLength( AECableType cable )
	{
		return 5;
	}

	@Override
	public void onPlacement( final EntityPlayer player, final EnumHand hand, final ItemStack held, final AEPartLocation side )
	{
		super.onPlacement( player, hand, held, side );
		this.getOuterProxy().setOwner( player );
	}

	private void updateInternalState()
	{
		final boolean intention = this.getIntention();
		if( intention == ( this.connection == null ) )
		{
			if( this.getProxy().getNode() != null && this.getOuterProxy().getNode() != null )
			{
				if( intention )
				{
					try
					{
						this.connection = AEApi.instance().createGridConnection( this.getProxy().getNode(), this.getOuterProxy().getNode() );
					}
					catch( final FailedConnection e )
					{
						// :(
					}
				}
				else
				{
					this.connection.destroy();
					this.connection = null;
				}
			}
		}
	}

	AENetworkProxy getOuterProxy()
	{
		return this.outerProxy;
	}

	@Override
	public IPartModel getStaticModels()
	{
		if( hasRedstoneFlag() && isActive() && isPowered() )
		{
			return MODELS_HAS_CHANNEL;
		}
		else if( hasRedstoneFlag() && isPowered() )
		{
			return MODELS_ON;
		}
		else
		{
			return MODELS_OFF;
		}
	}
}
