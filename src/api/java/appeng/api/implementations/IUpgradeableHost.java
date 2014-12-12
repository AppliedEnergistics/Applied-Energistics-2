package appeng.api.implementations;

import appeng.api.util.IConfigurableObject;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.ISegmentedInventory;

public interface IUpgradeableHost extends IConfigurableObject, ISegmentedInventory
{

	/**
	 * determine how many of an upgrade are installed.
	 */
	int getInstalledUpgrades(Upgrades u);

	/**
	 * the tile...
	 * 
	 * @return tile entity
	 */
	TileEntity getTile();

}
