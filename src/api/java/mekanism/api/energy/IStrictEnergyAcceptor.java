package mekanism.api.energy;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Implement this if your TileEntity can accept energy at a floating-point double value from Universal Cables.
 * @author AidanBrady
 *
 */
public interface IStrictEnergyAcceptor extends IStrictEnergyStorage
{
	/**
	 * Transfer a certain amount of energy to this acceptor.
	 * @param amount - amount to transfer
	 * @return energy used
	 */
	public double transferEnergyToAcceptor(ForgeDirection side, double amount);

	/**
	 * Whether or not this tile entity accepts energy from a certain side.
	 * @param side - side to check
	 * @return if tile entity accepts energy
	 */
	public boolean canReceiveEnergy(ForgeDirection side);
}
