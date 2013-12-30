package appeng.parts.automation;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.Upgrades;
import appeng.api.implementations.ISegmentedInventory;
import appeng.api.util.IConfigManager;
import appeng.parts.PartBasicState;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class PartUpgradeable extends PartBasicState implements ISegmentedInventory, IAEAppEngInventory, IConfigManagerHost
{

	IConfigManager settings = new ConfigManager( this );
	private UpgradeInventory upgrades = new UpgradeInventory( this, 4 );

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return upgrades.getInstalledUpgrades( u );
	}

	public void writeToNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		settings.writeToNBT( extra );
		upgrades.writeToNBT( extra, "upgrades" );
	}

	public void readFromNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.readFromNBT( extra );
		settings.readFromNBT( extra );
		upgrades.readFromNBT( extra, "upgrades" );
	}

	public PartUpgradeable(Class c, ItemStack is) {
		super( c, is );
		upgrades.setMaxStackSize( 1 );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return null;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "upgrades" ) )
			return upgrades;

		return null;
	}

	@Override
	public void updateSetting(Enum settingName, Enum newValue)
	{

	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{

	}

}
