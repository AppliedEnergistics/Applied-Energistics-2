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

package appeng.integration.modules.BCHelpers;


import appeng.api.util.ICommonTile;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;


public class AEGenericSchematicTile extends SchematicTile
{

	@Override
	public void storeRequirements( final IBuilderContext context, final int x, final int y, final int z )
	{
		final TileEntity tile = context.world().getTileEntity( x, y, z );
		final ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		if( tile instanceof AEBaseTile )
		{
			final ICommonTile tcb = (ICommonTile) tile;
			tcb.getDrops( tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, list );
		}

		this.storedRequirements = list.toArray( new ItemStack[list.size()] );
	}

	@Override
	public void rotateLeft( final IBuilderContext context )
	{
		if( this.tileNBT.hasKey( "orientation_forward" ) && this.tileNBT.hasKey( "orientation_up" ) )
		{
			final String forward = this.tileNBT.getString( "orientation_forward" );
			final String up = this.tileNBT.getString( "orientation_up" );

			if( forward != null && up != null )
			{
				try
				{
					ForgeDirection fdForward = ForgeDirection.valueOf( forward );
					ForgeDirection fdUp = ForgeDirection.valueOf( up );

					fdForward = Platform.rotateAround( fdForward, ForgeDirection.DOWN );
					fdUp = Platform.rotateAround( fdUp, ForgeDirection.DOWN );

					this.tileNBT.setString( "orientation_forward", fdForward.name() );
					this.tileNBT.setString( "orientation_up", fdUp.name() );
				}
				catch( final Throwable ignored )
				{

				}
			}
		}
	}
}
