package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.storage.ITerminalHost;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;

public class ContainerPatternTerm extends ContainerMEMonitorable implements IAEAppEngInventory
{

	AppEngInternalInventory craftSlotInv = new AppEngInternalInventory( this, 1 );

	SlotFake craftingSlots[] = new SlotFake[9];
	SlotFake outputSlots[] = new SlotFake[3];
	SlotPatternTerm craftSlot;
	SlotRestrictedInput patternSlot;

	PartPatternTerminal ct;

	public ContainerPatternTerm(InventoryPlayer ip, ITerminalHost montiorable) {
		super( ip, montiorable, false );
		ct = (PartPatternTerminal) montiorable;

		IInventory patternInv = ct.getInventoryByName( "pattern" );
		IInventory crafting = ct.getInventoryByName( "crafting" );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				addSlotToContainer( craftingSlots[x + y * 3] = new SlotFake( crafting, x + y * 3, 54 + x * 18, -76 + y * 18 ) );

		for (int y = 0; y < 3; y++)
			addSlotToContainer( outputSlots[y] = new SlotFake( crafting, 9 + y, 146, -76 + y * 18 ) );

		addSlotToContainer( patternSlot = new SlotRestrictedInput( PlaceableItemType.PATTERN, patternInv, 0, 17, -72 - 9 ) );
		addSlotToContainer( craftSlot = new SlotPatternTerm( craftSlotInv, 0, 17, -72 + 34 ) );

		patternSlot.setStackLimit( 1 );
		bindPlayerInventory( ip, 0, 0 );
	}

	@Override
	public void saveChanges()
	{

	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{

	}
}
