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

package appeng.block.misc;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.block.AEBaseBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileCondenser;
import appeng.util.Platform;


public final class BlockCondenser extends AEBaseBlock
{

	public BlockCondenser()
	{
		super( Material.iron );

		this.setTileEntity( TileCondenser.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	public boolean onActivated( World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ )
	{
		if( player.isSneaking() )
		{
			return false;
		}

		if( Platform.isServer() )
		{
			TileCondenser tc = this.getTileEntity( w, x, y, z );
			if( tc != null && !player.isSneaking() )
			{
				Platform.openGUI( player, tc, ForgeDirection.getOrientation( side ), GuiBridge.GUI_CONDENSER );
				return true;
			}
		}

		return true;
	}
}
