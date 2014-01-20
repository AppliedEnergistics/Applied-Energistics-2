package appeng.integration.abstraction;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface IIC2
{

	void addToEnergyNet(TileEntity appEngTile);

	void removeFromEnergyNet(TileEntity appEngTile);

	ItemStack getItem(String string);

}
