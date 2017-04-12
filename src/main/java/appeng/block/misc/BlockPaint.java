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


import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderBlockPaint;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TilePaint;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;


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
	@SideOnly( Side.CLIENT )
	protected RenderBlockPaint getRenderer()
	{
		return new RenderBlockPaint();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		// do nothing
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool( final World world, final int x, final int y, final int z )
	{
		return null;
	}

	@Override
	public boolean canCollideCheck( final int metadata, final boolean isHoldingRightClick )
	{
		return false;
	}

	@Override
	public void onNeighborBlockChange( final World w, final int x, final int y, final int z, final Block junk )
	{
		final TilePaint tp = this.getTileEntity( w, x, y, z );

		if( tp != null )
		{
			tp.onNeighborBlockChange();
		}
	}

	@Override
	public Item getItemDropped( final int meta, final Random random, final int fortune )
	{
		return null;
	}

	@Override
	public void dropBlockAsItemWithChance( final World world, final int x, final int y, final int z, final int meta, final float chance, final int fortune )
	{

	}

	@Override
	public void fillWithRain( final World w, final int x, final int y, final int z )
	{
		if( Platform.isServer() )
		{
			w.setBlock( x, y, z, Platform.AIR_BLOCK, 0, 3 );
		}
	}

	@Override
	public int getLightValue( final IBlockAccess w, final int x, final int y, final int z )
	{
		final TilePaint tp = this.getTileEntity( w, x, y, z );

		if( tp != null )
		{
			return tp.getLightLevel();
		}

		return 0;
	}

	@Override
	public boolean isReplaceable( final IBlockAccess world, final int x, final int y, final int z )
	{
		return true;
	}

	@Override
	public boolean isAir( final IBlockAccess world, final int x, final int y, final int z )
	{
		return true;
	}
}
