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


import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.tile.inventory.AppEngNullInventory;
import appeng.util.Platform;
import appeng.util.inv.WrapperChainedInventory;
import appeng.util.inv.WrapperMCISidedInventory;


// TODO: BC Integration
//@Interface( iface = "buildcraft.api.transport.IPipeConnection", iname = IntegrationType.BuildCraftTransport )
public class PartP2PItems extends PartP2PTunnel<PartP2PItems> implements /* IPipeConnection, */ISidedInventory, IGridTickable
{

	private static final P2PModels MODELS = new P2PModels( "part/p2p/p2p_tunnel_items" );

	@PartModels
	public static List<IPartModel> getModels()
	{
		return MODELS.getModels();
	}

	private final LinkedList<IInventory> which = new LinkedList<IInventory>();
	private int oldSize = 0;
	private boolean requested;
	private IInventory cachedInv;

	public PartP2PItems( final ItemStack is )
	{
		super( is );
	}

	@Override
	public void onNeighborChanged()
	{
		this.cachedInv = null;
		final PartP2PItems input = this.getInput();
		if( input != null && this.isOutput() )
		{
			input.onTunnelNetworkChange();
		}
	}

	private IInventory getDestination()
	{
		this.requested = true;

		if( this.cachedInv != null )
		{
			return this.cachedInv;
		}

		final List<IInventory> outs = new LinkedList<IInventory>();
		final TunnelCollection<PartP2PItems> itemTunnels;

		try
		{
			itemTunnels = this.getOutputs();
		}
		catch( final GridAccessException e )
		{
			return new AppEngNullInventory();
		}

		for( final PartP2PItems t : itemTunnels )
		{
			final IInventory inv = t.getOutputInv();
			if( inv != null )
			{
				if( Platform.getRandomInt() % 2 == 0 )
				{
					outs.add( inv );
				}
				else
				{
					outs.add( 0, inv );
				}
			}
		}

		return this.cachedInv = new WrapperChainedInventory( outs );
	}

	private IInventory getOutputInv()
	{
		IInventory output = null;

		if( this.getProxy().isActive() )
		{
			final TileEntity te = this.getTile().getWorld().getTileEntity( this.getTile().getPos().offset( this.getSide().getFacing() ) );

			if( this.which.contains( this ) )
			{
				return null;
			}

			this.which.add( this );

			if( output == null )
			{
				if( te instanceof TileEntityChest )
				{
					output = Platform.GetChestInv( te );
				}
				else if( te instanceof ISidedInventory )
				{
					output = new WrapperMCISidedInventory( (ISidedInventory) te, this.getSide().getFacing().getOpposite() );
				}
				else if( te instanceof IInventory )
				{
					output = (IInventory) te;
				}
			}

			this.which.pop();
		}

		return output;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.ItemTunnel.getMin(), TickRates.ItemTunnel.getMax(), false, false );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		final boolean wasReq = this.requested;

		if( this.requested && this.cachedInv != null )
		{
			( (WrapperChainedInventory) this.cachedInv ).cycleOrder();
		}

		this.requested = false;
		return wasReq ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	@MENetworkEventSubscribe
	public void changeStateA( final MENetworkBootingStatusChange bs )
	{
		if( !this.isOutput() )
		{
			this.cachedInv = null;
			final int olderSize = this.oldSize;
			this.oldSize = this.getDestination().getSizeInventory();
			if( olderSize != this.oldSize )
			{
				this.getHost().notifyNeighbors();
			}
		}
	}

	@MENetworkEventSubscribe
	public void changeStateB( final MENetworkChannelsChanged bs )
	{
		if( !this.isOutput() )
		{
			this.cachedInv = null;
			final int olderSize = this.oldSize;
			this.oldSize = this.getDestination().getSizeInventory();
			if( olderSize != this.oldSize )
			{
				this.getHost().notifyNeighbors();
			}
		}
	}

	@MENetworkEventSubscribe
	public void changeStateC( final MENetworkPowerStatusChange bs )
	{
		if( !this.isOutput() )
		{
			this.cachedInv = null;
			final int olderSize = this.oldSize;
			this.oldSize = this.getDestination().getSizeInventory();
			if( olderSize != this.oldSize )
			{
				this.getHost().notifyNeighbors();
			}
		}
	}

	@Override
	public void onTunnelNetworkChange()
	{
		if( !this.isOutput() )
		{
			this.cachedInv = null;
			final int olderSize = this.oldSize;
			this.oldSize = this.getDestination().getSizeInventory();
			if( olderSize != this.oldSize )
			{
				this.getHost().notifyNeighbors();
			}
		}
		else
		{
			final PartP2PItems input = this.getInput();
			if( input != null )
			{
				input.getHost().notifyNeighbors();
			}
		}
	}

	@Override
	public int[] getSlotsForFace( final EnumFacing side )
	{
		final int[] slots = new int[this.getSizeInventory()];
		for( int x = 0; x < this.getSizeInventory(); x++ )
		{
			slots[x] = x;
		}
		return slots;
	}

	@Override
	public int getSizeInventory()
	{
		return this.getDestination().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot( final int i )
	{
		return this.getDestination().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize( final int i, final int j )
	{
		return this.getDestination().decrStackSize( i, j );
	}

	@Override
	public ItemStack removeStackFromSlot( final int i )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( final int i, final ItemStack itemstack )
	{
		this.getDestination().setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public boolean hasCustomName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return this.getDestination().getInventoryStackLimit();
	}

	@Override
	public void markDirty()
	{
		// eh?
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer entityplayer )
	{
		return false;
	}

	@Override
	public void openInventory( final EntityPlayer p )
	{
	}

	@Override
	public void closeInventory( final EntityPlayer p )
	{
	}

	@Override
	public boolean isItemValidForSlot( final int i, final net.minecraft.item.ItemStack itemstack )
	{
		return this.getDestination().isItemValidForSlot( i, itemstack );
	}

	@Override
	public boolean canInsertItem( final int i, final ItemStack itemstack, final EnumFacing j )
	{
		return this.getDestination().isItemValidForSlot( i, itemstack );
	}

	@Override
	public boolean canExtractItem( final int i, final ItemStack itemstack, final EnumFacing j )
	{
		return false;
	}

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	}

	@Override
	public int getField( final int id )
	{
		return 0;
	}

	// @Override
	// @Method( iname = IntegrationType.BuildCraftTransport )
	// public ConnectOverride overridePipeConnection( PipeType type, ForgeDirection with )
	// {
	// return 0;
	// }

	@Override
	public void setField( final int id, final int value )
	{

	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		// probobly not...
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return null;
	}

	@Override
	public IPartModel getStaticModels()
	{
		return MODELS.getModel( isPowered(), isActive() );
	}

}
