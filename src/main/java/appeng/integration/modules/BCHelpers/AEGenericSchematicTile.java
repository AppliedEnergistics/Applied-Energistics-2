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

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class AEGenericSchematicTile extends SchematicTile
{

	@Override
	public void writeRequirementsToBlueprint(IBuilderContext context, int x, int y, int z)
	{
		TileEntity tile = context.world().getTileEntity( x, y, z );
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		if ( tile instanceof AEBaseTile )
		{
			AEBaseTile tcb = (AEBaseTile) tile;
			tcb.getDrops( tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, list );
		}

		storedRequirements = list.toArray( new ItemStack[list.size()] );
	}

	@Override
	public void rotateLeft(IBuilderContext context)
	{
		if ( tileNBT.hasKey( "orientation_forward" ) && tileNBT.hasKey( "orientation_up" ) )
		{
			String forward = tileNBT.getString( "orientation_forward" );
			String up = tileNBT.getString( "orientation_up" );

			if ( forward != null && up != null )
			{
				try
				{
					ForgeDirection fdForward = ForgeDirection.valueOf( forward );
					ForgeDirection fdUp = ForgeDirection.valueOf( up );

					fdForward = Platform.rotateAround( fdForward, ForgeDirection.DOWN );
					fdUp = Platform.rotateAround( fdUp, ForgeDirection.DOWN );

					tileNBT.setString( "orientation_forward", fdForward.name() );
					tileNBT.setString( "orientation_up", fdUp.name() );
				}
				catch (Throwable ignored)
				{

				}
			}
		}
	}

}
