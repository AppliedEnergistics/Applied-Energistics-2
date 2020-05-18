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


import net.minecraft.block.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEColor;
import appeng.client.UnlistedProperty;
import appeng.tile.crafting.TileCraftingMonitorTile;


public class BlockCraftingMonitor extends BlockCraftingUnit
{

	public static final UnlistedProperty<AEColor> COLOR = new UnlistedProperty<>( "color", AEColor.class );

	public BlockCraftingMonitor()
	{
		super( CraftingUnitType.MONITOR );
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new ExtendedBlockState( this, this.getAEStates(), new IUnlistedProperty[] {
				STATE,
				COLOR,
				FORWARD,
				UP
		} );
	}

	@Override
	public IExtendedBlockState getExtendedState( BlockState state, IBlockReader world, BlockPos pos )
	{
		AEColor color = AEColor.TRANSPARENT;
		Direction forward = Direction.NORTH;
		Direction up = Direction.UP;

		TileCraftingMonitorTile te = this.getTileEntity( world, pos );
		if( te != null )
		{
			color = te.getColor();
			forward = te.getForward();
			up = te.getUp();
		}

		return super.getExtendedState( state, world, pos )
				.withProperty( COLOR, color )
				.withProperty( FORWARD, forward )
				.withProperty( UP, up );
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void getSubBlocks( final CreativeTabs tabs, final NonNullList<ItemStack> itemStacks )
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
	}
}
