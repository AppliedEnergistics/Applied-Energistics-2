package appeng.container.implementations;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.core.sync.packets.PacketPatternSlot;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.item.AEItemStack;

public class ContainerPatternTerm extends ContainerMEMonitorable implements IAEAppEngInventory, IOptionalSlotHost
{

	AppEngInternalInventory cOut = new AppEngInternalInventory( null, 1 );
	IInventory crafting;

	SlotFake craftingSlots[] = new SlotFake[9];
	OptionalSlotFake outputSlots[] = new OptionalSlotFake[3];

	SlotPatternTerm craftSlot;

	SlotRestrictedInput patternSlotIN;
	SlotRestrictedInput patternSlotOUT;

	public PartPatternTerminal ct;

	public ContainerPatternTerm(InventoryPlayer ip, ITerminalHost montiorable) {
		super( ip, montiorable, false );
		ct = (PartPatternTerminal) montiorable;

		IInventory patternInv = ct.getInventoryByName( "pattern" );
		IInventory output = ct.getInventoryByName( "output" );
		crafting = ct.getInventoryByName( "crafting" );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				addSlotToContainer( craftingSlots[x + y * 3] = new SlotFake( crafting, x + y * 3, 18 + x * 18, -76 + y * 18 ) );

		addSlotToContainer( craftSlot = new SlotPatternTerm( ip.player, mySrc, powerSrc, montiorable, crafting, patternInv, cOut, 110, -76 + 18, this, 2 ) );
		craftSlot.IIcon = -1;

		for (int y = 0; y < 3; y++)
		{
			addSlotToContainer( outputSlots[y] = new OptionalSlotFake( output, this, y, 110, -76 + y * 18, 0, 0, 1 ) );
			outputSlots[y].renderDisabled = false;
			outputSlots[y].IIcon = -1;
		}

		addSlotToContainer( patternSlotIN = new SlotRestrictedInput( PlaceableItemType.BLANK_PATTERN, patternInv, 0, 147, -72 - 9 ) );
		addSlotToContainer( patternSlotOUT = new SlotRestrictedInput( PlaceableItemType.ENCODED_PATTERN, patternInv, 1, 147, -72 + 34 ) );

		patternSlotOUT.setStackLimit( 1 );

		bindPlayerInventory( ip, 0, 0 );
		updateOrderOfOutputSlots();
	}

	private void updateOrderOfOutputSlots()
	{
		if ( !craftingMode )
		{
			craftSlot.xDisplayPosition = 0;

			for (int y = 0; y < 3; y++)
				outputSlots[y].xDisplayPosition = outputSlots[y].defX;
		}
		else
		{
			craftSlot.xDisplayPosition = craftSlot.defX;

			for (int y = 0; y < 3; y++)
				outputSlots[y].xDisplayPosition = 0;
		}
	}

	@Override
	public void putStackInSlot(int par1, ItemStack par2ItemStack)
	{
		super.putStackInSlot( par1, par2ItemStack );
		getAndUpdateOutput();
	}

	@Override
	public void putStacksInSlots(ItemStack[] par1ArrayOfItemStack)
	{
		super.putStacksInSlots( par1ArrayOfItemStack );
		getAndUpdateOutput();
	}

	public ItemStack getAndUpdateOutput()
	{
		InventoryCrafting ic = new InventoryCrafting( this, 3, 3 );
		for (int x = 0; x < ic.getSizeInventory(); x++)
			ic.setInventorySlotContents( x, crafting.getStackInSlot( x ) );

		ItemStack is = CraftingManager.getInstance().findMatchingRecipe( ic, this.getPlayerInv().player.worldObj );
		cOut.setInventorySlotContents( 0, is );
		return is;
	}

	@GuiSync(97)
	public boolean craftingMode = true;

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		if ( Platform.isServer() )
		{
			if ( craftingMode != ct.isCraftingRecipe() )
			{
				craftingMode = ct.isCraftingRecipe();
				updateOrderOfOutputSlots();
			}
		}
	}

	@Override
	public void onUpdate(String field, Object oldValue, Object newValue)
	{
		if ( field.equals( "craftingMode" ) )
		{
			getAndUpdateOutput();
			updateOrderOfOutputSlots();
		}
	}

	@Override
	public void saveChanges()
	{

	}

	public void encode()
	{
		ItemStack output = patternSlotOUT.getStack();

		ItemStack[] in = getInputs();
		ItemStack[] out = getOutputs();

		// if theres no input, this would be silly.
		if ( in == null || out == null )
			return;

		// first check the output slots, should either be null, or a pattern
		if ( output != null && !isPattern( output ) )
			return;

		// if nothing is there we should snag a new pattern.
		else if ( output == null )
		{
			output = patternSlotIN.getStack();
			if ( output == null || !isPattern( output ) )
				return; // no blanks.

			// remove one, and clear the input slot.
			output.stackSize--;
			if ( output.stackSize == 0 )
				patternSlotIN.putStack( null );

			// add a new encoded pattern.
			patternSlotOUT.putStack( output = AEApi.instance().items().itemEncodedPattern.stack( 1 ) );
		}

		// encode the slot.
		NBTTagCompound encodedValue = new NBTTagCompound();

		NBTTagList tagIn = new NBTTagList();
		NBTTagList tagOut = new NBTTagList();

		for (ItemStack i : in)
			tagIn.appendTag( createItemTag( i ) );

		for (ItemStack i : out)
			tagOut.appendTag( createItemTag( i ) );

		encodedValue.setTag( "in", tagIn );
		encodedValue.setTag( "out", tagOut );
		encodedValue.setBoolean( "crafting", craftingMode );

		output.setTagCompound( encodedValue );
	}

	private NBTBase createItemTag(ItemStack i)
	{
		NBTTagCompound c = new NBTTagCompound();

		if ( i != null )
			i.writeToNBT( c );

		return c;
	}

	private ItemStack[] getInputs()
	{
		ItemStack[] input = new ItemStack[9];
		boolean hasValue = false;

		for (int x = 0; x < craftingSlots.length; x++)
		{
			input[x] = craftingSlots[x].getStack();
			if ( input[x] != null )
				hasValue = true;
		}

		if ( hasValue )
			return input;

		return null;
	}

	private ItemStack[] getOutputs()
	{
		if ( craftingMode )
		{
			ItemStack out = getAndUpdateOutput();
			if ( out != null )
				return new ItemStack[] { out };
		}
		else
		{
			List<ItemStack> list = new ArrayList( 3 );
			boolean hasValue = false;

			for (int x = 0; x < outputSlots.length; x++)
			{
				ItemStack out = outputSlots[x].getStack();
				if ( out != null )
				{
					list.add( out );
					hasValue = true;
				}
			}

			if ( hasValue )
				return list.toArray( new ItemStack[list.size()] );
		}

		return null;
	}

	private boolean isPattern(ItemStack output)
	{
		if ( output == null )
			return false;

		return AEApi.instance().items().itemEncodedPattern.sameAsStack( output ) || AEApi.instance().materials().materialBlankPattern.sameAsStack( output );
	}

	@Override
	public boolean isSlotEnabled(int idx)
	{
		if ( idx == 1 )
			return Platform.isServer() ? ct.isCraftingRecipe() == false : craftingMode == false;
		if ( idx == 2 )
			return Platform.isServer() ? ct.isCraftingRecipe() == true : craftingMode == true;
		return false;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{

	}

	public void craftOrGetItem(PacketPatternSlot packetPatternSlot)
	{
		if ( packetPatternSlot.slotItem != null && cellInv != null )
		{
			IAEItemStack out = packetPatternSlot.slotItem.copy();

			InventoryAdaptor inv = new AdaptorPlayerHand( getPlayerInv().player );
			InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor( getPlayerInv().player, ForgeDirection.UNKNOWN );
			if ( packetPatternSlot.shift )
				inv = playerInv;

			if ( inv.simulateAdd( out.getItemStack() ) != null )
				return;

			IAEItemStack extracted = Platform.poweredExtraction( powerSrc, cellInv, out, mySrc );
			EntityPlayer p = getPlayerInv().player;

			if ( extracted != null )
			{
				inv.addItems( extracted.getItemStack() );
				if ( p instanceof EntityPlayerMP )
					updateHeld( (EntityPlayerMP) p );
				detectAndSendChanges();
				return;
			}

			InventoryCrafting ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
			InventoryCrafting real = new InventoryCrafting( new ContainerNull(), 3, 3 );
			for (int x = 0; x < 9; x++)
			{
				ic.setInventorySlotContents( x, packetPatternSlot.pattern[x] == null ? null : packetPatternSlot.pattern[x].getItemStack() );
			}

			IRecipe r = Platform.findMatchingRecipe( ic, p.worldObj );

			if ( r == null )
				return;

			IMEMonitor<IAEItemStack> storage = ct.getItemInventory();
			IItemList<IAEItemStack> all = storage.getStorageList();

			ItemStack is = r.getCraftingResult( ic );

			if ( r != null && inv != null )
			{
				for (int x = 0; x < ic.getSizeInventory(); x++)
				{
					if ( ic.getStackInSlot( x ) != null )
					{
						ItemStack pulled = Platform.extractItemsByRecipe( powerSrc, mySrc, storage, p.worldObj, r, is, ic, ic.getStackInSlot( x ), x, all );
						real.setInventorySlotContents( x, pulled );
					}
				}
			}

			IRecipe rr = Platform.findMatchingRecipe( real, p.worldObj );

			if ( rr == r && Platform.isSameItemPrecise( rr.getCraftingResult( real ), is ) )
			{
				SlotCrafting sc = new SlotCrafting( p, real, cOut, 0, 0, 0 );
				sc.onPickupFromSlot( p, is );

				for (int x = 0; x < real.getSizeInventory(); x++)
				{
					ItemStack failed = playerInv.addItems( real.getStackInSlot( x ) );
					if ( failed != null )
						p.dropPlayerItemWithRandomChoice( failed, false );
				}

				inv.addItems( is );
				if ( p instanceof EntityPlayerMP )
					updateHeld( (EntityPlayerMP) p );
				detectAndSendChanges();
			}
			else
			{
				for (int x = 0; x < real.getSizeInventory(); x++)
				{
					ItemStack failed = real.getStackInSlot( x );
					if ( failed != null )
					{
						cellInv.injectItems( AEItemStack.create( failed ), Actionable.MODULATE, new MachineSource( ct ) );
					}
				}
			}

		}
	}

	@Override
	public ItemStack slotClick(int slotNum, int p_75144_2_, int p_75144_3_, EntityPlayer p_75144_4_)
	{
		Slot s = null;
		for (Object g : inventorySlots)
		{
			if ( g instanceof Slot )
			{
				Slot gg = (Slot) g;
				if ( gg.slotNumber == slotNum )
					s = gg;
			}
		}

		ItemStack is = super.slotClick( slotNum, p_75144_2_, p_75144_3_, p_75144_4_ );
		if ( s == patternSlotOUT )
			detectAndSendChanges();
		return is;
	}

	public void clear()
	{
		for (Slot s : craftingSlots)
			s.putStack( null );

		for (Slot s : outputSlots)
			s.putStack( null );

		detectAndSendChanges();
		getAndUpdateOutput();
	}
}
