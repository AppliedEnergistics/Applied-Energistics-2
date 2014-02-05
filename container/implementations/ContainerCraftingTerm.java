package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import appeng.api.storage.IStorageMonitorable;
import appeng.container.slot.SlotNormal;
import appeng.container.slot.SlotOutput;
import appeng.parts.reporting.PartCraftingTerminal;

public class ContainerCraftingTerm extends ContainerMEMonitorable
{

	PartCraftingTerminal ct;

	SlotNormal craftingSlots[] = new SlotNormal[9];
	SlotOutput outputSlot;

	public ContainerCraftingTerm(InventoryPlayer ip, IStorageMonitorable montiorable) {
		super( ip, montiorable, false );
		ct = (PartCraftingTerminal) montiorable;

		bindPlayerInventory( ip, 0, 0 );
	}

}
