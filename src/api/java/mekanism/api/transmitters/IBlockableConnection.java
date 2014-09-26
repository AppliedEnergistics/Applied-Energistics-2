package mekanism.api.transmitters;

import net.minecraftforge.common.util.ForgeDirection;

public interface IBlockableConnection
{
	public boolean canConnectMutual(ForgeDirection side);
	
	public boolean canConnect(ForgeDirection side);
}
