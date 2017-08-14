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
import appeng.container.slot.*;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;


public abstract class AEBaseContainer extends Container
{

	private final InventoryPlayer invPlayer;
	private final BaseActionSource mySrc;
	private final HashSet<Integer> locked = new HashSet<Integer>();
	private final TileEntity tileEntity;
	private final IPart part;
	private final IGuiItemObject obj;
	private final List<PacketPartialItem> dataChunks = new LinkedList<PacketPartialItem>();
	private final HashMap<Integer, SyncData> syncData = new HashMap<Integer, SyncData>();
	private boolean isContainerValid = true;
	private String customName;
	private ContainerOpenContext openContext;
	private IMEInventoryHandler<IAEItemStack> cellInv;
	private IEnergySource powerSrc;
	private boolean sentCustomName;
	private int ticksSinceCheck = 900;
	private IAEItemStack clientRequestedTargetItem = null;

	public AEBaseContainer( final InventoryPlayer ip, final TileEntity myTile, final IPart myPart )
	{
		this( ip, myTile, myPart, null );
	}

	public AEBaseContainer( final InventoryPlayer ip, final TileEntity myTile, final IPart myPart, final IGuiItemObject gio )
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
		for( final Field f : this.getClass().getFields() )
		{
			if( f.isAnnotationPresent( GuiSync.class ) )
			{
				final GuiSync annotation = f.getAnnotation( GuiSync.class );
				if( this.syncData.containsKey( annotation.value() ) )
				{
					AELog.warn( "Channel already in use: " + annotation.value() + " for " + f.getName() );
				}
				else
				{
					this.syncData.put( annotation.value(), new SyncData( this, f, annotation ) );
				}
			}
		}
	}

	public AEBaseContainer( final InventoryPlayer ip, final Object anchor )
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

	public void postPartial( final PacketPartialItem packetPartialItem )
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
		for( final PacketPartialItem ppi : this.dataChunks )
		{
			total += ppi.getSize();
		}

		final byte[] buffer = new byte[total];
		int cursor = 0;

		for( final PacketPartialItem ppi : this.dataChunks )
		{
			cursor = ppi.write( buffer, cursor );
		}

		try
		{
			final NBTTagCompound data = CompressedStreamTools.readCompressed( new ByteArrayInputStream( buffer ) );
			if( data != null )
			{
				this.setTargetStack( AEApi.instance().storage().createItemStack( ItemStack.loadItemStackFromNBT( data ) ) );
			}
		}
		catch( final IOException e )
		{
			AELog.debug( e );
		}

		this.dataChunks.clear();
	}

	public IAEItemStack getTargetStack()
	{
		return this.clientRequestedTargetItem;
	}

	public void setTargetStack( final IAEItemStack stack )
	{
		// client doesn't need to re-send, makes for lower overhead rapid packets.
		if( Platform.isClient() )
		{
			final ItemStack a = stack == null ? null : stack.getItemStack();
			final ItemStack b = this.clientRequestedTargetItem == null ? null : this.clientRequestedTargetItem.getItemStack();

			if( Platform.isSameItemPrecise( a, b ) )
			{
				return;
			}

			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			final NBTTagCompound item = new NBTTagCompound();

			if( stack != null )
			{
				stack.writeToNBT( item );
			}

			try
			{
				CompressedStreamTools.writeCompressed( item, stream );

				final int maxChunkSize = 30000;
				final List<byte[]> miniPackets = new LinkedList<byte[]>();

				final byte[] data = stream.toByteArray();

				final ByteArrayInputStream bis = new ByteArrayInputStream( data, 0, stream.size() );
				while( bis.available() > 0 )
				{
					final int nextBLock = bis.available() > maxChunkSize ? maxChunkSize : bis.available();
					final byte[] nextSegment = new byte[nextBLock];
					bis.read( nextSegment );
					miniPackets.add( nextSegment );
				}
				bis.close();
				stream.close();

				int page = 0;
				for( final byte[] packet : miniPackets )
				{
					final PacketPartialItem ppi = new PacketPartialItem( page, miniPackets.size(), packet );
					page++;
					NetworkHandler.instance.sendToServer( ppi );
				}
			}
			catch( final IOException e )
			{
				AELog.debug( e );
				return;
			}
		}

		this.clientRequestedTargetItem = stack == null ? null : stack.copy();
	}

	public BaseActionSource getActionSource()
	{
		return this.mySrc;
	}

	public void verifyPermissions( final SecurityPermissions security, final boolean requirePower )
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
		this.setValidContainer( this.isValidContainer() && this.hasAccess( security, requirePower ) );
	}

	protected boolean hasAccess( final SecurityPermissions perm, final boolean requirePower )
	{
		final IActionHost host = this.getActionHost();

		if( host != null )
		{
			final IGridNode gn = host.getActionableNode();
			if( gn != null )
			{
				final IGrid g = gn.getGrid();
				if( g != null )
				{
					if( requirePower )
					{
						final IEnergyGrid eg = g.getCache( IEnergyGrid.class );
						if( !eg.isNetworkPowered() )
						{
							return false;
						}
					}

					final ISecurityGrid sg = g.getCache( ISecurityGrid.class );
					if( sg.hasPermission( this.getInventoryPlayer().player, perm ) )
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	public void lockPlayerInventorySlot( final int idx )
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
		return this.getInventoryPlayer();
	}

	public TileEntity getTileEntity()
	{
		return this.tileEntity;
	}

	public final void updateFullProgressBar( final int idx, final long value )
	{
		if( this.syncData.containsKey( idx ) )
		{
			this.syncData.get( idx ).update( value );
			return;
		}

		this.updateProgressBar( idx, (int) value );
	}

	public void stringSync( final int idx, final String value )
	{
		if( this.syncData.containsKey( idx ) )
		{
			this.syncData.get( idx ).update( value );
		}
	}

	protected void bindPlayerInventory( final InventoryPlayer inventoryPlayer, final int offsetX, final int offsetY )
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
	protected Slot addSlotToContainer( final Slot newSlot )
	{
		if( newSlot instanceof AppEngSlot )
		{
			final AppEngSlot s = (AppEngSlot) newSlot;
			s.setContainer( this );
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
			for( final Object crafter : this.crafters )
			{
				final ICrafting icrafting = (ICrafting) crafter;

				for( final SyncData sd : this.syncData.values() )
				{
					sd.tick( icrafting );
				}
			}
		}

		super.detectAndSendChanges();
	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer p, final int idx )
	{
		if( Platform.isClient() )
		{
			return null;
		}

		boolean hasMETiles = false;
		for( final Object is : this.inventorySlots )
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

		final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get( idx ); // require AE SLots!

		if( clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible )
		{
			return null;
		}
		if( clickSlot != null && clickSlot.getHasStack() )
		{
			ItemStack tis = clickSlot.getStack();

			if( tis == null )
			{
				return null;
			}

			final List<Slot> selectedSlots = new ArrayList<Slot>();

			/**
			 * Gather a list of valid destinations.
			 */
			if( clickSlot.isPlayerSide() )
			{
				tis = this.shiftStoreItem( tis );

				// target slots in the container...
				for( final Object inventorySlot : this.inventorySlots )
				{
					final AppEngSlot cs = (AppEngSlot) inventorySlot;

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
				for( final Object inventorySlot : this.inventorySlots )
				{
					final AppEngSlot cs = (AppEngSlot) inventorySlot;

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
					for( final Object inventorySlot : this.inventorySlots )
					{
						final AppEngSlot cs = (AppEngSlot) inventorySlot;
						final ItemStack destination = cs.getStack();

						if( !( cs.isPlayerSide() ) && cs instanceof SlotFake )
						{
							if( Platform.isSameItemPrecise( destination, tis ) )
							{
								break;
							}
							else if( destination == null )
							{
								cs.putStack( tis.copy() );
								cs.onSlotChanged();
								this.updateSlot( cs );
								break;
							}
						}
					}
				}
			}

			if( tis != null )
			{
				// find partials..
				for( final Slot d : selectedSlots )
				{
					if( d instanceof SlotDisabled || d instanceof SlotME )
					{
						continue;
					}

					if( d.isItemValid( tis ) )
					{
						if( d.getHasStack() )
						{
							final ItemStack t = d.getStack();

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
				for( final Slot d : selectedSlots )
				{
					if( d instanceof SlotDisabled || d instanceof SlotME )
					{
						continue;
					}

					if( d.isItemValid( tis ) )
					{
						if( d.getHasStack() )
						{
							final ItemStack t = d.getStack();

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

							final ItemStack tmp = tis.copy();
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
	public final void updateProgressBar( final int idx, final int value )
	{
		if( this.syncData.containsKey( idx ) )
		{
			this.syncData.get( idx ).update( (long) value );
		}
	}

	@Override
	public boolean canInteractWith( final EntityPlayer entityplayer )
	{
		if( this.isValidContainer() )
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
	public boolean canDragIntoSlot( final Slot s )
	{
		return ( (AppEngSlot) s ).isDraggable();
	}

	public void doAction( final EntityPlayerMP player, final InventoryAction action, final int slot, final long id )
	{
		if( slot >= 0 && slot < this.inventorySlots.size() )
		{
			final Slot s = this.getSlot( slot );

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
				final ItemStack hand = player.inventory.getItemStack();

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
							final ItemStack is = hand.copy();
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
				if( s instanceof SlotPatternTerm )
					return;
				//SlotPatternTerm should't transfer its ItemStack to the Player

				final List<Slot> from = new LinkedList<Slot>();

				for( final Object j : this.inventorySlots )
				{
					if( j instanceof Slot && j.getClass() == s.getClass() )
					{
						from.add( (Slot) j );
					}
				}

				for( final Slot fr : from )
				{
					this.transferStackInSlot( player, fr.slotNumber );
				}
			}

			return;
		}

		// get target item.
		final IAEItemStack slotItem = this.clientRequestedTargetItem;

		switch( action )
		{
			case SHIFT_CLICK:
				if( this.getPowerSource() == null || this.getCellInventory() == null )
				{
					return;
				}

				if( slotItem != null )
				{
					IAEItemStack ais = slotItem.copy();
					ItemStack myItem = ais.getItemStack();

					ais.setStackSize( myItem.getMaxStackSize() );

					final InventoryAdaptor adp = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
					myItem.stackSize = (int) ais.getStackSize();
					myItem = adp.simulateAdd( myItem );

					if( myItem != null )
					{
						ais.setStackSize( ais.getStackSize() - myItem.stackSize );
					}

					ais = Platform.poweredExtraction( this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource() );
					if( ais != null )
					{
						adp.addItems( ais.getItemStack() );
					}
				}
				break;
			case ROLL_DOWN:
				if( this.getPowerSource() == null || this.getCellInventory() == null )
				{
					return;
				}

				final int releaseQty = 1;
				final ItemStack isg = player.inventory.getItemStack();

				if( isg != null && releaseQty > 0 )
				{
					IAEItemStack ais = AEApi.instance().storage().createItemStack( isg );
					ais.setStackSize( 1 );
					final IAEItemStack extracted = ais.copy();

					ais = Platform.poweredInsert( this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource() );
					if( ais == null )
					{
						final InventoryAdaptor ia = new AdaptorPlayerHand( player );

						final ItemStack fail = ia.removeItems( 1, extracted.getItemStack(), null );
						if( fail == null )
						{
							this.getCellInventory().extractItems( extracted, Actionable.MODULATE, this.getActionSource() );
						}

						this.updateHeld( player );
					}
				}

				break;
			case ROLL_UP:
			case PICKUP_SINGLE:
				if( this.getPowerSource() == null || this.getCellInventory() == null )
				{
					return;
				}

				if( slotItem != null )
				{
					int liftQty = 1;
					final ItemStack item = player.inventory.getItemStack();

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
						ais = Platform.poweredExtraction( this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource() );
						if( ais != null )
						{
							final InventoryAdaptor ia = new AdaptorPlayerHand( player );

							final ItemStack fail = ia.addItems( ais.getItemStack() );
							if( fail != null )
							{
								this.getCellInventory().injectItems( ais, Actionable.MODULATE, this.getActionSource() );
							}

							this.updateHeld( player );
						}
					}
				}
				break;
			case PICKUP_OR_SET_DOWN:
				if( this.getPowerSource() == null || this.getCellInventory() == null )
				{
					return;
				}

				if( player.inventory.getItemStack() == null )
				{
					if( slotItem != null )
					{
						IAEItemStack ais = slotItem.copy();
						ais.setStackSize( ais.getItemStack().getMaxStackSize() );
						ais = Platform.poweredExtraction( this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource() );
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
					ais = Platform.poweredInsert( this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource() );
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
				if( this.getPowerSource() == null || this.getCellInventory() == null )
				{
					return;
				}

				if( player.inventory.getItemStack() == null )
				{
					if( slotItem != null )
					{
						IAEItemStack ais = slotItem.copy();
						final long maxSize = ais.getItemStack().getMaxStackSize();
						ais.setStackSize( maxSize );
						ais = this.getCellInventory().extractItems( ais, Actionable.SIMULATE, this.getActionSource() );

						if( ais != null )
						{
							final long stackSize = Math.min( maxSize, ais.getStackSize() );
							ais.setStackSize( ( stackSize + 1 ) >> 1 );
							ais = Platform.poweredExtraction( this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource() );
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
					ais = Platform.poweredInsert( this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource() );
					if( ais == null )
					{
						final ItemStack is = player.inventory.getItemStack();
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
					final ItemStack is = slotItem.getItemStack();
					is.stackSize = is.getMaxStackSize();
					player.inventory.setItemStack( is );
					this.updateHeld( player );
				}
				break;
			case MOVE_REGION:

				if( this.getPowerSource() == null || this.getCellInventory() == null )
				{
					return;
				}

				if( slotItem != null )
				{
					final int playerInv = 9 * 4;
					for( int slotNum = 0; slotNum < playerInv; slotNum++ )
					{
						IAEItemStack ais = slotItem.copy();
						ItemStack myItem = ais.getItemStack();

						ais.setStackSize( myItem.getMaxStackSize() );

						final InventoryAdaptor adp = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
						myItem.stackSize = (int) ais.getStackSize();
						myItem = adp.simulateAdd( myItem );

						if( myItem != null )
						{
							ais.setStackSize( ais.getStackSize() - myItem.stackSize );
						}

						ais = Platform.poweredExtraction( this.getPowerSource(), this.getCellInventory(), ais, this.getActionSource() );
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

	protected void updateHeld( final EntityPlayerMP p )
	{
		if( Platform.isServer() )
		{
			try
			{
				NetworkHandler.instance.sendTo( new PacketInventoryAction( InventoryAction.UPDATE_HAND, 0, AEItemStack.create( p.inventory.getItemStack() ) ), p );
			}
			catch( final IOException e )
			{
				AELog.debug( e );
			}
		}
	}

	private ItemStack shiftStoreItem( final ItemStack input )
	{
		if( this.getPowerSource() == null || this.getCellInventory() == null )
		{
			return input;
		}
		final IAEItemStack ais = Platform.poweredInsert( this.getPowerSource(), this.getCellInventory(), AEApi.instance().storage().createItemStack( input ), this.getActionSource() );
		if( ais == null )
		{
			return null;
		}
		return ais.getItemStack();
	}

	private void updateSlot( final Slot clickSlot )
	{
		// ???
		this.detectAndSendChanges();
	}

	private void sendCustomName()
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
						this.setCustomName( name.getCustomName() );
					}

					if( this.getCustomName() != null )
					{
						try
						{
							NetworkHandler.instance.sendTo( new PacketValueConfig( "CustomName", this.getCustomName() ), (EntityPlayerMP) this.getInventoryPlayer().player );
						}
						catch( final IOException e )
						{
							AELog.debug( e );
						}
					}
				}
			}
		}
	}

	public void swapSlotContents( final int slotA, final int slotB )
	{
		final Slot a = this.getSlot( slotA );
		final Slot b = this.getSlot( slotB );

		// NPE protection...
		if( a == null || b == null )
		{
			return;
		}

		final ItemStack isA = a.getStack();
		final ItemStack isB = b.getStack();

		// something to do?
		if( isA == null && isB == null )
		{
			return;
		}

		// can take?

		if( isA != null && !a.canTakeStack( this.getInventoryPlayer().player ) )
		{
			return;
		}

		if( isB != null && !b.canTakeStack( this.getInventoryPlayer().player ) )
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

			final int totalA = testA.stackSize;
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

			final int totalB = testB.stackSize;
			testB.stackSize = b.getSlotStackLimit();
			testA = testB.copy();

			testA.stackSize = totalB - testA.stackSize;
		}

		a.putStack( testA );
		b.putStack( testB );
	}

	public void onUpdate( final String field, final Object oldValue, final Object newValue )
	{

	}

	public void onSlotChange( final Slot s )
	{

	}

	public boolean isValidForSlot( final Slot s, final ItemStack i )
	{
		return true;
	}

	public IMEInventoryHandler<IAEItemStack> getCellInventory()
	{
		return this.cellInv;
	}

	public void setCellInventory( final IMEInventoryHandler<IAEItemStack> cellInv )
	{
		this.cellInv = cellInv;
	}

	public String getCustomName()
	{
		return this.customName;
	}

	public void setCustomName( final String customName )
	{
		this.customName = customName;
	}

	public InventoryPlayer getInventoryPlayer()
	{
		return this.invPlayer;
	}

	public boolean isValidContainer()
	{
		return this.isContainerValid;
	}

	public void setValidContainer( final boolean isContainerValid )
	{
		this.isContainerValid = isContainerValid;
	}

	public ContainerOpenContext getOpenContext()
	{
		return this.openContext;
	}

	public void setOpenContext( final ContainerOpenContext openContext )
	{
		this.openContext = openContext;
	}

	public IEnergySource getPowerSource()
	{
		return this.powerSrc;
	}

	public void setPowerSource( final IEnergySource powerSrc )
	{
		this.powerSrc = powerSrc;
	}
}
