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


import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockPaint;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TilePaint;
import appeng.util.Platform;


public class BlockPaint extends AEBaseTileBlock
{

	public BlockPaint()
	{
		super( new MaterialLiquid( MapColor.airColor ) );

		this.setTileEntity( TilePaint.class );
		this.setLightOpacity( 0 );
		this.setFullSize( false );
		this.setOpaque( false );
		this.setFeature( EnumSet.of( AEFeature.PaintBalls ) );
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockPaint.class;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		// do nothing
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state )
	{
		return null;
	}

	@Override
	public boolean canCollideCheck(
			final IBlockState state,
			final boolean hitIfLiquid )
	{
		return false;
	}

	@Override
	public void onNeighborBlockChange(
			final World w,
			final BlockPos pos,
			final IBlockState state,
			final Block neighborBlock )
	{
		final TilePaint tp = this.getTileEntity( w, pos );

		if( tp != null )
		{
			tp.onNeighborBlockChange();
		}
	}

	@Override
	public Item getItemDropped(
			final IBlockState state,
			final Random rand,
			final int fortune )
	{
		return null;
	}

	@Override
	public void dropBlockAsItemWithChance(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final float chance,
			final int fortune )
	{

	}

	@Override
	public void fillWithRain(
			final World w,
			final BlockPos pos )
	{
		if( Platform.isServer() )
		{
			w.setBlockToAir( pos );
		}
	}

	@Override
	public int getLightValue(
			final IBlockAccess w,
			final BlockPos pos )
	{
		final TilePaint tp = this.getTileEntity( w, pos );

		if( tp != null )
		{
			return tp.getLightLevel();
		}

		return 0;
	}

	@Override
	public boolean isAir(
			final IBlockAccess world,
			final BlockPos pos )
	{
		return true;
	}

	@Override
	public boolean isReplaceable(
			final World worldIn,
			final BlockPos pos )
	{
		return true;
	}

}
