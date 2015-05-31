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


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.tile.networking.TileWireless;


public final class WirelessTerminalGuiObject implements IPortableCell, IActionHost, IInventorySlotAware
{

	public final ItemStack effectiveItem;
	final IWirelessTermHandler wth;
	final String encryptionKey;
	final EntityPlayer myPlayer;
	IGrid targetGrid;
	IStorageGrid sg;
	IMEMonitor<IAEItemStack> itemStorage;
	IWirelessAccessPoint myWap;
	double sqRange = Double.MAX_VALUE;
	double myRange = Double.MAX_VALUE;
	private final int inventorySlot;

	public WirelessTerminalGuiObject( IWirelessTermHandler wh, ItemStack is, EntityPlayer ep, World w, int x, int y, int z )
	{
		this.encryptionKey = wh.getEncryptionKey( is );
		this.effectiveItem = is;
		this.myPlayer = ep;
		this.wth = wh;
		this.inventorySlot = x;

		ILocatable obj = null;

		try
		{
			long encKey = Long.parseLong( this.encryptionKey );
			obj = AEApi.instance().registries().locatable().getLocatableBy( encKey );
		}
		catch( NumberFormatException err )
		{
			// :P
		}

		if( obj instanceof IGridHost )
		{
			IGridNode n = ( (IGridHost) obj ).getGridNode( ForgeDirection.UNKNOWN );
			if( n != null )
			{
				this.targetGrid = n.getGrid();
				if( this.targetGrid != null )
				{
					this.sg = this.targetGrid.getCache( IStorageGrid.class );
					if( this.sg != null )
					{
						this.itemStorage = this.sg.getItemInventory();
					}
				}
			}
		}
	}

	public final double getRange()
	{
		return this.myRange;
	}

	@Override
	public final IMEMonitor<IAEItemStack> getItemInventory()
	{
		if( this.sg == null )
		{
			return null;
		}
		return this.sg.getItemInventory();
	}

	@Override
	public final IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		if( this.sg == null )
		{
			return null;
		}
		return this.sg.getFluidInventory();
	}

	@Override
	public final void addListener( IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken )
	{
		if( this.itemStorage != null )
		{
			this.itemStorage.addListener( l, verificationToken );
		}
	}

	@Override
	public final void removeListener( IMEMonitorHandlerReceiver<IAEItemStack> l )
	{
		if( this.itemStorage != null )
		{
			this.itemStorage.removeListener( l );
		}
	}

	@Override
	public final IItemList<IAEItemStack> getAvailableItems( IItemList out )
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.getAvailableItems( out );
		}
		return out;
	}

	@Override
	public final IItemList<IAEItemStack> getStorageList()
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.getStorageList();
		}
		return null;
	}

	@Override
	public final AccessRestriction getAccess()
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.getAccess();
		}
		return AccessRestriction.NO_ACCESS;
	}

	@Override
	public final boolean isPrioritized( IAEItemStack input )
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.isPrioritized( input );
		}
		return false;
	}

	@Override
	public final boolean canAccept( IAEItemStack input )
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.canAccept( input );
		}
		return false;
	}

	@Override
	public final int getPriority()
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.getPriority();
		}
		return 0;
	}

	@Override
	public final int getSlot()
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.getSlot();
		}
		return 0;
	}

	@Override
	public final boolean validForPass( int i )
	{
		return this.itemStorage.validForPass( i );
	}

	@Override
	public final IAEItemStack injectItems( IAEItemStack input, Actionable type, BaseActionSource src )
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.injectItems( input, type, src );
		}
		return input;
	}

	@Override
	public final IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.extractItems( request, mode, src );
		}
		return null;
	}

	@Override
	public final StorageChannel getChannel()
	{
		if( this.itemStorage != null )
		{
			return this.itemStorage.getChannel();
		}
		return StorageChannel.ITEMS;
	}

	@Override
	public final double extractAEPower( double amt, Actionable mode, PowerMultiplier usePowerMultiplier )
	{
		if( this.wth != null && this.effectiveItem != null )
		{
			if( mode == Actionable.SIMULATE )
			{
				return this.wth.hasPower( this.myPlayer, amt, this.effectiveItem ) ? amt : 0;
			}
			return this.wth.usePower( this.myPlayer, amt, this.effectiveItem ) ? amt : 0;
		}
		return 0.0;
	}

	@Override
	public final ItemStack getItemStack()
	{
		return this.effectiveItem;
	}

	@Override
	public final IConfigManager getConfigManager()
	{
		return this.wth.getConfigManager( this.effectiveItem );
	}

	@Override
	public final IGridNode getGridNode( ForgeDirection dir )
	{
		return this.getActionableNode();
	}

	@Override
	public final AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.NONE;
	}

	@Override
	public final void securityBreak()
	{

	}

	@Override
	public final IGridNode getActionableNode()
	{
		this.rangeCheck();
		if( this.myWap != null )
		{
			return this.myWap.getActionableNode();
		}
		return null;
	}

	public final boolean rangeCheck()
	{
		this.sqRange = this.myRange = Double.MAX_VALUE;

		if( this.targetGrid != null && this.itemStorage != null )
		{
			if( this.myWap != null )
			{
				if( this.myWap.getGrid() == this.targetGrid )
				{
					if( this.testWap( this.myWap ) )
					{
						return true;
					}
				}
				return false;
			}

			IMachineSet tw = this.targetGrid.getMachines( TileWireless.class );

			this.myWap = null;

			for( IGridNode n : tw )
			{
				IWirelessAccessPoint wap = (IWirelessAccessPoint) n.getMachine();
				if( this.testWap( wap ) )
				{
					this.myWap = wap;
				}
			}

			return this.myWap != null;
		}
		return false;
	}

	private boolean testWap( IWirelessAccessPoint wap )
	{
		double rangeLimit = wap.getRange();
		rangeLimit *= rangeLimit;

		DimensionalCoord dc = wap.getLocation();

		if( dc.getWorld() == this.myPlayer.worldObj )
		{
			double offX = dc.x - this.myPlayer.posX;
			double offY = dc.y - this.myPlayer.posY;
			double offZ = dc.z - this.myPlayer.posZ;

			double r = offX * offX + offY * offY + offZ * offZ;
			if( r < rangeLimit && this.sqRange > r )
			{
				if( wap.isActive() )
				{
					this.sqRange = r;
					this.myRange = Math.sqrt( r );
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getInventorySlot()
	{
		return this.inventorySlot;
	}

}
