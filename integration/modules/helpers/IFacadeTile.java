package appeng.integration.modules.helpers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public interface IFacadeTile
{

	boolean isConnected(ForgeDirection direction);

	void dropFacadeItem(ItemStack facade);

	IFacadeProxy getFacadeProxy();

	void markForUpdate();

	float getHoleThickness(ForgeDirection direction);

}
