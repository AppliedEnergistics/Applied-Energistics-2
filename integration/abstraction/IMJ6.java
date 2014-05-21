package appeng.integration.abstraction;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.mj.IBatteryObject;

public interface IMJ6
{

	IBatteryObject provider(TileEntity te, ForgeDirection side);

}
