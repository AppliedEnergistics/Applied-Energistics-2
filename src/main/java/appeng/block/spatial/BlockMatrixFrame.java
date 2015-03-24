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
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderNull;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;


public class BlockMatrixFrame extends AEBaseBlock implements ICustomCollision
{

	public BlockMatrixFrame()
	{
		super( BlockMatrixFrame.class, Material.anvil );
		this.setFeature( EnumSet.of( AEFeature.SpatialIO ) );
		this.setResistance( 6000000.0F );
		this.setBlockUnbreakable();
		this.setLightOpacity( 0 );
		this.isOpaque = false;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderNull.class;
	}

	@Override
	public void registerBlockIcons( IIconRegister iconRegistry )
	{

	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( Item item, CreativeTabs tabs, List<ItemStack> itemStacks )
	{
		// do nothing
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( World w, int x, int y, int z, Entity e, boolean isVisual )
	{
		return Arrays.asList( new AxisAlignedBB[] {} );// AxisAlignedBB.getBoundingBox( 0.25, 0, 0.25, 0.75, 0.5, 0.75 )
		// } );
	}

	@Override
	public void addCollidingBlockToList( World w, int x, int y, int z, AxisAlignedBB bb, List<AxisAlignedBB> out, Entity e )
	{
		out.add( AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 ) );
	}

	@Override
	public boolean canPlaceBlockAt( World world, int x, int y, int z )
	{
		return false;
	}

	@Override
	public boolean canEntityDestroy( IBlockAccess world, int x, int y, int z, Entity entity )
	{
		return false;
	}
}
