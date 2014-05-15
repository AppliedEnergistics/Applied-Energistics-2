package appeng.container.implementations;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import appeng.api.AEApi;
import appeng.container.AEBaseContainer;
import appeng.container.slot.QuartzKnifeOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.items.contents.QuartzKnifeObj;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;

public class ContainerQuartzKnife extends AEBaseContainer implements IAEAppEngInventory, IInventory
{

	QuartzKnifeObj toolInv;

	AppEngInternalInventory inSlot = new AppEngInternalInventory( this, 1 );
	SlotRestrictedInput metals;
	QuartzKnifeOutput output;
	String myName = "";

	public void setName(String value)
	{
		myName = value;
	}

	public ContainerQuartzKnife(InventoryPlayer ip, QuartzKnifeObj te) {
		super( ip, null, null );
		toolInv = te;

		addSlotToContainer( metals = new SlotRestrictedInput( PlaceableItemType.METAL_INGOTS, inSlot, 0, 94, 44 ) );
		addSlotToContainer( output = new QuartzKnifeOutput( this, 0, 134, 44, -1 ) );

		lockPlayerInventorySlot( ip.currentItem );

		bindPlayerInventory( ip, 0, 184 - /* height of playerinventory */82 );
	}

	@Override
	public void detectAndSendChanges()
	{
		ItemStack currentItem = getPlayerInv().getCurrentItem();

		if ( currentItem != toolInv.getItemStack() )
		{
			if ( currentItem != null )
			{
				if ( Platform.isSameItem( toolInv.getItemStack(), currentItem ) )
					getPlayerInv().setInventorySlotContents( getPlayerInv().currentItem, toolInv.getItemStack() );
				else
					isContainerValid = false;
			}
			else
				isContainerValid = false;
		}

		super.detectAndSendChanges();
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		if ( inSlot.getStackInSlot( 0 ) != null )
			par1EntityPlayer.dropPlayerItemWithRandomChoice( inSlot.getStackInSlot( 0 ), false );
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{

	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		ItemStack input = inSlot.getStackInSlot( 0 );
		if ( input == null )
			return null;

		if ( SlotRestrictedInput.isMetalIngot( input ) )
		{
			if ( myName.length() > 0 )
			{
				ItemStack name = AEApi.instance().materials().materialNamePress.stack( 1 );
				NBTTagCompound c = Platform.openNbtData( name );
				c.setString( "InscribeName", myName );
				return name;
			}
		}

		return null;
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		ItemStack is = getStackInSlot( 0 );
		if ( is != null )
		{
			if ( makePlate() )
				return is;
		}
		return null;
	}

	private boolean makePlate()
	{
		if ( inSlot.decrStackSize( 0, 1 ) != null )
		{
			ItemStack item = toolInv.getItemStack();
			item.damageItem( 1, getPlayerInv().player );

			if ( item.stackSize == 0 )
			{
				getPlayerInv().mainInventory[getPlayerInv().currentItem] = null;
				MinecraftForge.EVENT_BUS.post( new PlayerDestroyItemEvent( getPlayerInv().player, item ) );
			}

			return true;
		}
		return false;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2)
	{
		if ( var2 == null && Platform.isServer() )
			makePlate();
	}

	@Override
	public String getInventoryName()
	{
		return "Quartz Knife Output";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public void markDirty()
	{

	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
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
	public boolean isItemValidForSlot(int var1, ItemStack var2)
	{
		return false;
	}

}
