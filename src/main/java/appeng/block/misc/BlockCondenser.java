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

package appeng.block.misc;


import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileCondenser;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public class BlockCondenser extends AEBaseTileBlock
{

	public BlockCondenser()
	{
		super( Material.iron );

		this.setTileEntity( TileCondenser.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( player.isSneaking() )
		{
			return false;
		}

		if( Platform.isServer() )
		{
			final TileCondenser tc = this.getTileEntity( w, x, y, z );
			if( tc != null && !player.isSneaking() )
			{
				Platform.openGUI( player, tc, ForgeDirection.getOrientation( side ), GuiBridge.GUI_CONDENSER );
				return true;
			}
		}

		return true;
	}
}
