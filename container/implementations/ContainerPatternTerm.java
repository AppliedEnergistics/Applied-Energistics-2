package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.storage.ITerminalHost;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class ContainerPatternTerm extends ContainerMEMonitorable implements IAEAppEngInventory, IOptionalSlotHost
{

	AppEngInternalInventory craftSlotInv = new AppEngInternalInventory( this, 1 );

	SlotFake craftingSlots[] = new SlotFake[9];
	OptionalSlotFake outputSlots[] = new OptionalSlotFake[3];

	SlotPatternTerm craftSlot;

	SlotRestrictedInput patternSlotIN;
	SlotRestrictedInput patternSlotOUT;

	public PartPatternTerminal ct;
	public boolean craftingMode = true;

	public ContainerPatternTerm(InventoryPlayer ip, ITerminalHost montiorable) {
		super( ip, montiorable, false );
		ct = (PartPatternTerminal) montiorable;

		IInventory patternInv = ct.getInventoryByName( "pattern" );
		IInventory output = ct.getInventoryByName( "output" );
		IInventory crafting = ct.getInventoryByName( "crafting" );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				addSlotToContainer( craftingSlots[x + y * 3] = new SlotFake( crafting, x + y * 3, 18 + x * 18, -76 + y * 18 ) );

		addSlotToContainer( craftSlot = new SlotPatternTerm( craftSlotInv, this, 0, 110, -76 + 18, 2 ) );
		craftSlot.renderDisabled = false;
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
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		if ( craftingMode != ct.craftingMode )
		{
			craftingMode = ct.craftingMode;
			updateOrderOfOutputSlots();

			for (Object c : this.crafters)
			{
				if ( c instanceof ICrafting )
					((ICrafting) c).sendProgressBarUpdate( this, 97, craftingMode ? 1 : 0 );
			}
		}
	}

	@Override
	public void updateProgressBar(int idx, int value)
	{
		super.updateProgressBar( idx, value );
		if ( idx == 97 )
		{
			craftingMode = value == 1;
			updateOrderOfOutputSlots();
		}
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{

	}

	public void encode()
	{

	}

	@Override
	public boolean isSlotEnabled(int idx)
	{
		if ( idx == 1 )
			return craftingMode == false;
		if ( idx == 2 )
			return craftingMode == true;
		return false;
	}
}
