package appeng.container.implementations;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotFakeTypeOnly;
import appeng.container.slot.SlotRestrictedInput;
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

	public void setLevel(long l, EntityPlayer player)
	{
		lvlEmitter.setReportingValue( l );
		EmitterValue = l;
	}

	@Override
	protected void setupConfig()
	{
		int x = 80 + 44;
		int y = 40;

		IInventory upgrades = myte.getInventoryByName( "upgrades" );
		if ( availableUpgrades() > 0 )
			addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8 + 18 * 0, invPlayer )).setNotDraggable() );
		if ( availableUpgrades() > 1 )
			addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18 * 1, invPlayer )).setNotDraggable() );
		if ( availableUpgrades() > 2 )
			addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, invPlayer )).setNotDraggable() );
		if ( availableUpgrades() > 3 )
			addSlotToContainer( (new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, invPlayer )).setNotDraggable() );

		IInventory inv = myte.getInventoryByName( "config" );
		addSlotToContainer( new SlotFakeTypeOnly( inv, 0, x, y ) );
	}

	@GuiSync(2)
	public LevelType lvType;

	@GuiSync(3)
	public long EmitterValue = -1;

	@GuiSync(4)
	public YesNo cmType;

	@Override
	public void detectAndSendChanges()
	{
		verifyPermissions( SecurityPermissions.BUILD, false );

		if ( Platform.isServer() )
		{
			this.EmitterValue = lvlEmitter.getReportingValue();
			this.cmType = (YesNo) this.myte.getConfigManager().getSetting( Settings.CRAFT_VIA_REDSTONE );
			this.lvType = (LevelType) this.myte.getConfigManager().getSetting( Settings.LEVEL_TYPE );
			this.fzMode = (FuzzyMode) this.myte.getConfigManager().getSetting( Settings.FUZZY_MODE );
			this.rsMode = (RedstoneMode) this.myte.getConfigManager().getSetting( Settings.REDSTONE_EMITTER );
		}

		standardDetectAndSendChanges();
	}

	public void onUpdate(String field, Object oldValue, Object newValue)
	{
		if ( field.equals( "EmitterValue" ) )
		{
			if ( textField != null )
				textField.setText( "" + EmitterValue );
		}
	}

}
