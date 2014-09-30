package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.recipes.handlers.Inscribe;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;

public class ContainerInscriber extends ContainerUpgradeable implements IProgressProvider
{

	TileInscriber ti;

	Slot top;
	Slot middle;
	Slot bottom;

	@GuiSync(2)
	public int maxProcessingTime = -1;

	@GuiSync(3)
	public int processingTime = -1;

	public ContainerInscriber(InventoryPlayer ip, TileInscriber te)
	{
		super( ip, te );
		ti = te;

		addSlotToContainer( top = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, ti, 0, 45, 16, invPlayer ) );
		addSlotToContainer( bottom = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_PLATE, ti, 1, 45, 62, invPlayer ) );
		addSlotToContainer( middle = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.INSCRIBER_INPUT, ti, 2, 63, 39, invPlayer ) );

		addSlotToContainer( new SlotOutput( ti, 3, 113, 40, -1 ) );

		bindPlayerInventory( ip, 0, getHeight() - /* height of player inventory */82 );

	}

	@Override
	protected int getHeight()
	{
		return 176;
	}

	@Override
	public int availableUpgrades()
	{
		return 3;
	}

	@Override
	protected boolean supportCapacity()
	{
		return false;
	}

	@Override
	/**
	 * Overridden super.setupConfig to prevent setting up the fake slots
	 */
	protected void setupConfig()
	{
		setupUpgrades();
	}

	@Override
	public boolean isValidForSlot(Slot s, ItemStack is)
	{
		ItemStack PlateA = ti.getStackInSlot( 0 );
		ItemStack PlateB = ti.getStackInSlot( 1 );

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
		standardDetectAndSendChanges();

		if ( Platform.isServer() )
		{
			this.maxProcessingTime = ti.maxProcessingTime;
			this.processingTime = ti.processingTime;
		}
	}

	@Override
	public int getCurrentProgress()
	{
		return processingTime;
	}

	@Override
	public int getMaxProgress()
	{
		return maxProcessingTime;
	}
}
