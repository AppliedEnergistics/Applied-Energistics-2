/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

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

	private Capabilities()
	{
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
