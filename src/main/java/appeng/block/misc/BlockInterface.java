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
import net.minecraft.block.properties.BooleanProperty;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.core.sync.GuiBridge;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;


public class BlockInterface extends AEBaseTileBlock
{

	private static final BooleanProperty OMNIDIRECTIONAL = BooleanProperty.create( "omnidirectional" );

	public BlockInterface()
	{
		super( Material.IRON );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { OMNIDIRECTIONAL };
	}

	@Override
	public BlockState getActualState( BlockState state, IBlockReader world, BlockPos pos )
	{
		// Determine whether the interface is omni-directional or not
		TileInterface te = this.getTileEntity( world, pos );
		boolean omniDirectional = true; // The default
		if( te != null )
		{
			omniDirectional = te.isOmniDirectional();
		}

		return super.getActualState( state, world, pos )
				.with( OMNIDIRECTIONAL, omniDirectional );
	}

	@Override
	public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand, final @Nullable ItemStack heldItem, final BlockRayTraceResult hit)
	{
		if( p.isShiftKeyDown() )
		{
			return false;
		}

		final TileInterface tg = this.getTileEntity( w, pos );
		if( tg != null )
		{
			if( Platform.isServer() )
			{
				Platform.openGUI( p, tg, AEPartLocation.fromFacing(hit), GuiBridge.GUI_INTERFACE );
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean hasCustomRotation()
	{
		return true;
	}

	@Override
	protected void customRotateBlock( final IOrientable rotatable, final Direction axis )
	{
		if( rotatable instanceof TileInterface )
		{
			( (TileInterface) rotatable ).setSide( axis );
		}
	}
}
