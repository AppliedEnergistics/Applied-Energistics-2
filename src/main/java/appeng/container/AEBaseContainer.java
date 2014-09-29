package appeng.container;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import appeng.container.slot.*;
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
import appeng.container.slot.SlotInaccessible;
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
	final TileEntity tileEntity;
	final IPart part;
	final IGuiItemObject obj;

	final protected BaseActionSource mySrc;
	public boolean isContainerValid = true;

	boolean sentCustomName;
	public String customName;

	int ticksSinceCheck = 900;

	IAEItemStack clientRequestedTargetItem = null;
	List<PacketPartialItem> dataChunks = new LinkedList<PacketPartialItem>();

	public void postPartial(PacketPartialItem packetPartialItem)
	{
		dataChunks.add( packetPartialItem );
		if ( packetPartialItem.getPageCount() == dataChunks.size() )
			parsePartials();
	}

	private void parsePartials()
	{
		int total = 0;
		for (PacketPartialItem ppi : dataChunks)
			total += ppi.getSize();

		byte[] buffer = new byte[total];
		int cursor = 0;

		for (PacketPartialItem ppi : dataChunks)
			cursor = ppi.write( buffer, cursor );

		try
		{
			NBTTagCompound data = CompressedStreamTools.readCompressed( new ByteArrayInputStream( buffer ) );
			if ( data != null )
				setTargetStack( AEApi.instance().storage().createItemStack( ItemStack.loadItemStackFromNBT( data ) ) );
		}
		catch (IOException e)
		{
			AELog.error( e );
		}

		dataChunks.clear();
	}

	public void setTargetStack(IAEItemStack stack)
	{
		// client doesn't need to re-send, makes for lower overhead rapid packets.
		if ( Platform.isClient() )
		{
			ItemStack a = stack == null ? null : stack.getItemStack();
			ItemStack b = clientRequestedTargetItem == null ? null : clientRequestedTargetItem.getItemStack();

			if ( Platform.isSameItemPrecise( a, b ) )
				return;

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			NBTTagCompound item = new NBTTagCompound();

			if ( stack != null )
				stack.writeToNBT( item );

			try
			{
				CompressedStreamTools.writeCompressed( item, stream );

				int maxChunkSize = 30000;
				List<byte[]> miniPackets = new LinkedList<byte[]>();

				byte[] data = stream.toByteArray();

				ByteArrayInputStream bis = new ByteArrayInputStream( data, 0, stream.size() );
				while (bis.available() > 0)
				{
					int nextBLock = bis.available() > maxChunkSize ? maxChunkSize : bis.available();
					byte[] nextSegment = new byte[nextBLock];
					bis.read( nextSegment );
					miniPackets.add( nextSegment );
				}
				bis.close();
				stream.close();

				int page = 0;
				for (byte[] packet : miniPackets)
				{
					PacketPartialItem ppi = new PacketPartialItem( page++, miniPackets.size(), packet );
					NetworkHandler.instance.sendToServer( ppi );
				}
			}
			catch (IOException e)
			{
				AELog.error( e );
				return;
			}
		}

		clientRequestedTargetItem = stack == null ? null : stack.copy();
	}

	public IAEItemStack getTargetStack()
	{
		return clientRequestedTargetItem;
	}

	public BaseActionSource getSource()
	{
		return mySrc;
	}

	public void verifyPermissions(SecurityPermissions security, boolean requirePower)
	{
		if ( Platform.isClient() )
			return;

		ticksSinceCheck++;
		if ( ticksSinceCheck < 20 )
			return;

		ticksSinceCheck = 0;
		isContainerValid = isContainerValid && hasAccess( security, requirePower );
	}

	protected boolean hasAccess(SecurityPermissions perm, boolean requirePower)
	{
		IActionHost host = getActionHost();

		if ( host != null )
		{
			IGridNode gn = host.getActionableNode();
			if ( gn != null )
			{
				IGrid g = gn.getGrid();
				if ( g != null )
				{
					if ( requirePower )
					{
						IEnergyGrid eg = g.getCache( IEnergyGrid.class );
						if ( !eg.isNetworkPowered() )
							return false;
					}

					ISecurityGrid sg = g.getCache( ISecurityGrid.class );
					if ( sg.hasPermission( invPlayer.player, perm ) )
						return true;
				}
			}
		}

		return false;
	}

	public ContainerOpenContext openContext;

	protected IMEInventoryHandler<IAEItemStack> cellInv;
	protected HashSet<Integer> locked = new HashSet<Integer>();
	protected IEnergySource powerSrc;

	public void lockPlayerInventorySlot(int idx)
	{
		locked.add( idx );
	}

	public Object getTarget()
	{
		if ( tileEntity != null )
			return tileEntity;
		if ( part != null )
			return part;
		if ( obj != null )
			return obj;
		return null;
	}

	public AEBaseContainer(InventoryPlayer ip, TileEntity myTile, IPart myPart) {
		this( ip, myTile, myPart, null );
	}

	public AEBaseContainer(InventoryPlayer ip, TileEntity myTile, IPart myPart, IGuiItemObject gio) {
		invPlayer = ip;
		tileEntity = myTile;
		part = myPart;
		obj = gio;
		mySrc = new PlayerSource( ip.player, getActionHost() );
		prepareSync();
	}

	public AEBaseContainer(InventoryPlayer ip, Object anchor) {
		invPlayer = ip;
		tileEntity = anchor instanceof TileEntity ? (TileEntity) anchor : null;
		part = anchor instanceof IPart ? (IPart) anchor : null;
		obj = anchor instanceof IGuiItemObject ? (IGuiItemObject) anchor : null;

		if ( tileEntity == null && part == null && obj == null )
			throw new RuntimeException( "Must have a valid anchor" );

		mySrc = new PlayerSource( ip.player, getActionHost() );

		prepareSync();
	}

	protected IActionHost getActionHost()
	{
		if ( obj instanceof IActionHost )
			return (IActionHost) obj;

		if ( tileEntity instanceof IActionHost )
			return (IActionHost) tileEntity;

		if ( part instanceof IActionHost )
			return (IActionHost) part;

		return null;
	}

	@Override
	public boolean canDragIntoSlot(Slot s)
	{
		return ((AppEngSlot) s).isDraggable;
	}

	public InventoryPlayer getPlayerInv()
	{
		return invPlayer;
	}

	public TileEntity getTileEntity()
	{
		return tileEntity;
	}

	@Override
	protected Slot addSlotToContainer(Slot newSlot)
	{
		if ( newSlot instanceof AppEngSlot )
		{
			AppEngSlot s = (AppEngSlot) newSlot;
			s.myContainer = this;
			return super.addSlotToContainer( newSlot );
		}
		else
			throw new RuntimeException( "Invalid Slot for AE Container." );
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		if ( isContainerValid )
		{
			if ( tileEntity instanceof IInventory )
				return ((IInventory) tileEntity).isUseableByPlayer( entityplayer );
			return true;
		}
		return false;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer p, int idx)
	{
		if ( Platform.isClient() )
			return null;

		boolean hasMETiles = false;
		for (Object is : this.inventorySlots)
		{
			if ( is instanceof InternalSlotME )
			{
				hasMETiles = true;
				break;
			}
		}

		if ( hasMETiles && Platform.isClient() )
		{
			return null;
		}

		ItemStack tis = null;
		AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get( idx ); // require AE SLots!

		if ( clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessible )
			return null;
		if ( clickSlot != null && clickSlot.getHasStack() )
		{
			tis = clickSlot.getStack();

			if ( tis == null )
				return null;

			List<Slot> selectedSlots = new ArrayList<Slot>();

			/**
			 * Gather a list of valid destinations.
			 */
			if ( clickSlot.isPlayerSide() )
			{
				tis = shiftStoreItem( tis );

				// target slots in the container...
				for (int x = 0; x < this.inventorySlots.size(); x++)
				{
					AppEngSlot cs = (AppEngSlot) this.inventorySlots.get( x );

					if ( !(cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof SlotCraftingMatrix) )
					{
						if ( cs.isItemValid( tis ) )
							selectedSlots.add( cs );
					}
				}
			}
			else
			{
				// target slots in the container...
				for (int x = 0; x < this.inventorySlots.size(); x++)
				{
					AppEngSlot cs = (AppEngSlot) this.inventorySlots.get( x );

					if ( (cs.isPlayerSide()) && !(cs instanceof SlotFake) && !(cs instanceof SlotCraftingMatrix) )
					{
						if ( cs.isItemValid( tis ) )
							selectedSlots.add( cs );
					}
				}
			}

			/**
			 * Handle Fake Slot Shift clicking.
			 */
			if ( selectedSlots.isEmpty() && clickSlot.isPlayerSide() )
			{
				if ( tis != null )
				{
					// target slots in the container...
					for (int x = 0; x < this.inventorySlots.size(); x++)
					{
						AppEngSlot cs = (AppEngSlot) this.inventorySlots.get( x );
						ItemStack dest = cs.getStack();

						if ( !(cs.isPlayerSide()) && cs instanceof SlotFake )
						{
							if ( Platform.isSameItemPrecise( dest, tis ) )
								return null;
							else if ( dest == null )
							{
								cs.putStack( tis.copy() );
								cs.onSlotChanged();
								updateSlot( cs );
								return null;
							}
						}
					}
				}
			}

			if ( tis != null )
			{
				// find partials..
				for (Slot d : selectedSlots)
				{
					if ( d instanceof SlotDisabled || d instanceof SlotME )
						continue;

					if ( d.isItemValid( tis ) )
					{
						if ( d.getHasStack() )
						{
							ItemStack t = d.getStack();

							if ( Platform.isSameItemPrecise( tis, t ) ) // t.isItemEqual(tis))
							{
								int maxSize = t.getMaxStackSize();
								if ( maxSize > d.getSlotStackLimit() )
									maxSize = d.getSlotStackLimit();

								int placeAble = maxSize - t.stackSize;

								if ( tis.stackSize < placeAble )
								{
									placeAble = tis.stackSize;
								}

								t.stackSize += placeAble;
								tis.stackSize -= placeAble;

								if ( tis.stackSize <= 0 )
								{
									clickSlot.putStack( null );
									d.onSlotChanged();

									// if ( hasMETiles ) updateClient();

									updateSlot( clickSlot );
									updateSlot( d );
									return null;
								}
								else
									updateSlot( d );
							}
						}
					}
				}

				// any match..
				for (Slot d : selectedSlots)
				{
					if ( d instanceof SlotDisabled || d instanceof SlotME )
						continue;

					if ( d.isItemValid( tis ) )
					{
						if ( d.getHasStack() )
						{
							ItemStack t = d.getStack();

							if ( Platform.isSameItemPrecise( t, tis ) )
							{
								int maxSize = t.getMaxStackSize();
								if ( d.getSlotStackLimit() < maxSize )
									maxSize = d.getSlotStackLimit();

								int placeAble = maxSize - t.stackSize;

								if ( tis.stackSize < placeAble )
								{
									placeAble = tis.stackSize;
								}

								t.stackSize += placeAble;
								tis.stackSize -= placeAble;

								if ( tis.stackSize <= 0 )
								{
									clickSlot.putStack( null );
									d.onSlotChanged();

									// if ( worldEntity != null )
									// worldEntity.markDirty();
									// if ( hasMETiles ) updateClient();

									updateSlot( clickSlot );
									updateSlot( d );
									return null;
								}
								else
									updateSlot( d );
							}
						}
						else
						{
							int maxSize = tis.getMaxStackSize();
							if ( maxSize > d.getSlotStackLimit() )
								maxSize = d.getSlotStackLimit();

							ItemStack tmp = tis.copy();
							if ( tmp.stackSize > maxSize )
								tmp.stackSize = maxSize;

							tis.stackSize -= tmp.stackSize;
							d.putStack( tmp );

							if ( tis.stackSize <= 0 )
							{
								clickSlot.putStack( null );
								d.onSlotChanged();

								// if ( worldEntity != null )
								// worldEntity.markDirty();
								// if ( hasMETiles ) updateClient();

								updateSlot( clickSlot );
								updateSlot( d );
								return null;
							}
							else
								updateSlot( d );
						}
					}
				}
			}

			clickSlot.putStack( tis != null ? tis.copy() : null );
		}

		updateSlot( clickSlot );
		return null;
	}

	private void updateSlot(Slot clickSlot)
	{
		// ???
		detectAndSendChanges();
	}

	HashMap<Integer, SyncData> syncData = new HashMap<Integer, SyncData>();

	@Override
	public void detectAndSendChanges()
	{
		sendCustomName();

		if ( Platform.isServer() )
		{
			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				for (SyncData sd : syncData.values())
					sd.tick( icrafting );
			}
		}

		super.detectAndSendChanges();
	}

	@Override
	final public void updateProgressBar(int idx, int value)
	{
		if ( syncData.containsKey( idx ) )
		{
			syncData.get( idx ).update( (long) value );
			return;
		}

	}

	final public void updateFullProgressBar(int idx, long value)
	{
		if ( syncData.containsKey( idx ) )
		{
			syncData.get( idx ).update( value );
			return;
		}

		updateProgressBar( idx, (int) value );
	}

	public void stringSync(int idx, String value)
	{
		if ( syncData.containsKey( idx ) )
		{
			syncData.get( idx ).update( value );
		}
	}

	private void prepareSync()
	{
		for (Field f : getClass().getFields())
		{
			if ( f.isAnnotationPresent( GuiSync.class ) )
			{
				GuiSync annotation = f.getAnnotation( GuiSync.class );
				if ( syncData.containsKey( annotation.value() ) )
					AELog.warning( "Channel already in use: " + annotation.value() + " for " + f.getName() );
				else
					syncData.put( annotation.value(), new SyncData( this, f, annotation ) );
			}
		}
	}

	protected void sendCustomName()
	{
		if ( !sentCustomName )
		{
			sentCustomName = true;
			if ( Platform.isServer() )
			{
				ICustomNameObject name = null;

				if ( part instanceof ICustomNameObject )
					name = (ICustomNameObject) part;

				if ( tileEntity instanceof ICustomNameObject )
					name = (ICustomNameObject) tileEntity;

				if ( obj instanceof ICustomNameObject )
					name = (ICustomNameObject) obj;

				if ( this instanceof ICustomNameObject )
					name = (ICustomNameObject) this;

				if ( name != null )
				{
					if ( name.hasCustomName() )
						customName = name.getCustomName();

					if ( customName != null )
					{
						try
						{
							NetworkHandler.instance.sendTo( new PacketValueConfig( "CustomName", customName ), (EntityPlayerMP) invPlayer.player );
						}
						catch (IOException e)
						{
							AELog.error( e );
						}
					}
				}
			}
		}
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer, int offset_x, int offset_y)
	{
		for (int i = 0; i < 9; i++)
		{
			if ( locked.contains( i ) )
				addSlotToContainer( new SlotDisabled( inventoryPlayer, i, 8 + i * 18 + offset_x, 58 + offset_y ) );
			else
				addSlotToContainer( new SlotPlayerHotBar( inventoryPlayer, i, 8 + i * 18 + offset_x, 58 + offset_y ) );
		}

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				if ( locked.contains( j + i * 9 + 9 ) )
					addSlotToContainer( new SlotDisabled( inventoryPlayer, j + i * 9 + 9, 8 + j * 18 + offset_x, offset_y + i * 18 ) );
				else
					addSlotToContainer( new SlotPlayerInv( inventoryPlayer, j + i * 9 + 9, 8 + j * 18 + offset_x, offset_y + i * 18 ) );
			}
		}
	}

	public ItemStack shiftStoreItem(ItemStack input)
	{
		if ( powerSrc == null || cellInv == null )
			return input;
		IAEItemStack ais = Platform.poweredInsert( powerSrc, cellInv, AEApi.instance().storage().createItemStack( input ), mySrc );
		if ( ais == null )
			return null;
		return ais.getItemStack();
	}

	public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id)
	{
		if ( slot >= 0 && slot < inventorySlots.size() )
		{
			Slot s = getSlot( slot );

			if ( s instanceof SlotCraftingTerm )
			{
				switch (action)
				{
				case CRAFT_SHIFT:
				case CRAFT_ITEM:
				case CRAFT_STACK:
					((SlotCraftingTerm) s).doClick( action, player );
					updateHeld( player );
				default:
				}
			}

			if ( s instanceof SlotFake )
			{
				ItemStack hand = player.inventory.getItemStack();

				switch (action)
				{
				case PICKUP_OR_SET_DOWN:

					if ( hand == null )
						s.putStack( null );
					else
						s.putStack( hand.copy() );

					break;
				case PLACE_SINGLE:

					if ( hand != null )
					{
						ItemStack is = hand.copy();
						is.stackSize = 1;
						s.putStack( is );
					}

					break;
				case SPLIT_OR_PLACE_SINGLE:

					ItemStack is = s.getStack();
					if ( is != null )
					{
						if ( hand == null )
							is.stackSize--;
						else if ( hand.isItemEqual( is ) )
							is.stackSize = Math.min( is.getMaxStackSize(), is.stackSize + 1 );
						else
						{
							is = hand.copy();
							is.stackSize = 1;
						}

						s.putStack( is );
					}
					else if ( hand != null )
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

			if ( action == InventoryAction.MOVE_REGION )
			{
				List<Slot> from = new LinkedList<Slot>();

				for (Object j : inventorySlots)
				{
					if ( j instanceof Slot && j.getClass() == s.getClass() )
						from.add( (Slot) j );
				}

				for (Slot fr : from)
					transferStackInSlot( player, fr.slotNumber );
			}

			return;
		}

		// get target item.
		IAEItemStack slotItem = getTargetStack();

		switch (action)
		{
		case SHIFT_CLICK:
			if ( powerSrc == null || cellInv == null )
				return;

			if ( slotItem != null )
			{
				IAEItemStack ais = slotItem.copy();
				ItemStack myItem = ais.getItemStack();

				ais.setStackSize( myItem.getMaxStackSize() );

				InventoryAdaptor adp = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
				myItem.stackSize = (int) ais.getStackSize();
				myItem = adp.simulateAdd( myItem );

				if ( myItem != null )
					ais.setStackSize( ais.getStackSize() - myItem.stackSize );

				ais = Platform.poweredExtraction( powerSrc, cellInv, ais, mySrc );
				if ( ais != null )
					adp.addItems( ais.getItemStack() );
			}
			break;
		case ROLL_DOWN:
			if ( powerSrc == null || cellInv == null )
				return;

			int releaseQty = 1;
			ItemStack isg = player.inventory.getItemStack();

			if ( isg != null && releaseQty > 0 )
			{
				IAEItemStack ais = AEApi.instance().storage().createItemStack( isg );
				ais.setStackSize( 1 );
				IAEItemStack extracted = ais.copy();

				ais = Platform.poweredInsert( powerSrc, cellInv, ais, mySrc );
				if ( ais == null )
				{
					InventoryAdaptor ia = new AdaptorPlayerHand( player );

					ItemStack fail = ia.removeItems( 1, extracted.getItemStack(), null );
					if ( fail == null )
						cellInv.extractItems( extracted, Actionable.MODULATE, mySrc );

					updateHeld( player );
				}
			}

			break;
		case ROLL_UP:
		case PICKUP_SINGLE:
			if ( powerSrc == null || cellInv == null )
				return;

			if ( slotItem != null )
			{
				int liftQty = 1;
				ItemStack item = player.inventory.getItemStack();

				if ( item != null )
				{
					if ( item.stackSize >= item.getMaxStackSize() )
						liftQty = 0;
					if ( !Platform.isSameItemPrecise( slotItem.getItemStack(), item ) )
						liftQty = 0;
				}

				if ( liftQty > 0 )
				{
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize( 1 );
					ais = Platform.poweredExtraction( powerSrc, cellInv, ais, mySrc );
					if ( ais != null )
					{
						InventoryAdaptor ia = new AdaptorPlayerHand( player );

						ItemStack fail = ia.addItems( ais.getItemStack() );
						if ( fail != null )
							cellInv.injectItems( ais, Actionable.MODULATE, mySrc );

						updateHeld( player );
					}
				}
			}
			break;
		case PICKUP_OR_SET_DOWN:
			if ( powerSrc == null || cellInv == null )
				return;

			if ( player.inventory.getItemStack() == null )
			{
				if ( slotItem != null )
				{
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize( ais.getItemStack().getMaxStackSize() );
					ais = Platform.poweredExtraction( powerSrc, cellInv, ais, mySrc );
					if ( ais != null )
						player.inventory.setItemStack( ais.getItemStack() );
					else
						player.inventory.setItemStack( null );
					updateHeld( player );
				}
			}
			else
			{
				IAEItemStack ais = AEApi.instance().storage().createItemStack( player.inventory.getItemStack() );
				ais = Platform.poweredInsert( powerSrc, cellInv, ais, mySrc );
				if ( ais != null )
					player.inventory.setItemStack( ais.getItemStack() );
				else
					player.inventory.setItemStack( null );
				updateHeld( player );
			}

			break;
		case SPLIT_OR_PLACE_SINGLE:
			if ( powerSrc == null || cellInv == null )
				return;

			if ( player.inventory.getItemStack() == null )
			{
				if ( slotItem != null )
				{
					IAEItemStack ais = slotItem.copy();
					long maxSize = ais.getItemStack().getMaxStackSize();
					ais.setStackSize( maxSize );
					ais = cellInv.extractItems( ais, Actionable.SIMULATE, mySrc );

					if ( ais != null )
					{
						long stackSize = Math.min( maxSize, ais.getStackSize() );
						ais.setStackSize( (stackSize + 1) >> 1 );
						ais = Platform.poweredExtraction( powerSrc, cellInv, ais, mySrc );
					}

					if ( ais != null )
						player.inventory.setItemStack( ais.getItemStack() );
					else
						player.inventory.setItemStack( null );
					updateHeld( player );
				}
			}
			else
			{
				IAEItemStack ais = AEApi.instance().storage().createItemStack( player.inventory.getItemStack() );
				ais.setStackSize( 1 );
				ais = Platform.poweredInsert( powerSrc, cellInv, ais, mySrc );
				if ( ais == null )
				{
					ItemStack is = player.inventory.getItemStack();
					is.stackSize--;
					if ( is.stackSize <= 0 )
						player.inventory.setItemStack( null );
					updateHeld( player );
				}
			}

			break;
		case CREATIVE_DUPLICATE:
			if ( player.capabilities.isCreativeMode && slotItem != null )
			{
				ItemStack is = slotItem.getItemStack();
				is.stackSize = is.getMaxStackSize();
				player.inventory.setItemStack( is );
				updateHeld( player );
			}
			break;
		case MOVE_REGION:

			if ( powerSrc == null || cellInv == null )
				return;

			if ( slotItem != null )
			{
				int playerInv = 9 * 4;
				for (int slotNum = 0; slotNum < playerInv; slotNum++)
				{
					IAEItemStack ais = slotItem.copy();
					ItemStack myItem = ais.getItemStack();

					ais.setStackSize( myItem.getMaxStackSize() );

					InventoryAdaptor adp = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
					myItem.stackSize = (int) ais.getStackSize();
					myItem = adp.simulateAdd( myItem );

					if ( myItem != null )
						ais.setStackSize( ais.getStackSize() - myItem.stackSize );

					ais = Platform.poweredExtraction( powerSrc, cellInv, ais, mySrc );
					if ( ais != null )
						adp.addItems( ais.getItemStack() );
					else
						return;
				}
			}

			break;
		default:
			break;
		}
	}

	protected void updateHeld(EntityPlayerMP p)
	{
		if ( Platform.isServer() )
		{
			try
			{
				NetworkHandler.instance.sendTo( new PacketInventoryAction( InventoryAction.UPDATE_HAND, 0, AEItemStack.create( p.inventory.getItemStack() ) ),
						p );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
	}

	public void swapSlotContents(int slotA, int slotB)
	{
		Slot a = getSlot( slotA );
		Slot b = getSlot( slotB );

		// NPE protection...
		if ( a == null || b == null )
			return;

		ItemStack isA = a.getStack();
		ItemStack isB = b.getStack();

		// something to do?
		if ( isA == null && isB == null )
			return;

		// can take?

		if ( isA != null && !a.canTakeStack( invPlayer.player ) )
			return;

		if ( isB != null && !b.canTakeStack( invPlayer.player ) )
			return;

		// swap valid?

		if ( isB != null && !a.isItemValid( isB ) )
			return;

		if ( isA != null && !b.isItemValid( isA ) )
			return;

		ItemStack testA = isB == null ? null : isB.copy();
		ItemStack testB = isA == null ? null : isA.copy();

		// can put some back?
		if ( testA != null && testA.stackSize > a.getSlotStackLimit() )
		{
			if ( testB != null )
				return;

			int totalA = testA.stackSize;
			testA.stackSize = a.getSlotStackLimit();
			testB = testA.copy();

			testB.stackSize = totalA - testA.stackSize;
		}

		if ( testB != null && testB.stackSize > b.getSlotStackLimit() )
		{
			if ( testA != null )
				return;

			int totalB = testB.stackSize;
			testB.stackSize = b.getSlotStackLimit();
			testA = testB.copy();

			testA.stackSize = totalB - testA.stackSize;
		}

		a.putStack( testA );
		b.putStack( testB );
	}

	public void onUpdate(String field, Object oldValue, Object newValue)
	{

	}

	public void onSlotChange(Slot s)
	{

	}

	public boolean isValidForSlot(Slot s, ItemStack i)
	{
		return true;
	}

}
