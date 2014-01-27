package appeng.items.contents;

import net.minecraft.item.ItemStack;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.Platform;

public class CellUpgrades extends UpgradeInventory
{

	final ItemStack is;

	public CellUpgrades(ItemStack is, int upgrades) {
		super( is.getItem(), null, upgrades );
		this.is = is;
		readFromNBT( Platform.openNbtData( is ), "upgrades" );
	}

	@Override
	public void onInventoryChanged()
	{
		writeToNBT( Platform.openNbtData( is ), "upgrades" );
	}

}