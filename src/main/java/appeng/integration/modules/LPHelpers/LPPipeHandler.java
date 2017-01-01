/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.integration.modules.LPHelpers;


import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.ILogisticsPipes;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


public class LPPipeHandler implements IExternalStorageHandler
{
	@Override
	public boolean canHandle( final TileEntity te, final ForgeDirection d, final StorageChannel channel, final BaseActionSource mySrc )
	{
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.LogisticsPipes ) )
		{
			final ILogisticsPipes lp = (ILogisticsPipes) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.LogisticsPipes );

			return channel == StorageChannel.ITEMS && lp.isPipe( te, d );
		}
		return false;
	}

	@Override
	public IMEInventory getInventory( final TileEntity te, final ForgeDirection d, final StorageChannel channel, final BaseActionSource src )
	{
		if( channel == StorageChannel.ITEMS )
		{
			return new LPPipeInventory( te, d );
		}
		return null;
	}
}
