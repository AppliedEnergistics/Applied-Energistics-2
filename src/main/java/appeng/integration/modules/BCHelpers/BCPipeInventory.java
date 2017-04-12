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

package appeng.integration.modules.BCHelpers;


import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


public class BCPipeInventory implements IMEInventory<IAEItemStack>
{

	private final TileEntity te;
	private final ForgeDirection direction;

	public BCPipeInventory( final TileEntity te, final ForgeDirection direction )
	{
		this.te = te;
		this.direction = direction;
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable mode, final BaseActionSource src )
	{
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
		{
			final IBuildCraftTransport registry = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );

			if( mode == Actionable.SIMULATE )
			{
				if( registry.canAddItemsToPipe( this.te, input.getItemStack(), this.direction ) )
				{
					return null;
				}
				return input;
			}

			if( registry.addItemsToPipe( this.te, input.getItemStack(), this.direction ) )
			{
				return null;
			}
		}

		return input;
	}

	@Override
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable mode, final BaseActionSource src )
	{
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( final IItemList<IAEItemStack> out )
	{
		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}
