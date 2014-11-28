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


import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockSkyChest;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomCollision;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;

public class BlockSkyChest extends AEBaseBlock implements ICustomCollision
{

	public BlockSkyChest() {
		super( BlockSkyChest.class, Material.rock );
		setFeature( EnumSet.of( AEFeature.Core, AEFeature.SkyStoneChests ) );
		setTileEntity( TileSkyChest.class );
		isOpaque = isFullSize = false;
		lightOpacity = 0;
		hasSubtypes = true;
		setHardness( 50 );
		blockResistance = 150.0f;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		if ( is.getItemDamage() == 1 )
			return getUnlocalizedName() + ".Block";

		return getUnlocalizedName();
	}

	@Override
	public int damageDropped(int metadata) {
		return metadata;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int direction, int metadata)
	{
		if ( metadata == 1 )
			return AEApi.instance().blocks().blockSkyStone.block().getIcon( direction, 1 );
		return AEApi.instance().blocks().blockSkyStone.block().getIcon( direction, metadata );
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		ItemStack is = super.getPickBlock( target, world, x, y, z );
		is.setItemDamage( world.getBlockMetadata( x, y, z ) );
		return is;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getCheckedSubBlocks(Item item, CreativeTabs tabs, List<ItemStack> itemStacks)
	{
		super.getCheckedSubBlocks( item, tabs, itemStacks );

		itemStacks.add( new ItemStack( item, 1, 1 ) );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( Platform.isServer() )
			Platform.openGUI( player, getTileEntity( w, x, y, z ), ForgeDirection.getOrientation( side ), GuiBridge.GUI_SKYCHEST );

		return true;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockSkyChest.class;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		TileSkyChest sk = getTileEntity( w, x, y, z );
		double sc = 0.06;
		ForgeDirection o = ForgeDirection.UNKNOWN;

		if ( sk != null )
			o = sk.getUp();

		double X = o.offsetX == 0 ? 0.06 : 0.0;
		double Y = o.offsetY == 0 ? 0.06 : 0.0;
		double Z = o.offsetZ == 0 ? 0.06 : 0.0;

		return Collections.singletonList( AxisAlignedBB.getBoundingBox( Math.max( 0.0, X - o.offsetX * sc ), Math.max( 0.0, Y - o.offsetY * sc ), Math.max( 0.0, Z - o.offsetZ * sc ), Math.min( 1.0, ( 1.0 - X ) - o.offsetX * sc ), Math.min( 1.0, ( 1.0 - Y ) - o.offsetY * sc ), Math.min( 1.0, ( 1.0 - Z ) - o.offsetZ * sc ) ) );
	}

	@Override
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List<AxisAlignedBB> out, Entity e)
	{
		out.add( AxisAlignedBB.getBoundingBox( 0.05, 0.05, 0.05, 0.95, 0.95, 0.95 ) );
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegistry)
	{
	}
}
