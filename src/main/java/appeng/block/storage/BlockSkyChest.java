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

package appeng.block.storage;


import appeng.api.AEApi;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderBlockSkyChest;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomCollision;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;


public class BlockSkyChest extends AEBaseTileBlock implements ICustomCollision
{

	private static final double AABB_OFFSET_BOTTOM = .0625d;
	private static final double AABB_OFFSET_SIDES = 0;
	private static final double AABB_OFFSET_TOP = .125d;

	public BlockSkyChest()
	{
		super( Material.rock );
		this.setTileEntity( TileSkyChest.class );
		this.isOpaque = this.isFullSize = false;
		this.hasSubtypes = true;
		this.lightOpacity = 0;
		this.setHardness( 50 );
		this.blockResistance = 150.0f;
		this.setFeature( EnumSet.of( AEFeature.Core, AEFeature.SkyStoneChests ) );
	}

	@Override
	public int damageDropped( final int metadata )
	{
		return metadata;
	}


	@Override
	public ItemStack getPickBlock( final MovingObjectPosition target, final World world, final int x, final int y, final int z, final EntityPlayer player )
	{
		final ItemStack is = super.getPickBlock( target, world, x, y, z, player );
		is.setItemDamage( world.getBlockMetadata( x, y, z ) );

		return is;
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderBlockSkyChest getRenderer()
	{
		return new RenderBlockSkyChest();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public IIcon getIcon( final int direction, final int metadata )
	{
		for( final Block skyStoneBlock : AEApi.instance().definitions().blocks().skyStone().maybeBlock().asSet() )
		{
			return skyStoneBlock.getIcon( direction, metadata );
		}

		return Blocks.stone.getIcon( direction, metadata );
	}

	@Override
	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( player, this.getTileEntity( w, x, y, z ), ForgeDirection.getOrientation( side ), GuiBridge.GUI_SKYCHEST );
		}

		return true;
	}

	@Override
	public void registerBlockIcons( final IIconRegister iconRegistry )
	{
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		super.getCheckedSubBlocks( item, tabs, itemStacks );

		itemStacks.add( new ItemStack( item, 1, 1 ) );
	}

	@Override
	public String getUnlocalizedName( final ItemStack is )
	{
		if( is.getItemDamage() == 1 )
		{
			return this.getUnlocalizedName() + ".Block";
		}

		return this.getUnlocalizedName();
	}
	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final int x, final int y, final int z, final Entity thePlayer, final boolean isVisual )
	{
		return Collections.singletonList( computeAABB( w, x, y, z ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final int x, final int y, final int z, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		out.add( computeAABB( w, x, y, z ) );
	}

	private AxisAlignedBB computeAABB( final World w, final int x, final int y, final int z )
	{
		final TileSkyChest sk = this.getTileEntity( w, x, y, z );
		ForgeDirection o = ForgeDirection.UP;

		if( sk != null )
			o = sk.getUp();

		final double offsetX = o.offsetX == 0 ? AABB_OFFSET_BOTTOM : AABB_OFFSET_SIDES;
		final double offsetY = o.offsetY == 0 ? AABB_OFFSET_BOTTOM : AABB_OFFSET_SIDES;
		final double offsetZ = o.offsetZ == 0 ? AABB_OFFSET_BOTTOM : AABB_OFFSET_SIDES;

		// x/z needs to be multiplied by -1, thus we simply add not substract.
		final double minX = Math.max( 0.0, offsetX + o.offsetX * AABB_OFFSET_TOP );
		final double minY = Math.max( 0.0, offsetY - o.offsetY * AABB_OFFSET_TOP );
		final double minZ = Math.max( 0.0, offsetZ + o.offsetZ * AABB_OFFSET_TOP );

		final double maxX = Math.min( 1.0, ( 1.0 - offsetX ) + o.offsetX * AABB_OFFSET_TOP );
		final double maxY = Math.min( 1.0, ( 1.0 - offsetY ) - o.offsetY * AABB_OFFSET_TOP );
		final double maxZ = Math.min( 1.0, ( 1.0 - offsetZ ) + o.offsetZ * AABB_OFFSET_TOP );

		return AxisAlignedBB.getBoundingBox( minX, minY, minZ, maxX, maxY, maxZ );
	}
}
