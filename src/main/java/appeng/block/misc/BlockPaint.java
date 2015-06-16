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
		this.isFullSize = false;
		this.isOpaque = false;
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
	public void getCheckedSubBlocks( Item item, CreativeTabs tabs, List<ItemStack> itemStacks )
	{
		// do nothing
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(
			World worldIn,
			BlockPos pos,
			IBlockState state )
	{
		return null;
	}

	@Override
	public boolean canCollideCheck(
			IBlockState state,
			boolean hitIfLiquid )
	{
		return false;
	}

	@Override
	public void onNeighborBlockChange(
			World w,
			BlockPos pos,
			IBlockState state,
			Block neighborBlock )
	{
		TilePaint tp = this.getTileEntity( w, pos );

		if( tp != null )
		{
			tp.onNeighborBlockChange();
		}
	}

	@Override
	public Item getItemDropped(
			IBlockState state,
			Random rand,
			int fortune )
	{
		return null;
	}

	@Override
	public void dropBlockAsItemWithChance(
			World worldIn,
			BlockPos pos,
			IBlockState state,
			float chance,
			int fortune )
	{
		
	}
	
	@Override
	public void fillWithRain(
			World w,
			BlockPos pos )
	{
		if( Platform.isServer() )
		{
			w.setBlockToAir( pos );
		}
	}
	
	@Override
	public int getLightValue(
			IBlockAccess w,
			BlockPos pos )
	{
		TilePaint tp = this.getTileEntity( w, pos );

		if( tp != null )
		{
			return tp.getLightLevel();
		}

		return 0;
	}

	@Override
	public boolean isAir(
			IBlockAccess world,
			BlockPos pos )
	{
		return true;
	}
	
	@Override
	public boolean isReplaceable(
			World worldIn,
			BlockPos pos )
	{
		return true;
	}
	
}
