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

package appeng.block.spatial;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.block.AEBaseBlock;
import appeng.helpers.ICustomCollision;


public class BlockMatrixFrame extends AEBaseBlock implements ICustomCollision
{

	public BlockMatrixFrame()
	{
		super( Material.ANVIL );
		this.setResistance( 6000000.0F );
		this.setBlockUnbreakable();
		this.setLightOpacity( 0 );
		this.setOpaque( false );
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void getSubBlocks( final CreativeTabs tabs, final NonNullList<ItemStack> itemStacks )
	{
		// do nothing
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity thePlayer, final boolean b )
	{
		return Collections.emptyList();// AxisAlignedBB.getBoundingBox( 0.25, 0, 0.25, 0.75, 0.5, 0.75 )
		// } );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		out.add( new AxisAlignedBB( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 ) );
	}

	@Override
	public boolean canPlaceBlockAt( final World worldIn, final BlockPos pos )
	{
		return false;
	}

	@Override
	public void onBlockExploded( final World world, final BlockPos pos, final Explosion explosion )
	{
		// Don't explode.
	}

	@Override
	public boolean canEntityDestroy( final BlockState state, final IBlockReader world, final BlockPos pos, final Entity entity )
	{
		return false;
	}
}
