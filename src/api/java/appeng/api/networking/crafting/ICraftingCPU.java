package appeng.api.networking.crafting;

import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.data.IAEItemStack;

public interface ICraftingCPU extends IBaseMonitor<IAEItemStack>
{

	/**
	 * @return true if the CPU currently has a job.
	 */
	boolean isBusy();

	/**
	 * @return the action source for the CPU.
	 */
	BaseActionSource getActionSource();

	/**
	 * @return the available storage in bytes
	 */
	long getAvailableStorage();

	/**
	 * @return the number of co-processors in the CPU.
	 */
	int getCoProcessors();

	/**
	 * @return an empty string or the name of the cpu.
	 */
	String getName();

}
