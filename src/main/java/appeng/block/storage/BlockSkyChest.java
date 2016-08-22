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
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomCollision;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;


public class BlockSkyChest extends AEBaseTileBlock implements ICustomCollision
{

	public enum SkyChestType
	{
		STONE, BLOCK
	};

	public final SkyChestType type;

	public BlockSkyChest( final SkyChestType type )
	{
		super( Material.ROCK );
		this.setTileEntity( TileSkyChest.class );
		this.setOpaque( this.setFullSize( false ) );
		this.lightOpacity = 0;
		this.setHasSubtypes( true );
		this.setHardness( 50 );
		this.blockResistance = 150.0f;
		this.type = type;
	}

	@Override
	public EnumBlockRenderType getRenderType( IBlockState state )
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}


	@Override
	public boolean onActivated( final World w, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( player, this.getTileEntity( w, pos ), AEPartLocation.fromFacing( side ), GuiBridge.GUI_SKYCHEST );
		}

		return true;
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity thePlayer, final boolean b )
	{
		final TileSkyChest sk = this.getTileEntity( w, pos );
		EnumFacing o = EnumFacing.UP;

		if( sk != null )
		{
			o = sk.getUp();
		}

		final double offsetX = o.getFrontOffsetX() == 0 ? 0.06 : 0.0;
		final double offsetY = o.getFrontOffsetY() == 0 ? 0.06 : 0.0;
		final double offsetZ = o.getFrontOffsetZ() == 0 ? 0.06 : 0.0;

		final double sc = 0.06;
		return Collections.singletonList( new AxisAlignedBB( Math.max( 0.0, offsetX - o.getFrontOffsetX() * sc ), Math.max( 0.0, offsetY - o.getFrontOffsetY() * sc ), Math.max( 0.0, offsetZ - o.getFrontOffsetZ() * sc ), Math.min( 1.0, ( 1.0 - offsetX ) - o.getFrontOffsetX() * sc ), Math.min( 1.0, ( 1.0 - offsetY ) - o.getFrontOffsetY() * sc ), Math.min( 1.0, ( 1.0 - offsetZ ) - o.getFrontOffsetZ() * sc ) ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		out.add( new AxisAlignedBB( 0.05, 0.05, 0.05, 0.95, 0.95, 0.95 ) );
	}
}
