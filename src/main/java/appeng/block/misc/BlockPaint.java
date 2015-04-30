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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockPaint;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TilePaint;
import appeng.util.Platform;


public class BlockPaint extends AEBaseBlock
{

	public BlockPaint()
	{
		super( BlockPaint.class, new MaterialLiquid( MapColor.airColor ) );
		this.setTileEntity( TilePaint.class );
		this.setLightOpacity( 0 );
		this.isFullSize = false;
		this.isOpaque = false;
		this.setFeature( EnumSet.of( AEFeature.PaintBalls ) );
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
	public AxisAlignedBB getCollisionBoundingBoxFromPool( World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_ )
	{
		return null;
	}

	@Override
	public boolean canCollideCheck( int p_149678_1_, boolean p_149678_2_ )
	{
		return false;
	}

	@Override
	public void onNeighborBlockChange( World w, int x, int y, int z, Block junk )
	{
		TilePaint tp = this.getTileEntity( w, x, y, z );

		if( tp != null )
		{
			tp.onNeighborBlockChange();
		}
	}

	@Override
	public Item getItemDropped( int p_149650_1_, Random p_149650_2_, int p_149650_3_ )
	{
		return null;
	}

	@Override
	public void dropBlockAsItemWithChance( World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_, int p_149690_5_, float p_149690_6_, int p_149690_7_ )
	{

	}

	@Override
	public void fillWithRain( World w, int x, int y, int z )
	{
		if( Platform.isServer() )
		{
			w.setBlock( x, y, z, Platform.AIR, 0, 3 );
		}
	}

	@Override
	public int getLightValue( IBlockAccess w, int x, int y, int z )
	{
		TilePaint tp = this.getTileEntity( w, x, y, z );

		if( tp != null )
		{
			return tp.getLightLevel();
		}

		return 0;
	}

	@Override
	public boolean isReplaceable( IBlockAccess world, int x, int y, int z )
	{
		return true;
	}

	@Override
	public boolean isAir( IBlockAccess world, int x, int y, int z )
	{
		return true;
	}
}
