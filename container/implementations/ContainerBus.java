package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import appeng.api.implementations.IBusCommon;
import appeng.container.AEBaseContainer;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.inventory.AppEngInternalInventory;

public class ContainerBus extends AEBaseContainer implements IOptionalSlotHost
{

	IBusCommon myte;
	IInventory toolbox = new AppEngInternalInventory( null, 9 );

	public ContainerBus(InventoryPlayer ip, IBusCommon te) {
		super( ip, null );
		myte = te;

		IInventory upgrades = myte.getInventoryByName( "upgrades" );
		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0 ) );
		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1 ) );
		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2 ) );
		addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3 ) );

		if ( hasToolbox() )
		{
			for (int v = 0; v < 3; v++)
				for (int u = 0; u < 3; u++)
					addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, toolbox, u + v * 3, 186 + u * 18, 102 + v * 18 )).setPlayerSide() );
		}

		int x = 80;
		int y = 40;

		IInventory inv = myte.getInventoryByName( "config" );
		addSlotToContainer( new SlotFake( inv, 0, x, y ) );

		addSlotToContainer( new OptionalSlotFake( inv, this, 1, x, y, -1, 0, 1 ) );
		addSlotToContainer( new OptionalSlotFake( inv, this, 2, x, y, 1, 0, 1 ) );
		addSlotToContainer( new OptionalSlotFake( inv, this, 3, x, y, 0, -1, 1 ) );
		addSlotToContainer( new OptionalSlotFake( inv, this, 4, x, y, 0, 1, 1 ) );

		addSlotToContainer( new OptionalSlotFake( inv, this, 5, x, y, -1, -1, 2 ) );
		addSlotToContainer( new OptionalSlotFake( inv, this, 6, x, y, 1, -1, 2 ) );
		addSlotToContainer( new OptionalSlotFake( inv, this, 7, x, y, -1, 1, 2 ) );
		addSlotToContainer( new OptionalSlotFake( inv, this, 8, x, y, 1, 1, 2 ) );

		/*
		 * addSlotToContainer( new OptionalSlotFake( inv, this, 9, x, y, -2, 0, 3 ) ); addSlotToContainer( new
		 * OptionalSlotFake( inv, this, 10, x, y, 2, 0, 3 ) ); addSlotToContainer( new OptionalSlotFake( inv, this, 11,
		 * x, y, 0, -2, 3 ) ); addSlotToContainer( new OptionalSlotFake( inv, this, 12, x, y, 0, 2, 3 ) );
		 * 
		 * addSlotToContainer( new OptionalSlotFake( inv, this, 13, x, y, 2, 1, 4 ) ); addSlotToContainer( new
		 * OptionalSlotFake( inv, this, 14, x, y, 2, -1, 4 ) ); addSlotToContainer( new OptionalSlotFake( inv, this, 15,
		 * x, y, -2, -1, 4 ) ); addSlotToContainer( new OptionalSlotFake( inv, this, 16, x, y, -2, 1, 4 ) );
		 */

		bindPlayerInventory( ip, 0, 184 - /* height of playerinventory */82 );
	}

	public boolean hasToolbox()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isSlotEnabled(int idx, OptionalSlotFake osf)
	{
		if ( idx == 1 )
			return true;
		if ( idx == 2 )
			return true;

		return false;
	}

}
