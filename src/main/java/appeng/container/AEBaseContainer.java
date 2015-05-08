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

package appeng.container;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.PlayerSource;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.guisync.GuiSync;
import appeng.container.guisync.SyncData;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotInaccessible;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketPartialItem;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.InventoryAction;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;


public abstract class AEBaseContainer extends Container
{

	protected final InventoryPlayer invPlayer;
	protected final BaseActionSource mySrc;
	protected final Set<Integer> locked = new HashSet<Integer>();
	final TileEntity tileEntity;
	final IPart part;
	final IGuiItemObject obj;
	final Collection<PacketPartialItem> dataChunks = new LinkedList<PacketPartialItem>();
	final Map<Integer, SyncData> syncData = new HashMap<Integer, SyncData>();
	public boolean isContainerValid = true;
	public String customName;
	public ContainerOpenContext openContext;
	protected IMEInventoryHandler<IAEItemStack> cellInv;
	protected IEnergySource powerSrc;
	boolean sentCustomName;
	int ticksSinceCheck = 900;
	IAEItemStack clientRequestedTargetItem = null;

	public AEBaseContainer( InventoryPlayer ip, TileEntity myTile, IPart myPart )
	{
		this( ip, myTile, myPart, null );
	}

	public AEBaseContainer( InventoryPlayer ip, TileEntity myTile, IPart myPart, IGuiItemObject gio )
	{
		this.invPlayer = ip;
		this.tileEntity = myTile;
		this.part = myPart;
		this.obj = gio;
		this.mySrc = new PlayerSource( ip.player, this.getActionHost() );
		this.prepareSync();
	}

	protected IActionHost getActionHost()
	{
		if( this.obj instanceof IActionHost )
		{
			return (IActionHost) this.obj;
		}

		if( this.tileEntity instanceof IActionHost )
		{
			return (IActionHost) this.tileEntity;
		}

		if( this.part instanceof IActionHost )
		{
			return (IActionHost) this.part;
		}

		return null;
	}

	private void prepareSync()
	{
		for( Field f : this.getClass().getFields() )
		{
			if( f.isAnnotationPresent( GuiSync.class ) )
			{
				GuiSync annotation = f.getAnnotation( GuiSync.class );
				if( this.syncData.containsKey( annotation.value() ) )
				{
					AELog.warning( "Channel already in use: " + annotation.value() + " for " + f.getName() );
				}
				else
				{
					this.syncData.put( annotation.value(), new SyncData( this, f, annotation ) );
				}
			}
		}
	}

	public AEBaseContainer( InventoryPlayer ip, Object anchor )
	{
		this.invPlayer = ip;
		this.tileEntity = anchor instanceof TileEntity ? (TileEntity) anchor : null;
		this.part = anchor instanceof IPart ? (IPart) anchor : null;
		this.obj = anchor instanceof IGuiItemObject ? (IGuiItemObject) anchor : null;

		if( this.tileEntity == null && this.part == null && this.obj == null )
		{
			throw new IllegalArgumentException( "Must have a valid anchor, instead " + anchor + " in " + ip );
		}

		this.mySrc = new PlayerSource( ip.player, this.getActionHost() );

		this.prepareSync();
	}

	public void postPartial( PacketPartialItem packetPartialItem )
	{
		this.dataChunks.add( packetPartialItem );
		if( packetPartialItem.getPageCount() == this.dataChunks.size() )
		{
			this.parsePartials();
		}
	}

	private void parsePartials()
	{
		int total = 0;
		for( PacketPartialItem ppi : this.dataChunks )
		{
			total += ppi.getSize();
		}

		byte[] buffer = new byte[total];
		int cursor = 0;

		for( PacketPartialItem ppi : this.dataChunks )
		{
			cursor = ppi.write( buffer, cursor );
		}

		try
		{
			NBTTagCompound data = CompressedStreamTools.readCompressed( new ByteArrayInputStream( buffer ) );
			if( data != null )
			{
				this.setTargetStack( AEApi.instance().storage().createItemStack( ItemStack.loadItemStackFromNBT( data ) ) );
			}
		}
		catch( IOException e )
		{
			AELog.error( e );
		}

		this.dataChunks.clear();
	}

	public IAEItemStack getTargetStack()
	{
		return this.clientRequestedTargetItem;
	}

	public void setTargetStack( IAEItemStack stack )
	{
		// client doesn't need to re-send, makes for lower overhead rapid packets.
		if( Platform.isClient() )
		{
			ItemStack a = stack == null ? null : stack.getItemStack();
			ItemStack b = this.clientRequestedTargetItem == null ? null : this.clientRequestedTargetItem.getItemStack();

			if( Platform.isSameItemPrecise( a, b ) )
			{
				return;
			}

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			NBTTagCompound item = new NBTTagCompound();

			if( stack != null )
			{
				stack.writeToNBT( item );
			}

			try
			{
				CompressedStreamTools.writeCompressed( item, stream );

				int maxChunkSize = 30000;
				Collection<byte[]> miniPackets = new LinkedList<byte[]>();

				byte[] data = stream.toByteArray();

				ByteArrayInputStream bis = new ByteArrayInputStream( data, 0, stream.size() );
				while( bis.available() > 0 )
				{
					int nextBLock = bis.available() > maxChunkSize ? maxChunkSize : bis.available();
					byte[] nextSegment = new byte[nextBLock];
					bis.read( nextSegment );
					miniPackets.add( nextSegment );
				}
				bis.close();
				stream.close();

				int page = 0;
				for( byte[] packet : miniPackets )
				{
					PacketPartialItem ppi = new PacketPartialItem( page, miniPackets.size(), packet );
					page++;
					NetworkHandler.instance.sendToServer( ppi );
				}
			}
			catch( IOException e )
			{
				AELog.error( e );
				return;
			}
		}

		this.clientRequestedTargetItem = stack == null ? null : stack.copy();
	}

	public BaseActionSource getSource()
	{
		return this.mySrc;
	}

	public void verifyPermissions( SecurityPermissions security, boolean requirePower )
	{
		if( Platform.isClient() )
		{
			return;
		}

		this.ticksSinceCheck++;
		if( this.ticksSinceCheck < 20 )
		{
			return;
		}

		this.ticksSinceCheck = 0;
		this.isContainerValid = this.isContainerValid && this.hasAccess( security, requirePower );
	}

	protected boolean hasAccess( SecurityPermissions perm, boolean requirePower )
	{
		IActionHost host = this.getActionHost();

		if( host != null )
		{
			IGridNode gn = host.getActionableNode();
			if( gn != null )
			{
				IGrid g = gn.getGrid();
				if( g != null )
				{
					if( requirePower )
					{
						IEnergyGrid eg = g.getCache( IEnergyGrid.class );
						if( !eg.isNetworkPowered() )
						{
							return false;
						}
					}

					ISecurityGrid sg = g.getCache( ISecurityGrid.class );
					if( sg.hasPermission( this.invPlayer.player, perm ) )
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	public void lockPlayerInventorySlot( int idx )
	{
		this.locked.add( idx );
	}

	public Object getTarget()
	{
		if( this.tileEntity != null )
		{
			return this.tileEntity;
		}
		if( this.part != null )
		{
			return this.part;
		}
		if( this.obj != null )
		{
			return this.obj;
		}
		return null;
	}

	public InventoryPlayer getPlayerInv()
	{
		return this.invPlayer;
	}

	public TileEntity getTileEntity()
	{
		return this.tileEntity;
	}

	public final void updateFullProgressBar( int idx, long value )
	{
		if( this.syncData.containsKey( idx ) )
		{
			this.syncData.get( idx ).update( value );
			return;
		}

		this.updateProgressBar( idx, (int) value );
	}

	public void stringSync( int idx, String value )
	{
		if( this.syncData.containsKey( idx ) )
		{
			this.syncData.get( idx ).update( value );
		}
	}

	protected void bindPlayerInventory( InventoryPlayer inventoryPlayer, int offsetX, int offsetY )
	{
		// bind player inventory
		for( int i = 0; i < 3; i++ )
		{
			for( int j = 0; j < 9; j++ )
			{
				if( this.locked.contains( j + i * 9 + 9 ) )
				{
					this.addSlotToContainer( new SlotDisabled( inventoryPlayer, j + i * 9 + 9, 8 + j * 18 + offsetX, offsetY + i * 18 ) );
				}
				else
				{
					this.addSlotToContainer( new SlotPlayerInv( inventoryPlayer, j + i * 9 + 9, 8 + j * 18 + offsetX, offsetY + i * 18 ) );
				}
			}
		}

		// bind player hotbar
		for( int i = 0; i < 9; i++ )
		{
			if( this.locked.contains( i ) )
			{
				this.addSlotToContainer( new SlotDisabled( inventoryPlayer, i, 8 + i * 18 + offsetX, 58 + offsetY ) );
			}
			else
			{
				this.addSlotToContainer( new SlotPlayerHotBar( inventoryPlayer, i, 8 + i * 18 + offsetX, 58 + offsetY ) );
			}
		}
	}

	@Override
	protected Slot addSlotToContainer( Slot newSlot )
	{
		if( newSlot instanceof AppEngSlot )
		{
			AppEngSlot s = (AppEngSlot) newSlot;
			s.myContainer = this;
			return super.addSlotToContainer( newSlot );
		}
		else
		{
			throw new IllegalArgumentException( "Invalid Slot [" + newSlot + "]for AE Container instead of AppEngSlot." );
		}
	}

	@Override
	public void detectAndSendChanges()
	{
		this.sendCustomName();

		if( Platform.isServer() )
		{
			for( Object crafter : this.crafters )
			{
				ICrafting icrafting = (ICrafting) crafter;

				for( SyncData sd : this.syncData.values() )
				{
					sd.tick( icrafting );
				}
			}
		}

		super.detectAndSendChanges();
	}

	@Override
	public ItemStack transferStackInSlot( EntityPlayer p, int idx )
	{
		if( Platform.isClient() )
		{
			return null;
		}

		boolean hasMETiles = false;
		for( Object is : this.inventorySlots )
		{
			if( is instanceof InternalSlotME )
			{
				hasMETiles = true;
				break;
			}
		}

		if( hasMETiles && Platform.isClient() )
		{
			return null;
		}

		ItemStack tis = null;
		AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get( idx ); // require AE SLots!

		if( clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible )
		{
			return null;
		}
		if( clickSlot != null && clickSlot.getHasStack() )
		{
			tis = clickSlot.getStack();

			if( tis == null )
			{
				return null;
			}

			Collection<Slot> selectedSlots = new ArrayList<Slot>();

			/**
			 * Gather a list of valid destinations.
			 */
			if( clickSlot.isPlayerSide() )
			{
				tis = this.shiftStoreItem( tis );

				// target slots in the container...
				for( Object inventorySlot : this.inventorySlots )
				{
					AppEngSlot cs = (AppEngSlot) inventorySlot;

					if( !( cs.isPlayerSide() ) && !( cs instanceof SlotFake ) && !( cs instanceof SlotCraftingMatrix ) )
					{
						if( cs.isItemValid( tis ) )
						{
							selectedSlots.add( cs );
						}
					}
				}
			}
			else
			{
				// target slots in the container...
				for( Object inventorySlot : this.inventorySlots )
				{
					AppEngSlot cs = (AppEngSlot) inventorySlot;

					if( ( cs.isPlayerSide() ) && !( cs instanceof SlotFake ) && !( cs instanceof SlotCraftingMatrix ) )
					{
						if( cs.isItemValid( tis ) )
						{
							selectedSlots.add( cs );
						}
					}
				}
			}

			/**
			 * Handle Fake Slot Shift clicking.
			 */
			if( selectedSlots.isEmpty() && clickSlot.isPlayerSide() )
			{
				if( tis != null )
				{
					// target slots in the container...
					for( Object inventorySlot : this.inventorySlots )
					{
						AppEngSlot cs = (AppEngSlot) inventorySlot;
						ItemStack destination = cs.getStack();

						if( !( cs.isPlayerSide() ) && cs instanceof SlotFake )
						{
							if( Platform.isSameItemPrecise( destination, tis ) )
							{
								return null;
							}
							else if( destination == null )
							{
								cs.putStack( tis.copy() );
								cs.onSlotChanged();
								this.updateSlot( cs );
								return null;
							}
						}
					}
				}
			}

			if( tis != null )
			{
				// find partials..
				for( Slot d : selectedSlots )
				{
					if( d instanceof SlotDisabled || d instanceof SlotME )
					{
						continue;
					}

					if( d.isItemValid( tis ) )
					{
						if( d.getHasStack() )
						{
							ItemStack t = d.getStack();

							if( Platform.isSameItemPrecise( tis, t ) ) // t.isItemEqual(tis))
							{
								int maxSize = t.getMaxStackSize();
								if( maxSize > d.getSlotStackLimit() )
								{
									maxSize = d.getSlotStackLimit();
								}

								int placeAble = maxSize - t.stackSize;

								if( tis.stackSize < placeAble )
								{
									placeAble = tis.stackSize;
								}

								t.stackSize += placeAble;
								tis.stackSize -= placeAble;

								if( tis.stackSize <= 0 )
								{
									clickSlot.putStack( null );
									d.onSlotChanged();

									// if ( hasMETiles ) updateClient();

									this.updateSlot( clickSlot );
									this.updateSlot( d );
									return null;
								}
								else
								{
									this.updateSlot( d );
								}
							}
						}
					}
				}

				// any match..
				for( Slot d : selectedSlots )
				{
					if( d instanceof SlotDisabled || d instanceof SlotME )
					{
						continue;
					}

					if( d.isItemValid( tis ) )
					{
						if( d.getHasStack() )
						{
							ItemStack t = d.getStack();

							if( Platform.isSameItemPrecise( t, tis ) )
							{
								int maxSize = t.getMaxStackSize();
								if( d.getSlotStackLimit() < maxSize )
								{
									maxSize = d.getSlotStackLimit();
								}

								int placeAble = maxSize - t.stackSize;

								if( tis.stackSize < placeAble )
								{
									placeAble = tis.stackSize;
								}

								t.stackSize += placeAble;
								tis.stackSize -= placeAble;

								if( tis.stackSize <= 0 )
								{
									clickSlot.putStack( null );
									d.onSlotChanged();

									// if ( worldEntity != null )
									// worldEntity.markDirty();
									// if ( hasMETiles ) updateClient();

									this.updateSlot( clickSlot );
									this.updateSlot( d );
									return null;
								}
								else
								{
									this.updateSlot( d );
								}
							}
						}
						else
						{
							int maxSize = tis.getMaxStackSize();
							if( maxSize > d.getSlotStackLimit() )
							{
								maxSize = d.getSlotStackLimit();
							}

							ItemStack tmp = tis.copy();
							if( tmp.stackSize > maxSize )
							{
								tmp.stackSize = maxSize;
							}

							tis.stackSize -= tmp.stackSize;
							d.putStack( tmp );

							if( tis.stackSize <= 0 )
							{
								clickSlot.putStack( null );
								d.onSlotChanged();

								// if ( worldEntity != null )
								// worldEntity.markDirty();
								// if ( hasMETiles ) updateClient();

								this.updateSlot( clickSlot );
								this.updateSlot( d );
								return null;
							}
							else
							{
								this.updateSlot( d );
							}
						}
					}
				}
			}

			clickSlot.putStack( tis != null ? tis.copy() : null );
		}

		this.updateSlot( clickSlot );
		return null;
	}

	@Override
	public final void updateProgressBar( int idx, int value )
	{
		if( this.syncData.containsKey( idx ) )
		{
			this.syncData.get( idx ).update( (long) value );
		}
	}

	@Override
	public boolean canInteractWith( EntityPlayer entityplayer )
	{
		if( this.isContainerValid )
		{
			if( this.tileEntity instanceof IInventory )
			{
				return ( (IInventory) this.tileEntity ).isUseableByPlayer( entityplayer );
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canDragIntoSlot( Slot s )
	{
		return ( (AppEngSlot) s ).isDraggable;
	}

	public void doAction( EntityPlayerMP player, InventoryAction action, int slot, long id )
	{
		if( slot >= 0 && slot < this.inventorySlots.size() )
		{
			Slot s = this.getSlot( slot );

			if( s instanceof SlotCraftingTerm )
			{
				switch( action )
				{
					case CRAFT_SHIFT:
					case CRAFT_ITEM:
					case CRAFT_STACK:
						( (SlotCraftingTerm) s ).doClick( action, player );
						this.updateHeld( player );
					default:
				}
			}

			if( s instanceof SlotFake )
			{
				ItemStack hand = player.inventory.getItemStack();

				switch( action )
				{
					case PICKUP_OR_SET_DOWN:

						if( hand == null )
						{
							s.putStack( null );
						}
						else
						{
							s.putStack( hand.copy() );
						}

						break;
					case PLACE_SINGLE:

						if( hand != null )
						{
							ItemStack is = hand.copy();
							is.stackSize = 1;
							s.putStack( is );
						}

						break;
					case SPLIT_OR_PLACE_SINGLE:

						ItemStack is = s.getStack();
						if( is != null )
						{
							if( hand == null )
							{
								is.stackSize--;
							}
							else if( hand.isItemEqual( is ) )
							{
								is.stackSize = Math.min( is.getMaxStackSize(), is.stackSize + 1 );
							}
							else
							{
								is = hand.copy();
								is.stackSize = 1;
							}

							s.putStack( is );
						}
						else if( hand != null )
						{
							is = hand.copy();
							is.stackSize = 1;
							s.putStack( is );
						}

						break;
					case CREATIVE_DUPLICATE:
					case MOVE_REGION:
					case SHIFT_CLICK:
					default:
						break;
				}
			}

			if( action == InventoryAction.MOVE_REGION )
			{
				Collection<Slot> from = new LinkedList<Slot>();

				for( Object j : this.inventorySlots )
				{
					if( j instanceof Slot && j.getClass() == s.getClass() )
					{
						from.add( (Slot) j );
					}
				}

				for( Slot fr : from )
				{
					this.transferStackInSlot( player, fr.slotNumber );
				}
			}

			return;
		}

		// get target item.
		IAEItemStack slotItem = this.clientRequestedTargetItem;

		switch( action )
		{
			case SHIFT_CLICK:
				if( this.powerSrc == null || this.cellInv == null )
				{
					return;
				}

				if( slotItem != null )
				{
					IAEItemStack ais = slotItem.copy();
					ItemStack myItem = ais.getItemStack();

					ais.setStackSize( myItem.getMaxStackSize() );

					InventoryAdaptor adp = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
					myItem.stackSize = (int) ais.getStackSize();
					myItem = adp.simulateAdd( myItem );

					if( myItem != null )
					{
						ais.setStackSize( ais.getStackSize() - myItem.stackSize );
					}

					ais = Platform.poweredExtraction( this.powerSrc, this.cellInv, ais, this.mySrc );
					if( ais != null )
					{
						adp.addItems( ais.getItemStack() );
					}
				}
				break;
			case ROLL_DOWN:
				if( this.powerSrc == null || this.cellInv == null )
				{
					return;
				}

				int releaseQty = 1;
				ItemStack isg = player.inventory.getItemStack();

				if( isg != null && releaseQty > 0 )
				{
					IAEItemStack ais = AEApi.instance().storage().createItemStack( isg );
					ais.setStackSize( 1 );
					IAEItemStack extracted = ais.copy();

					ais = Platform.poweredInsert( this.powerSrc, this.cellInv, ais, this.mySrc );
					if( ais == null )
					{
						InventoryAdaptor ia = new AdaptorPlayerHand( player );

						ItemStack fail = ia.removeItems( 1, extracted.getItemStack(), null );
						if( fail == null )
						{
							this.cellInv.extractItems( extracted, Actionable.MODULATE, this.mySrc );
						}

						this.updateHeld( player );
					}
				}

				break;
			case ROLL_UP:
			case PICKUP_SINGLE:
				if( this.powerSrc == null || this.cellInv == null )
				{
					return;
				}

				if( slotItem != null )
				{
					int liftQty = 1;
					ItemStack item = player.inventory.getItemStack();

					if( item != null )
					{
						if( item.stackSize >= item.getMaxStackSize() )
						{
							liftQty = 0;
						}
						if( !Platform.isSameItemPrecise( slotItem.getItemStack(), item ) )
						{
							liftQty = 0;
						}
					}

					if( liftQty > 0 )
					{
						IAEItemStack ais = slotItem.copy();
						ais.setStackSize( 1 );
						ais = Platform.poweredExtraction( this.powerSrc, this.cellInv, ais, this.mySrc );
						if( ais != null )
						{
							InventoryAdaptor ia = new AdaptorPlayerHand( player );

							ItemStack fail = ia.addItems( ais.getItemStack() );
							if( fail != null )
							{
								this.cellInv.injectItems( ais, Actionable.MODULATE, this.mySrc );
							}

							this.updateHeld( player );
						}
					}
				}
				break;
			case PICKUP_OR_SET_DOWN:
				if( this.powerSrc == null || this.cellInv == null )
				{
					return;
				}

				if( player.inventory.getItemStack() == null )
				{
					if( slotItem != null )
					{
						IAEItemStack ais = slotItem.copy();
						ais.setStackSize( ais.getItemStack().getMaxStackSize() );
						ais = Platform.poweredExtraction( this.powerSrc, this.cellInv, ais, this.mySrc );
						if( ais != null )
						{
							player.inventory.setItemStack( ais.getItemStack() );
						}
						else
						{
							player.inventory.setItemStack( null );
						}
						this.updateHeld( player );
					}
				}
				else
				{
					IAEItemStack ais = AEApi.instance().storage().createItemStack( player.inventory.getItemStack() );
					ais = Platform.poweredInsert( this.powerSrc, this.cellInv, ais, this.mySrc );
					if( ais != null )
					{
						player.inventory.setItemStack( ais.getItemStack() );
					}
					else
					{
						player.inventory.setItemStack( null );
					}
					this.updateHeld( player );
				}

				break;
			case SPLIT_OR_PLACE_SINGLE:
				if( this.powerSrc == null || this.cellInv == null )
				{
					return;
				}

				if( player.inventory.getItemStack() == null )
				{
					if( slotItem != null )
					{
						IAEItemStack ais = slotItem.copy();
						long maxSize = ais.getItemStack().getMaxStackSize();
						ais.setStackSize( maxSize );
						ais = this.cellInv.extractItems( ais, Actionable.SIMULATE, this.mySrc );

						if( ais != null )
						{
							long stackSize = Math.min( maxSize, ais.getStackSize() );
							ais.setStackSize( ( stackSize + 1 ) >> 1 );
							ais = Platform.poweredExtraction( this.powerSrc, this.cellInv, ais, this.mySrc );
						}

						if( ais != null )
						{
							player.inventory.setItemStack( ais.getItemStack() );
						}
						else
						{
							player.inventory.setItemStack( null );
						}
						this.updateHeld( player );
					}
				}
				else
				{
					IAEItemStack ais = AEApi.instance().storage().createItemStack( player.inventory.getItemStack() );
					ais.setStackSize( 1 );
					ais = Platform.poweredInsert( this.powerSrc, this.cellInv, ais, this.mySrc );
					if( ais == null )
					{
						ItemStack is = player.inventory.getItemStack();
						is.stackSize--;
						if( is.stackSize <= 0 )
						{
							player.inventory.setItemStack( null );
						}
						this.updateHeld( player );
					}
				}

				break;
			case CREATIVE_DUPLICATE:
				if( player.capabilities.isCreativeMode && slotItem != null )
				{
					ItemStack is = slotItem.getItemStack();
					is.stackSize = is.getMaxStackSize();
					player.inventory.setItemStack( is );
					this.updateHeld( player );
				}
				break;
			case MOVE_REGION:

				if( this.powerSrc == null || this.cellInv == null )
				{
					return;
				}

				if( slotItem != null )
				{
					int playerInv = 9 * 4;
					for( int slotNum = 0; slotNum < playerInv; slotNum++ )
					{
						IAEItemStack ais = slotItem.copy();
						ItemStack myItem = ais.getItemStack();

						ais.setStackSize( myItem.getMaxStackSize() );

						InventoryAdaptor adp = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
						myItem.stackSize = (int) ais.getStackSize();
						myItem = adp.simulateAdd( myItem );

						if( myItem != null )
						{
							ais.setStackSize( ais.getStackSize() - myItem.stackSize );
						}

						ais = Platform.poweredExtraction( this.powerSrc, this.cellInv, ais, this.mySrc );
						if( ais != null )
						{
							adp.addItems( ais.getItemStack() );
						}
						else
						{
							return;
						}
					}
				}

				break;
			default:
				break;
		}
	}

	protected void updateHeld( EntityPlayerMP p )
	{
		if( Platform.isServer() )
		{
			try
			{
				NetworkHandler.instance.sendTo( new PacketInventoryAction( InventoryAction.UPDATE_HAND, 0, AEItemStack.create( p.inventory.getItemStack() ) ), p );
			}
			catch( IOException e )
			{
				AELog.error( e );
			}
		}
	}

	public ItemStack shiftStoreItem( ItemStack input )
	{
		if( this.powerSrc == null || this.cellInv == null )
		{
			return input;
		}
		IAEItemStack ais = Platform.poweredInsert( this.powerSrc, this.cellInv, AEApi.instance().storage().createItemStack( input ), this.mySrc );
		if( ais == null )
		{
			return null;
		}
		return ais.getItemStack();
	}

	private void updateSlot( Slot clickSlot )
	{
		// ???
		this.detectAndSendChanges();
	}

	protected void sendCustomName()
	{
		if( !this.sentCustomName )
		{
			this.sentCustomName = true;
			if( Platform.isServer() )
			{
				ICustomNameObject name = null;

				if( this.part instanceof ICustomNameObject )
				{
					name = (ICustomNameObject) this.part;
				}

				if( this.tileEntity instanceof ICustomNameObject )
				{
					name = (ICustomNameObject) this.tileEntity;
				}

				if( this.obj instanceof ICustomNameObject )
				{
					name = (ICustomNameObject) this.obj;
				}

				if( this instanceof ICustomNameObject )
				{
					name = (ICustomNameObject) this;
				}

				if( name != null )
				{
					if( name.hasCustomName() )
					{
						this.customName = name.getCustomName();
					}

					if( this.customName != null )
					{
						try
						{
							NetworkHandler.instance.sendTo( new PacketValueConfig( "CustomName", this.customName ), (EntityPlayerMP) this.invPlayer.player );
						}
						catch( IOException e )
						{
							AELog.error( e );
						}
					}
				}
			}
		}
	}

	public void swapSlotContents( int slotA, int slotB )
	{
		Slot a = this.getSlot( slotA );
		Slot b = this.getSlot( slotB );

		// NPE protection...
		if( a == null || b == null )
		{
			return;
		}

		ItemStack isA = a.getStack();
		ItemStack isB = b.getStack();

		// something to do?
		if( isA == null && isB == null )
		{
			return;
		}

		// can take?

		if( isA != null && !a.canTakeStack( this.invPlayer.player ) )
		{
			return;
		}

		if( isB != null && !b.canTakeStack( this.invPlayer.player ) )
		{
			return;
		}

		// swap valid?

		if( isB != null && !a.isItemValid( isB ) )
		{
			return;
		}

		if( isA != null && !b.isItemValid( isA ) )
		{
			return;
		}

		ItemStack testA = isB == null ? null : isB.copy();
		ItemStack testB = isA == null ? null : isA.copy();

		// can put some back?
		if( testA != null && testA.stackSize > a.getSlotStackLimit() )
		{
			if( testB != null )
			{
				return;
			}

			int totalA = testA.stackSize;
			testA.stackSize = a.getSlotStackLimit();
			testB = testA.copy();

			testB.stackSize = totalA - testA.stackSize;
		}

		if( testB != null && testB.stackSize > b.getSlotStackLimit() )
		{
			if( testA != null )
			{
				return;
			}

			int totalB = testB.stackSize;
			testB.stackSize = b.getSlotStackLimit();
			testA = testB.copy();

			testA.stackSize = totalB - testA.stackSize;
		}

		a.putStack( testA );
		b.putStack( testB );
	}

	public void onUpdate( String field, Object oldValue, Object newValue )
	{

	}

	public void onSlotChange( Slot s )
	{

	}

	public boolean isValidForSlot( Slot s, ItemStack i )
	{
		return true;
	}
}
