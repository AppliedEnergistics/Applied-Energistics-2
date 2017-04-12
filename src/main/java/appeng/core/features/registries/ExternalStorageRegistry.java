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

package appeng.core.features.registries;


import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IExternalStorageRegistry;
import appeng.api.storage.StorageChannel;
import appeng.core.features.registries.entries.ExternalIInv;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;


public class ExternalStorageRegistry implements IExternalStorageRegistry
{

	private final List<IExternalStorageHandler> Handlers;
	private final ExternalIInv lastHandler = new ExternalIInv();

	public ExternalStorageRegistry()
	{
		this.Handlers = new ArrayList<IExternalStorageHandler>();
	}

	@Override
	public void addExternalStorageInterface( final IExternalStorageHandler ei )
	{
		this.Handlers.add( ei );
	}

	@Override
	public IExternalStorageHandler getHandler( final TileEntity te, final ForgeDirection d, final StorageChannel chan, final BaseActionSource mySrc )
	{
		for( final IExternalStorageHandler x : this.Handlers )
		{
			if( x.canHandle( te, d, chan, mySrc ) )
			{
				return x;
			}
		}

		if( this.lastHandler.canHandle( te, d, chan, mySrc ) )
		{
			return this.lastHandler;
		}

		return null;
	}
}
