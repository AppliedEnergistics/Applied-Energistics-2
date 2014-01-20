package appeng.container.implementations;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput.PlaceableItemType;
import appeng.parts.automation.PartLevelEmitter;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerLevelEmitter extends ContainerUpgradeable
{

	PartLevelEmitter lvlEmitter;

	@SideOnly(Side.CLIENT)
	public GuiTextField textField;

	@SideOnly(Side.CLIENT)
	public void setTextField(GuiTextField level)
	{
		textField = level;
		textField.setText( "" + EmitterValue );
	}

	public ContainerLevelEmitter(InventoryPlayer ip, PartLevelEmitter te) {
		super( ip, te );
		lvlEmitter = te;
	}

	@Override
	public int availableUpgrades()
	{

		return 1;
	}

	@Override
	protected boolean supportCapacity()
	{
		return false;
	}

	int EmitterValue = -1;

	public void setLevel(int newValue, EntityPlayer player)
	{
		lvlEmitter.setReportingValue( newValue );
		EmitterValue = newValue;
	}

	@Override
	protected void setupConfig()
	{
		int x = 80 + 34;
		int y = 40;

		IInventory upgrades = myte.getInventoryByName( "upgrades" );
		if ( availableUpgrades() > 0 )
			addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0 )).setNotDraggable() );
		if ( availableUpgrades() > 1 )
			addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1 )).setNotDraggable() );
		if ( availableUpgrades() > 2 )
			addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2 )).setNotDraggable() );
		if ( availableUpgrades() > 3 )
			addSlotToContainer( (new SlotRestrictedInput( PlaceableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3 )).setNotDraggable() );

		IInventory inv = myte.getInventoryByName( "config" );
		addSlotToContainer( new SlotFakeTypeOnly( inv, 0, x, y ) );
	}

	@Override
	public void detectAndSendChanges()
	{
		if ( Platform.isServer() )
		{
			for (int i = 0; i < this.crafters.size(); ++i)
			{
				ICrafting icrafting = (ICrafting) this.crafters.get( i );

				if ( this.rsMode != this.myte.getConfigManager().getSetting( Settings.REDSTONE_EMITTER ) )
				{
					icrafting.sendProgressBarUpdate( this, 0, (int) this.myte.getConfigManager().getSetting( Settings.REDSTONE_EMITTER ).ordinal() );
				}

				if ( this.fzMode != this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE ) )
				{
					icrafting.sendProgressBarUpdate( this, 1, (int) this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE ).ordinal() );
				}

				if ( this.EmitterValue != lvlEmitter.getReportingValue() )
				{
					icrafting.sendProgressBarUpdate( this, 2, (int) lvlEmitter.getReportingValue() );
				}
			}

			this.EmitterValue = (int) lvlEmitter.getReportingValue();
			this.fzMode = (FuzzyMode) this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE );
			this.rsMode = (RedstoneMode) this.myte.getConfigManager().getSetting( Settings.REDSTONE_EMITTER );
		}

		standardDetectAndSendChanges();
	}

	@Override
	public void updateProgressBar(int idx, int value)
	{
		super.updateProgressBar( idx, value );

		if ( idx == 2 )
		{
			EmitterValue = value;
			if ( textField != null )
				textField.setText( "" + EmitterValue );
		}
	}

}
