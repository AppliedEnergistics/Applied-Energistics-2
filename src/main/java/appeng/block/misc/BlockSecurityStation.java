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


import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileSecurityStation;
import appeng.util.Platform;


public class BlockSecurityStation extends AEBaseTileBlock
{

	private static final PropertyBool POWERED = PropertyBool.create( "powered" );

	public BlockSecurityStation()
	{
		super( Material.IRON );

		this.setDefaultState( this.getDefaultState().withProperty( POWERED, false ) );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { POWERED };
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public BlockState getActualState( BlockState state, IBlockReader world, BlockPos pos )
	{
		boolean powered = false;
		TileSecurityStation te = this.getTileEntity( world, pos );
		if( te != null )
		{
			powered = te.isActive();
		}

		return super.getActualState( state, world, pos )
				.withProperty( POWERED, powered );
	}

	@Override
	public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand, final @Nullable ItemStack heldItem, final BlockRayTraceResult hit)
	{
		if( p.isShiftKeyDown() )
		{
			return false;
		}

		final TileSecurityStation tg = this.getTileEntity( w, pos );
		if( tg != null )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( p, tg, AEPartLocation.fromFacing(hit), GuiBridge.GUI_SECURITY );
			return true;
		}
		return false;
	}
}
