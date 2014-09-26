package mekanism.api.transmitters;

import mekanism.api.gas.IGasTransmitter;
import net.minecraft.tileentity.TileEntity;

public enum TransmissionType
{
	ENERGY,
	FLUID,
	GAS,
	ITEM;

	public static boolean checkTransmissionType(TileEntity sideTile, TransmissionType type)
	{
		return checkTransmissionType(sideTile, type, null);
	}

	public static boolean checkTransmissionType(TileEntity sideTile, TransmissionType type, TileEntity currentPipe)
	{
		return type.checkTransmissionType(sideTile, currentPipe);
	}

	public boolean checkTransmissionType(TileEntity sideTile, TileEntity currentTile)
	{
		if(sideTile instanceof ITransmitter)
		{
			if(((ITransmitter)sideTile).getTransmissionType() == this)
			{
				return true;
			}
		}

		if(this == GAS && currentTile instanceof IGasTransmitter)
		{
			if(((IGasTransmitter)currentTile).canTransferGasToTube(sideTile))
			{
				return true;
			}
		}

		return false;
	}
}