package cofh.api.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * Reference implementation of {@link IEnergyReceiver} and {@link IEnergyProvider}. Use/extend this or implement your own.
 *
 * This class is really meant to summarize how each interface is properly used.
 *
 * @author King Lemming
 */
public class TileEnergyHandler extends TileEntity implements IEnergyReceiver, IEnergyProvider {

	protected EnergyStorage storage = new EnergyStorage(32000);

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
		storage.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		storage.writeToNBT(nbt);
		return nbt;
	}

	/* IEnergyConnection */
	@Override
	public boolean canConnectEnergy(EnumFacing from) {

		return true;
	}

	/* IEnergyReceiver */
	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

		return storage.receiveEnergy(maxReceive, simulate);
	}

	/* IEnergyProvider */
	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

		return storage.extractEnergy(maxExtract, simulate);
	}

	/* IEnergyHandler */
	@Override
	public int getEnergyStored(EnumFacing from) {

		return storage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {

		return storage.getMaxEnergyStored();
	}

}
