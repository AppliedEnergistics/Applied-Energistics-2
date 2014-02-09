package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import appeng.api.config.FullnessMode;
import appeng.api.config.OperationMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.storage.TileIOPort;
import appeng.util.Platform;

public class ContainerIOPort extends ContainerUpgradeable
{

	TileIOPort ioPort;
	public OperationMode opMode = OperationMode.EMPTY;
	public FullnessMode fMode = FullnessMode.EMPTY;

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
		int offx = 19;
		int offy = 17;

		IInventory cells = myte.getInventoryByName( "cells" );

		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 2; x++)
				addSlotToContainer( new SlotRestrictedInput( PlaceableItemType.STORAGE_CELLS, cells, x + y * 2, offx + x * 18, offy + y * 18 ) );

		offx = 122;
		offy = 17;
		for (int y = 0; y < 3; y++)
			for (int x = 0; x < 2; x++)
				addSlotToContainer( new SlotOutput( cells, 6 + x + y * 2, offx + x * 18, offy + y * 18, PlaceableItemType.STORAGE_CELLS.IIcon ) );

		IInventory upgrades = myte.getInventoryByName( "upgrades" );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0 )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1 )).setNotDraggable() );
		addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2 )).setNotDraggable() );
	}

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.rsMode != this.myte.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED ) )
				{
					icrafting.sendProgressBarUpdate( this, 0, (int) this.myte.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED ).ordinal() );
				}

				if ( this.fMode != this.myte.getConfigManager().getSetting( Settings.FULLNESS_MODE ) )
				{
					icrafting.sendProgressBarUpdate( this, 2, (int) this.myte.getConfigManager().getSetting( Settings.FULLNESS_MODE ).ordinal() );
				}

				if ( this.opMode != this.myte.getConfigManager().getSetting( Settings.OPERATION_MODE ) )
				{
					icrafting.sendProgressBarUpdate( this, 3, (int) this.myte.getConfigManager().getSetting( Settings.OPERATION_MODE ).ordinal() );
				}
			}

			this.opMode = (OperationMode) myte.getConfigManager().getSetting( Settings.OPERATION_MODE );
			this.fMode = (FullnessMode) this.myte.getConfigManager().getSetting( Settings.FULLNESS_MODE );
			this.rsMode = (RedstoneMode) this.myte.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED );
		}

		standardDetectAndSendChanges();
	}

	@Override
	public void updateProgressBar(int idx, int value)
	{
		super.updateProgressBar( idx, value );

		if ( idx == 2 )
			this.fMode = FullnessMode.values()[value];

		if ( idx == 3 )
			this.opMode = OperationMode.values()[value];
	}

}
