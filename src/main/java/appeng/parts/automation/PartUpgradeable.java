package appeng.parts.automation;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.util.IConfigManager;
import appeng.parts.PartBasicState;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class PartUpgradeable extends PartBasicState implements ISegmentedInventory, IAEAppEngInventory, IConfigManagerHost
{

	final IConfigManager settings = new ConfigManager( this );
	private final UpgradeInventory upgrades = new UpgradeInventory( is, this, getUpgradeSlots() );

	@Override
	public int getInstalledUpgrades(Upgrades u)
	{
		return upgrades.getInstalledUpgrades( u );
	}

	@Override
	public boolean canConnectRedstone()
	{
		return upgrades.getMaxInstalled( Upgrades.REDSTONE ) > 0;
	}

	protected int getUpgradeSlots()
	{
		return 4;
	}

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched)
	{
		for (ItemStack is : upgrades)
			if ( is != null )
				drops.add( is );
	}

	@Override
	public void writeToNBT(net.minecraft.nbt.NBTTagCompound extra)
	{
		super.writeToNBT( extra );
		settings.writeToNBT( extra );
		upgrades.writeToNBT( extra, "upgrades" );
	}

	@Override
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
		return settings;
	}

	@Override
	public IInventory getInventoryByName(String name)
	{
		if ( name.equals( "upgrades" ) )
			return upgrades;

		return null;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{

	}

	public void upgradesChanged()
	{

	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack)
	{
		if ( inv == upgrades )
		{
			upgradesChanged();
		}
	}

	public RedstoneMode getRSMode()
	{
		return null;
	}

	protected boolean isSleeping()
	{
		if ( getInstalledUpgrades( Upgrades.REDSTONE ) > 0 )
		{
			switch (getRSMode())
			{
			case IGNORE:
				return false;

			case HIGH_SIGNAL:
				if ( host.hasRedstone( side ) )
					return false;

				break;

			case LOW_SIGNAL:
				if ( !host.hasRedstone( side ) )
					return false;

				break;

			case SIGNAL_PULSE:
			default:
				break;

			}

			return true;
		}

		return false;
	}
}
