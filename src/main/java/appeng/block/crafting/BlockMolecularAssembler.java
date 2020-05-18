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

package appeng.block.crafting;


import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.util.Platform;


public class BlockMolecularAssembler extends AEBaseTileBlock
{

	public static final PropertyBool POWERED = PropertyBool.create( "powered" );

	public BlockMolecularAssembler()
	{
		super( Material.IRON );

		this.setOpaque( false );
		this.lightOpacity = 1;
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { POWERED };
	}

	@Override
	public BlockState getActualState( BlockState state, IBlockReader worldIn, BlockPos pos )
	{
		boolean powered = false;
		TileMolecularAssembler te = this.getTileEntity( worldIn, pos );
		if( te != null )
		{
			powered = te.isPowered();
		}

		return super.getActualState( state, worldIn, pos ).withProperty( POWERED, powered );
	}

	/**
	 * NOTE: This is only used to determine how to render an item being held in hand.
	 * For determining block rendering, the method below is used (canRenderInLayer).
	 */
	@OnlyIn( Dist.CLIENT )
	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public boolean canRenderInLayer( BlockState state, BlockRenderLayer layer )
	{
		return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isFullCube( BlockState state )
	{
		return false;
	}

	@Override
	public boolean onBlockActivated( final World w, final BlockPos pos, final BlockState state, final PlayerEntity p, final Hand hand, final Direction side, final float hitX, final float hitY, final float hitZ )
	{
		final TileMolecularAssembler tg = this.getTileEntity( w, pos );
		if( tg != null && !p.isShiftKeyDown() )
		{
			Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_MAC );
			return true;
		}

		return super.onBlockActivated( w, pos, state, p, hand, side, hitX, hitY, hitZ );
	}
}
