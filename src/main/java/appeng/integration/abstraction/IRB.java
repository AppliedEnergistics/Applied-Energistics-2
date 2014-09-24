package appeng.integration.abstraction;

import net.minecraft.tileentity.TileEntity;
import appeng.api.util.IOrientable;

public interface IRB
{

	IOrientable getOrientable(TileEntity te);

}
