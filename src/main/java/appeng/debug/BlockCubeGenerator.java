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

package appeng.debug;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;


public class BlockCubeGenerator extends AEBaseTileBlock
{

	public BlockCubeGenerator()
	{
		super( Material.iron );
		this.setTileEntity( TileCubeGenerator.class );
		this.setFeature( EnumSet.of( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ) );
	}

	@Override
	public boolean onActivated(
			final World w,
			final BlockPos pos,
			final EntityPlayer player,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final TileCubeGenerator tcg = this.getTileEntity( w, pos );
		if( tcg != null )
		{
			tcg.click( player );
		}

		return true;
	}

}
