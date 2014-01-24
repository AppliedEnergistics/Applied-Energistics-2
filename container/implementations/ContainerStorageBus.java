package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.parts.misc.PartStorageBus;
import appeng.util.Platform;

public class ContainerStorageBus extends ContainerUpgradeable
{

	PartStorageBus storageBus;
	public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

	public ContainerStorageBus(InventoryPlayer ip, PartStorageBus te) {
		super( ip, te );
		storageBus = te;
	}

	@Override
	protected int getHeight()
	{
		return 245;
	}

	@Override
	public int availableUpgrades()
	{
		return 5;
	}

	@Override
	protected boolean supportCapacity()
	{
		return true;
	}

	@Override
	protected void setupConfig()
	{
		IInventory upgrades = myte.getInventoryByName( "upgrades" );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0 )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1 )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2 )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3 )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4 )).setNotDraggable() );

	}

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.rwMode != this.myte.getConfigManager().getSetting( Settings.ACCESS ) )
				{
					icrafting.sendProgressBarUpdate( this, 3, (int) this.myte.getConfigManager().getSetting( Settings.ACCESS ).ordinal() );
				}

				if ( this.fzMode != this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE ) )
				{
					icrafting.sendProgressBarUpdate( this, 4, (int) this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE ).ordinal() );
				}
			}

			this.fzMode = (FuzzyMode) this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE );
			this.rwMode = (AccessRestriction) this.myte.getConfigManager().getSetting( Settings.ACCESS );
		}

		standardDetectAndSendChanges();
	}

	@Override
	public void updateProgressBar(int idx, int value)
	{
		super.updateProgressBar( idx, value );

		if ( idx == 3 )
			this.rwMode = AccessRestriction.values()[value];

		if ( idx == 4 )
			this.fzMode = FuzzyMode.values()[value];

	}

}
