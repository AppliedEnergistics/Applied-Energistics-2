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
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.settings.TickRates;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.tile.inventory.AppEngNullInventory;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.Method;
import appeng.util.Platform;
import appeng.util.inv.WrapperBCPipe;
import appeng.util.inv.WrapperChainedInventory;
import appeng.util.inv.WrapperMCISidedInventory;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.LinkedList;
import java.util.List;


@Interface( iface = "buildcraft.api.transport.IPipeConnection", iname = IntegrationType.BuildCraftTransport )
public class PartP2PItems extends PartP2PTunnel<PartP2PItems> implements IPipeConnection, ISidedInventory, IGridTickable
{

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
			final TileEntity te = this.getTile().getWorldObj().getTileEntity( this.getTile().xCoord + this.getSide().offsetX, this.getTile().yCoord + this.getSide().offsetY, this.getTile().zCoord + this.getSide().offsetZ );

			if( this.which.contains( this ) )
			{
				return null;
			}

			this.which.add( this );

			if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
			{
				final IBuildCraftTransport buildcraft = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
				if( buildcraft.isPipe( te, this.getSide().getOpposite() ) )
				{
					try
					{
						output = new WrapperBCPipe( te, this.getSide().getOpposite() );
					}
					catch( final Throwable ignore )
					{
					}
				}
			}

			/*
			 * if ( AppEng.INSTANCE.isIntegrationEnabled( "TE" ) ) { ITE thermal = (ITE) AppEng.INSTANCE.getIntegration(
			 * "TE" ); if ( thermal != null ) { if ( thermal.isPipe( te, side.getOpposite() ) ) { try { output = new
			 * WrapperTEPipe( te, side.getOpposite() ); } catch (Throwable ignore) { } } } }
			 */

			if( output == null )
			{
				if( te instanceof TileEntityChest )
				{
					output = Platform.GetChestInv( te );
				}
				else if( te instanceof ISidedInventory )
				{
					output = new WrapperMCISidedInventory( (ISidedInventory) te, this.getSide().getOpposite() );
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
	@SideOnly( Side.CLIENT )
	public IIcon getTypeTexture()
	{
		return Blocks.hopper.getBlockTextureFromSide( 0 );
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
	public int[] getAccessibleSlotsFromSide( final int var1 )
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
	public ItemStack getStackInSlotOnClosing( final int i )
	{
		return null;
	}

	@Override
	public void setInventorySlotContents( final int i, final ItemStack itemstack )
	{
		this.getDestination().setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getInventoryName()
	{
		return null;
	}

	@Override
	public boolean hasCustomInventoryName()
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
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot( final int i, final net.minecraft.item.ItemStack itemstack )
	{
		return this.getDestination().isItemValidForSlot( i, itemstack );
	}

	@Override
	public boolean canInsertItem( final int i, final ItemStack itemstack, final int j )
	{
		return this.getDestination().isItemValidForSlot( i, itemstack );
	}

	@Override
	public boolean canExtractItem( final int i, final ItemStack itemstack, final int j )
	{
		return false;
	}

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	}

	@Override
	@Method( iname = IntegrationType.BuildCraftTransport )
	public ConnectOverride overridePipeConnection( final PipeType type, final ForgeDirection with )
	{
		return this.getSide() == with && type == PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
	}
}
