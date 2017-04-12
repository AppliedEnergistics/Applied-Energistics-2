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

package appeng.me.storage;


import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.tile.misc.TileCondenser;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


public class AEExternalHandler implements IExternalStorageHandler
{

	@Override
	public boolean canHandle( final TileEntity te, final ForgeDirection d, final StorageChannel channel, final BaseActionSource mySrc )
	{
		if( channel == StorageChannel.ITEMS && te instanceof ITileStorageMonitorable )
		{
			return ( (ITileStorageMonitorable) te ).getMonitorable( d, mySrc ) != null;
		}

		return te instanceof TileCondenser;
	}

	@Override
	public IMEInventory getInventory( final TileEntity te, final ForgeDirection d, final StorageChannel channel, final BaseActionSource src )
	{
		if( te instanceof TileCondenser )
		{
			if( channel == StorageChannel.ITEMS )
			{
				return new VoidItemInventory( (TileCondenser) te );
			}
			else
			{
				return new VoidFluidInventory( (TileCondenser) te );
			}
		}

		if( te instanceof ITileStorageMonitorable )
		{
			final ITileStorageMonitorable iface = (ITileStorageMonitorable) te;
			final IStorageMonitorable sm = iface.getMonitorable( d, src );

			if( channel == StorageChannel.ITEMS && sm != null )
			{
				final IMEInventory<IAEItemStack> ii = sm.getItemInventory();
				if( ii != null )
				{
					return ii;
				}
			}

			if( channel == StorageChannel.FLUIDS && sm != null )
			{
				final IMEInventory<IAEFluidStack> fi = sm.getFluidInventory();
				if( fi != null )
				{
					return fi;
				}
			}
		}

		return null;
	}
}
