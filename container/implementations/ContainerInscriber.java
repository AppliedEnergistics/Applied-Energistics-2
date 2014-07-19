package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;

public class ContainerInscriber extends AEBaseContainer
{

	TileInscriber myte;

	Slot top;
	Slot middle;
	Slot bottom;

	@GuiSync(0)
	public int maxProessingTime = -1;

	@GuiSync(1)
	public int processingTime = -1;

	public ContainerInscriber(InventoryPlayer ip, TileInscriber te) {
		super( ip, te, null );
		myte = te;

		addSlotToContainer( top = new SlotRestrictedInput( PlaceableItemType.INSCRIBER_PLATE, myte, 0, 45, 16, invPlayer ) );
		addSlotToContainer( bottom = new SlotRestrictedInput( PlaceableItemType.INSCRIBER_PLATE, myte, 1, 45, 62, invPlayer ) );
		addSlotToContainer( middle = new SlotRestrictedInput( PlaceableItemType.INSCRIBER_INPUT, myte, 2, 63, 39, invPlayer ) );

		addSlotToContainer( new SlotOutput( myte, 3, 113, 40, -1 ) );

		bindPlayerInventory( ip, 0, 176 - /* height of playerinventory */82 );
	}

	public boolean isValidForSlot(Slot s, ItemStack is)
	{
		ItemStack PlateA = myte.getStackInSlot( 0 );
		ItemStack PlateB = myte.getStackInSlot( 1 );

		if ( s == middle )
		{
			for (ItemStack i : Inscribe.plates)
			{
				if ( Platform.isSameItemPrecise( i, is ) )
					return false;
			}

			boolean matches = false;
			boolean found = false;

			for (InscriberRecipe i : Inscribe.recipes)
			{
				boolean matchA = (PlateA == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateA, i.plateA )) && // and...
						(PlateB == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateB, i.plateB ));

				boolean matchB = (PlateB == null && i.plateA == null) || (Platform.isSameItemPrecise( PlateB, i.plateA )) && // and...
						(PlateA == null && i.plateB == null) | (Platform.isSameItemPrecise( PlateA, i.plateB ));

				if ( matchA || matchB )
				{
					matches = true;
					for (ItemStack option : i.imprintable)
					{
						if ( Platform.isSameItemPrecise( is, option ) )
							found = true;
					}

				}
			}

			if ( matches && found == false )
				return false;
		}

		if ( (s == top && PlateB != null) || (s == bottom && PlateA != null) )
		{
			boolean isValid = false;
			ItemStack otherSlot = null;
			if ( s == top )
				otherSlot = bottom.getStack();
			else
				otherSlot = top.getStack();

			// name presses
			if ( AEApi.instance().materials().materialNamePress.sameAsStack( otherSlot ) )
				return AEApi.instance().materials().materialNamePress.sameAsStack( is );

			// everything else
			for (InscriberRecipe i : Inscribe.recipes)
			{
				if ( Platform.isSameItemPrecise( i.plateA, otherSlot ) )
				{
					isValid = Platform.isSameItemPrecise( is, i.plateB );
				}
				else if ( Platform.isSameItemPrecise( i.plateB, otherSlot ) )
				{
					isValid = Platform.isSameItemPrecise( is, i.plateA );
				}

				if ( isValid )
					break;
			}

			if ( !isValid )
				return false;
		}

		return true;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		if ( Platform.isServer() )
		{
			this.maxProessingTime = myte.maxProessingTime;
			this.processingTime = myte.processingTime;
		}
	}
}
