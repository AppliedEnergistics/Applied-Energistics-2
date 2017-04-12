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

package appeng.core.features.registries.entries;


import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.me.storage.MEMonitorIInventory;
import appeng.util.InventoryAdaptor;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


public class ExternalIInv implements IExternalStorageHandler
{

	@Override
	public boolean canHandle( final TileEntity te, final ForgeDirection d, final StorageChannel channel, final BaseActionSource mySrc )
	{
		return channel == StorageChannel.ITEMS && te instanceof IInventory;
	}

	@Override
	public IMEInventory getInventory( final TileEntity te, final ForgeDirection d, final StorageChannel channel, final BaseActionSource src )
	{
		final InventoryAdaptor ad = InventoryAdaptor.getAdaptor( te, d );

		if( channel == StorageChannel.ITEMS && ad != null )
		{
			return new MEMonitorIInventory( ad );
		}

		return null;
	}
}
