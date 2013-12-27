package appeng.integration.abstraction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface IIC2
{

	boolean canUse(ItemStack i, int powerusage);

	boolean use(ItemStack i, int powerusage, EntityPlayer p);

	int discharge(ItemStack i, int powerusage, int tier, boolean ignoreTransferLimit, boolean simulate);

	void addCompressorRecipe(ItemStack dustRecipe, ItemStack matQuartz);

	void addMaceratorRecipe(ItemStack itemStack, ItemStack matFlour);

	void addToEnergyNet(TileEntity appEngTile);

	void removeFromEnergyNet(TileEntity appEngTile);

	ItemStack getItem(String string);

}
