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

package appeng.block.grindstone;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.grindstone.TileGrinder;
import appeng.util.Platform;


public class BlockGrinder extends AEBaseTileBlock
{

	public BlockGrinder()
	{
		super( Material.rock );

		this.setTileEntity( TileGrinder.class );
		this.setHardness( 3.2F );
		this.setFeature( EnumSet.of( AEFeature.GrindStone ) );
	}

	@Override
	public boolean onBlockActivated(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final EntityPlayer playerIn,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		// TODO Auto-generated method stub
		return super.onBlockActivated( worldIn, pos, state, playerIn, side, hitX, hitY, hitZ );
	}
	
	@Override
	public boolean onActivated(
			final World w,
			final BlockPos pos,
			final EntityPlayer p,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final TileGrinder tg = this.getTileEntity( w, pos );
		if( tg != null && !p.isSneaking() )
		{
			Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_GRINDER );
			return true;
		}
		return false;
	}
}
