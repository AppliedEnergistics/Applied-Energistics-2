package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.storage.TileIOPort;
import appeng.util.Platform;

public class ContainerIOPort extends ContainerUpgradeable
{

	TileIOPort ioPort;

	@GuiSync(2)
	public FullnessMode fMode = FullnessMode.EMPTY;
	@GuiSync(3)
	public OperationMode opMode = OperationMode.EMPTY;

	public ContainerIOPort(InventoryPlayer ip, TileIOPort te) {
		super( ip, te );
		ioPort = te;
	}

	@Override
	protected int getHeight()
	{
		return 166;
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
	protected void setupConfig()
	{
		int offX = 19;
		int offY = 17;

		IInventory cells = upgradeable.getInventoryByName( "cells" );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 2; x++)
				addSlotToContainer( new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.STORAGE_CELLS, cells, x + y * 2, offX + x * 18, offY + y * 18, invPlayer ) );

		offX = 122;
		offY = 17;
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 2; x++)
				addSlotToContainer( new SlotOutput( cells, 6 + x + y * 2, offX + x * 18, offY + y * 18, SlotRestrictedInput.PlacableItemType.STORAGE_CELLS.IIcon ) );

		IInventory upgrades = upgradeable.getInventoryByName( "upgrades" );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1, invPlayer )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, invPlayer )).setNotDraggable() );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.opMode = (OperationMode) upgradeable.getConfigManager().getSetting( Settings.OPERATION_MODE );
			this.fMode = (FullnessMode) this.upgradeable.getConfigManager().getSetting( Settings.FULLNESS_MODE );
			this.rsMode = (RedstoneMode) this.upgradeable.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED );
		}

		standardDetectAndSendChanges();
	}

}
