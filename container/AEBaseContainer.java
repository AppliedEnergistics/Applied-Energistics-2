package appeng.container;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotDisabled;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotInaccessable;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.helpers.InventoryAction;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

public abstract class AEBaseContainer extends Container
{

	final InventoryPlayer invPlayer;
	final TileEntity tileEntity;
	final IPart part;

	protected IMEInventoryHandler<IAEItemStack> cellInv;
	protected IEnergySource powerSrc;

	public Object getTarget()
	{
		if ( tileEntity != null )
			return tileEntity;
		if ( part != null )
			return part;
		return null;
	}

	public AEBaseContainer(InventoryPlayer ip, TileEntity myTile, IPart myPart) {
		invPlayer = ip;
		tileEntity = myTile;
		part = myPart;
	}

	public boolean canDragIntoSlot(Slot s)
	{
		return ((AppEngSlot) s).isDraggable;
	}

	public TileEntity getTileEntity()
	{
		return tileEntity;
	}

	@Override
	protected Slot addSlotToContainer(Slot newSlot)
	{
		if ( newSlot instanceof AppEngSlot )
			return super.addSlotToContainer( newSlot );
		else
			throw new RuntimeException( "Invalid Slot for AE Container." );
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		if ( tileEntity instanceof IInventory )
			return ((IInventory) tileEntity).isUseableByPlayer( entityplayer );
		return true;
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

		if ( clickSlot instanceof SlotDisabled || clickSlot instanceof SlotInaccessable )
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
							if ( Platform.isSameItem( dest, tis ) )
								return null;
							else if ( dest == null )
							{
								cs.putStack( tis != null ? tis.copy() : null );
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

					if ( d.isItemValid( tis ) && tis != null )
					{
						if ( d.getHasStack() )
						{
							ItemStack t = d.getStack();

							if ( tis != null && Platform.isSameItem( tis, t ) ) // t.isItemEqual(tis))
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

					if ( d.isItemValid( tis ) && tis != null )
					{
						if ( d.getHasStack() )
						{
							ItemStack t = d.getStack();

							if ( tis != null && Platform.isSameItem( t, tis ) )
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
									// worldEntity.onInventoryChanged();
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
								// worldEntity.onInventoryChanged();
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

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer, int offset_x, int offset_y)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer( new SlotPlayerInv( inventoryPlayer, j + i * 9 + 9, 8 + j * 18 + offset_x, offset_y + i * 18 ) );
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer( new SlotPlayerHotBar( inventoryPlayer, i, 8 + i * 18 + offset_x, 58 + offset_y ) );
		}
	}

	public ItemStack shiftStoreItem(ItemStack input)
	{
		if ( powerSrc == null || cellInv == null )
			return input;
		IAEItemStack ais = Platform.poweredInsert( powerSrc, cellInv, AEApi.instance().storage().createItemStack( input ) );
		if ( ais == null )
			return null;
		return ais.getItemStack();
	}

	public void doAction(EntityPlayerMP player, InventoryAction action, int slot, IAEItemStack slotItem)
	{
		if ( slot >= 0 && slot < inventorySlots.size() )
		{
			Slot s = getSlot( slot );

			if ( s instanceof SlotFake )
			{
				ItemStack hand = player.inventory.getItemStack();

				switch (action)
				{
				case PICKUP_OR_SETDOWN:

					if ( hand == null )
						s.putStack( null );
					else
						s.putStack( hand.copy() );

					break;
				case SPLIT_OR_PLACESINGLE:

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

			return;
		}

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

				InventoryAdaptor adp = InventoryAdaptor.getAdaptor( player.inventory, ForgeDirection.UNKNOWN );
				myItem.stackSize = (int) ais.getStackSize();
				myItem = adp.simulateAdd( myItem );

				if ( myItem != null )
					ais.setStackSize( ais.getStackSize() - myItem.stackSize );

				ais = Platform.poweredExtraction( powerSrc, cellInv, ais );
				if ( ais != null )
					adp.addItems( ais.getItemStack() );
			}
			break;
		case PICKUP_OR_SETDOWN:
			if ( powerSrc == null || cellInv == null )
				return;

			if ( player.inventory.getItemStack() == null )
			{
				if ( slotItem != null )
				{
					IAEItemStack ais = slotItem.copy();
					ais.setStackSize( ais.getItemStack().getMaxStackSize() );
					ais = Platform.poweredExtraction( powerSrc, cellInv, ais );
					if ( ais != null )
						player.inventory.setItemStack( ais.getItemStack() );
					else
						player.inventory.setItemStack( null );
					player.updateHeldItem();
				}
			}
			else
			{
				IAEItemStack ais = AEApi.instance().storage().createItemStack( player.inventory.getItemStack() );
				ais = Platform.poweredInsert( powerSrc, cellInv, ais );
				if ( ais != null )
					player.inventory.setItemStack( ais.getItemStack() );
				else
					player.inventory.setItemStack( null );
				player.updateHeldItem();
			}

			break;
		case SPLIT_OR_PLACESINGLE:
			if ( powerSrc == null || cellInv == null )
				return;

			if ( player.inventory.getItemStack() == null )
			{
				if ( slotItem != null )
				{
					IAEItemStack ais = slotItem.copy();
					long stackSize = Math.min( ais.getItemStack().getMaxStackSize(), ais.getStackSize() );
					ais.setStackSize( (stackSize + 1) >> 1 );
					ais = Platform.poweredExtraction( powerSrc, cellInv, ais );
					if ( ais != null )
						player.inventory.setItemStack( ais.getItemStack() );
					else
						player.inventory.setItemStack( null );
					player.updateHeldItem();
				}
			}
			else
			{
				IAEItemStack ais = AEApi.instance().storage().createItemStack( player.inventory.getItemStack() );
				ais.setStackSize( 1 );
				ais = Platform.poweredInsert( powerSrc, cellInv, ais );
				if ( ais == null )
				{
					ItemStack is = player.inventory.getItemStack();
					is.stackSize--;
					if ( is.stackSize <= 0 )
						player.inventory.setItemStack( null );
					player.updateHeldItem();
				}
			}

			break;
		case CREATIVE_DUPLICATE:
			if ( player.capabilities.isCreativeMode && slotItem != null )
			{
				ItemStack is = slotItem.getItemStack();
				is.stackSize = is.getMaxStackSize();
				player.inventory.setItemStack( is );
				player.updateHeldItem();
			}
			break;
		case MOVE_REGION:
			break;
		default:
			break;
		}
	}

}
