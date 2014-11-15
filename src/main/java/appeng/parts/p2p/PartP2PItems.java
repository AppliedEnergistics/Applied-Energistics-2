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

package appeng.parts.p2p;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.TunnelType;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBC;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.tile.inventory.AppEngNullInventory;
import appeng.transformer.annotations.integration.Interface;
import appeng.transformer.annotations.integration.Method;
import appeng.util.Platform;
import appeng.util.inv.WrapperBCPipe;
import appeng.util.inv.WrapperChainedInventory;
import appeng.util.inv.WrapperMCISidedInventory;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Interface(iface = "buildcraft.api.transport.IPipeConnection", iname = "BC")
public class PartP2PItems extends PartP2PTunnel<PartP2PItems> implements IPipeConnection, ISidedInventory, IGridTickable
{

	@Override
	public TunnelType getTunnelType()
	{
		return TunnelType.ITEM;
	}

	public PartP2PItems(ItemStack is) {
		super( is );
	}

	int oldSize = 0;
	boolean requested;
	IInventory cachedInv;

	final LinkedList<IInventory> which = new LinkedList<IInventory>();

	IInventory getOutputInv()
	{
		IInventory output = null;

		if ( proxy.isActive() )
		{
			TileEntity te = tile.getWorldObj().getTileEntity( tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ );

			if ( which.contains( this ) )
				return null;

			which.add( this );

			if ( AppEng.instance.isIntegrationEnabled( IntegrationType.BC ) )
			{
				IBC buildcraft = (IBC) AppEng.instance.getIntegration( IntegrationType.BC );
				if ( buildcraft != null )
				{
					if ( buildcraft.isPipe( te, side.getOpposite() ) )
					{
						try
						{
							output = new WrapperBCPipe( te, side.getOpposite() );
						}
						catch (Throwable ignore)
						{
						}
					}
				}
			}

			/*
			 * if ( AppEng.instance.isIntegrationEnabled( "TE" ) ) { ITE thermal = (ITE) AppEng.instance.getIntegration(
			 * "TE" ); if ( thermal != null ) { if ( thermal.isPipe( te, side.getOpposite() ) ) { try { output = new
			 * WrapperTEPipe( te, side.getOpposite() ); } catch (Throwable ignore) { } } } }
			 */

			if ( output == null )
			{
				if ( te instanceof TileEntityChest )
				{
					output = Platform.GetChestInv( te );
				}
				else if ( te instanceof ISidedInventory )
				{
					output = new WrapperMCISidedInventory( (ISidedInventory) te, side.getOpposite() );
				}
				else if ( te instanceof IInventory )
				{
					output = (IInventory) te;
				}
			}

			which.pop();
		}

		return output;
	}

	@Override
	public void onNeighborChanged()
	{
		cachedInv = null;
		PartP2PItems input = getInput();
		if ( input != null && output )
			input.onTunnelNetworkChange();
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest( TickRates.ItemTunnel.min, TickRates.ItemTunnel.max, false, false );
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		boolean wasReq = requested;

		if ( requested && cachedInv != null )
			((WrapperChainedInventory) cachedInv).cycleOrder();

		requested = false;
		return wasReq ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
	}

	IInventory getDestination()
	{
		requested = true;

		if ( cachedInv != null )
			return cachedInv;

		List<IInventory> outs = new LinkedList<IInventory>();
		TunnelCollection<PartP2PItems> itemTunnels;

		try
		{
			itemTunnels = getOutputs();
		}
		catch (GridAccessException e)
		{
			return new AppEngNullInventory();
		}

		for (PartP2PItems t : itemTunnels)
		{
			IInventory inv = t.getOutputInv();
			if ( inv != null )
			{
				if ( Platform.getRandomInt() % 2 == 0 )
					outs.add( inv );
				else
					outs.add( 0, inv );
			}
		}

		return cachedInv = new WrapperChainedInventory( outs );
	}

	@MENetworkEventSubscribe
	public void changeStateA(MENetworkBootingStatusChange bs)
	{
		if ( !output )
		{
			cachedInv = null;
			int olderSize = oldSize;
			oldSize = getDestination().getSizeInventory();
			if ( olderSize != oldSize )
			{
				getHost().notifyNeighbors();
			}
		}
	}

	@MENetworkEventSubscribe
	public void changeStateB(MENetworkChannelsChanged bs)
	{
		if ( !output )
		{
			cachedInv = null;
			int olderSize = oldSize;
			oldSize = getDestination().getSizeInventory();
			if ( olderSize != oldSize )
			{
				getHost().notifyNeighbors();
			}
		}
	}

	@MENetworkEventSubscribe
	public void changeStateC(MENetworkPowerStatusChange bs)
	{
		if ( !output )
		{
			cachedInv = null;
			int olderSize = oldSize;
			oldSize = getDestination().getSizeInventory();
			if ( olderSize != oldSize )
			{
				getHost().notifyNeighbors();
			}
		}
	}

	@Override
	public void onTunnelNetworkChange()
	{
		if ( !output )
		{
			cachedInv = null;
			int olderSize = oldSize;
			oldSize = getDestination().getSizeInventory();
			if ( olderSize != oldSize )
			{
				getHost().notifyNeighbors();
			}
		}
		else
		{
			PartP2PItems input = getInput();
			if ( input != null )
				input.getHost().notifyNeighbors();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTypeTexture()
	{
		return Blocks.hopper.getBlockTextureFromSide( 0 );
	}

	@Override
	public int getSizeInventory()
	{
		return getDestination().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return getDestination().getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return getDestination().decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		getDestination().setInventorySlotContents( i, itemstack );
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
		return getDestination().getInventoryStackLimit();
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
	public boolean isItemValidForSlot(int i, net.minecraft.item.ItemStack itemstack)
	{
		return getDestination().isItemValidForSlot( i, itemstack );
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		int[] slots = new int[getSizeInventory()];
		for (int x = 0; x < getSizeInventory(); x++)
			slots[x] = x;
		return slots;
	}

	public float getPowerDrainPerTick()
	{
		return 2.0f;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return getDestination().isItemValidForSlot( i, itemstack );
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return false;
	}

	@Override
	@Method(iname = "BC")
	public ConnectOverride overridePipeConnection(PipeType type, ForgeDirection with)
	{
		return side.equals( with ) && type == PipeType.ITEM ? ConnectOverride.CONNECT : ConnectOverride.DEFAULT;
	}

	@Override
	public void markDirty()
	{
		// eh?
	}

}
