package appeng.capabilities;


import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import appeng.api.storage.IStorageMonitorableAccessor;


/**
 * Utility class that holds various capabilities, both by AE2 and other Mods.
 */
public final class Capabilities
{

	private Capabilities() {
	}

	@CapabilityInject( IStorageMonitorableAccessor.class )
	public static Capability<IStorageMonitorableAccessor> STORAGE_MONITORABLE_ACCESSOR;

	/**
	 * Register AE2 provided capabilities.
	 */
	public static void register()
	{
		CapabilityManager.INSTANCE.register( IStorageMonitorableAccessor.class, createNullStorage(), NullMENetworkAccessor::new );
	}

	// Create a storage implementation that does not do anything
	private static <T> Capability.IStorage<T> createNullStorage()
	{
		return new Capability.IStorage<T>()
		{
			@Override
			public NBTBase writeNBT( Capability<T> capability, T instance, EnumFacing side )
			{
				return null;
			}

			@Override
			public void readNBT( Capability<T> capability, T instance, EnumFacing side, NBTBase nbt )
			{

			}
		};
	}
}
