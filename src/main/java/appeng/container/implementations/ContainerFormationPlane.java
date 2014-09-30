package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.parts.automation.PartFormationPlane;
import appeng.util.Platform;

public class ContainerFormationPlane extends ContainerUpgradeable
{

	final PartFormationPlane storageBus;

	public ContainerFormationPlane(InventoryPlayer ip, PartFormationPlane te) {
		super( ip, te );
		storageBus = te;
	}

	@Override
	protected int getHeight()
	{
		return 251;
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
	public boolean isSlotEnabled(int idx)
	{
		int upgrades = upgradeable.getInstalledUpgrades( Upgrades.CAPACITY );

		return upgrades > idx;
	}

	@Override
	protected void setupConfig()
	{
		int xo = 8;
		int yo = 23 + 6;

		IInventory config = upgradeable.getInventoryByName( "config" );
		for (int y = 0; y < 7; y++)
		{
			for (int x = 0; x < 9; x++)
			{
				if ( y < 2 )
					addSlotToContainer( new SlotFakeTypeOnly( config, y * 9 + x, xo + x * 18, yo + y * 18 ) );
				else
					addSlotToContainer( new OptionalSlotFakeTypeOnly( config, this, y * 9 + x, xo, yo, x, y, y - 2 ) );
			}
		}

		IInventory upgrades = upgradeable.getInventoryByName( "upgrades" );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4, invPlayer )).setNotDraggable() );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.fzMode = (FuzzyMode) this.upgradeable.getConfigManager().getSetting( Settings.FUZZY_MODE );
		}

		standardDetectAndSendChanges();
	}

}
