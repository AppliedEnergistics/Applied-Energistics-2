package appeng.container.implementations;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IBusCommon;
import appeng.container.AEBaseContainer;
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.OptionalSlotFakeTypeOnly;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerBus extends AEBaseContainer implements IOptionalSlotHost
{

	IBusCommon myte;
	IInventory toolbox = new AppEngInternalInventory( null, 9 );

	public ContainerBus(InventoryPlayer ip, IBusCommon te) {
		super( ip, null, te );
		myte = te;

		IInventory upgrades = myte.getInventoryByName( "upgrades" );
		if ( availableUpgrades() > 0 )
			addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0 )).setNotDraggable() );
		if ( availableUpgrades() > 1 )
			addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1 )).setNotDraggable() );
		if ( availableUpgrades() > 2 )
			addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2 )).setNotDraggable() );
		if ( availableUpgrades() > 3 )
			addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3 )).setNotDraggable() );

		if ( hasToolbox() )
		{
			for (int v = 0; v < 3; v++)
				for (int u = 0; u < 3; u++)
					addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, toolbox, u + v * 3, 186 + u * 18, 102 + v * 18 )).setPlayerSide() );
		}

		int x = 80;
		int y = 40;

		IInventory inv = myte.getInventoryByName( "config" );
		addSlotToContainer( new SlotFakeTypeOnly( inv, 0, x, y ) );

		if ( supportCapacity() )
		{
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 1, x, y, -1, 0, 1 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 2, x, y, 1, 0, 1 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 3, x, y, 0, -1, 1 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 4, x, y, 0, 1, 1 ) );

			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 5, x, y, -1, -1, 2 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 6, x, y, 1, -1, 2 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 7, x, y, -1, 1, 2 ) );
			addSlotToContainer( new OptionalSlotFakeTypeOnly( inv, this, 8, x, y, 1, 1, 2 ) );
		}

		bindPlayerInventory( ip, 0, 184 - /* height of playerinventory */82 );
	}

	protected int availableUpgrades()
	{
		return 4;
	}

	protected boolean supportCapacity()
	{
		return true;
	}

	public RedstoneMode rsMode = RedstoneMode.IGNORE;
	public FuzzyMode fzMode = FuzzyMode.IGNORE_ALL;

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.rsMode != this.myte.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED ) )
				{
					icrafting.sendProgressBarUpdate( this, 0, (int) this.myte.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED ).ordinal() );
				}

				if ( this.fzMode != this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE ) )
				{
					icrafting.sendProgressBarUpdate( this, 1, (int) this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE ).ordinal() );
				}
			}

			this.fzMode = (FuzzyMode) this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE );
			this.rsMode = (RedstoneMode) this.myte.getConfigManager().getSetting( Settings.REDSTONE_CONTROLLED );
		}

		for (Object o : inventorySlots)
		{
			if ( o instanceof OptionalSlotFake )
			{
				OptionalSlotFake fs = (OptionalSlotFake) o;
				if ( !fs.isEnabled() && fs.getDisplayStack() != null )
					((OptionalSlotFake) fs).clearStack();
			}
		}

		standardDetectAndSendChanges();
	}

	protected void standardDetectAndSendChanges()
	{
		super.detectAndSendChanges();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int idx, int value)
	{

		if ( idx == 0 )
			this.rsMode = RedstoneMode.values()[value];

		if ( idx == 1 )
			this.fzMode = FuzzyMode.values()[value];

	}

	public boolean hasToolbox()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSlotEnabled(int idx, OptionalSlotFake osf)
	{
		int upgrades = myte.getInstalledUpgrades( Upgrades.CAPACITY );

		if ( idx == 1 && upgrades > 0 )
			return true;
		if ( idx == 2 && upgrades > 1 )
			return true;

		return false;
	}

}
