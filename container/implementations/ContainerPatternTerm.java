package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.storage.ITerminalHost;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotPatternTerm;
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

	PartPatternTerminal ct;

	public ContainerPatternTerm(InventoryPlayer ip, ITerminalHost montiorable) {
		super( ip, montiorable, false );
		ct = (PartPatternTerminal) montiorable;

		IInventory crafting = ct.getInventoryByName( "crafting" );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 3; x++)
				addSlotToContainer( craftingSlots[x + y * 3] = new SlotFake( crafting, x + y * 3, 37 + x * 18, -72 + y * 18 ) );

		for (int y = 0; y < 3; y++)
			addSlotToContainer( outputSlots[y] = new SlotFake( crafting, y * 3, 37, -72 + y * 18 ) );

		addSlotToContainer( craftSlot = new SlotPatternTerm( craftSlotInv, 0, 37, -72 ) );

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
