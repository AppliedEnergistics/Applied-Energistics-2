package cofh.api.energy;

/**
 * An energy storage is the unit of interaction with Energy inventories.<br>
 * This is not to be implemented on TileEntities. This is for internal use only.
 * <p>
 * A reference implementation can be found at {@link EnergyStorage}.
 *
 * @author King Lemming
 *
 */
public interface IEnergyStorage {

	/**
	 * Adds energy to the storage. Returns quantity of energy that was accepted.
	 *
	 * @param maxReceive
	 *            Maximum amount of energy to be inserted.
	 * @param simulate
	 *            If TRUE, the insertion will only be simulated.
	 * @return Amount of energy that was (or would have been, if simulated) accepted by the storage.
	 */
	int receiveEnergy(int maxReceive, boolean simulate);

	/**
	 * Removes energy from the storage. Returns quantity of energy that was removed.
	 *
	 * @param maxExtract
	 *            Maximum amount of energy to be extracted.
	 * @param simulate
	 *            If TRUE, the extraction will only be simulated.
	 * @return Amount of energy that was (or would have been, if simulated) extracted from the storage.
	 */
	int extractEnergy(int maxExtract, boolean simulate);

	/**
	 * Returns the amount of energy currently stored.
	 */
	int getEnergyStored();

	/**
	 * Returns the maximum amount of energy that can be stored.
	 */
	int getMaxEnergyStored();

}
