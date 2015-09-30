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

import com.google.common.base.Optional;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockSkyChest;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomCollision;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;


public class BlockSkyChest extends AEBaseTileBlock implements ICustomCollision
{
	
	public static enum SkyChestType
	{
		STONE, BLOCK
	};

	public final SkyChestType type;
	
	public BlockSkyChest( final SkyChestType type )
	{
		super( Material.rock, Optional.of( type.name() ) );
		this.setTileEntity( TileSkyChest.class );
		this.isOpaque = this.isFullSize = false;
		this.lightOpacity = 0;
		this.hasSubtypes = true;
		this.setHardness( 50 );
		this.blockResistance = 150.0f;
		this.type = type;
		this.setFeature( EnumSet.of( AEFeature.Core, AEFeature.SkyStoneChests ) );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockSkyChest.class;
	}

	@Override
	public boolean onActivated(
			final World w,
			final BlockPos pos,
			final EntityPlayer player,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( player, this.getTileEntity( w, pos ), AEPartLocation.fromFacing( side ), GuiBridge.GUI_SKYCHEST );
		}

		return true;
	}
	
	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(
			final World w,
			final BlockPos pos,
			final Entity thePlayer,
			final boolean b )
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
		return Collections.singletonList( AxisAlignedBB.fromBounds( Math.max( 0.0, offsetX - o.getFrontOffsetX() * sc ), Math.max( 0.0, offsetY - o.getFrontOffsetY() * sc ), Math.max( 0.0, offsetZ - o.getFrontOffsetZ() * sc ), Math.min( 1.0, ( 1.0 - offsetX ) - o.getFrontOffsetX() * sc ), Math.min( 1.0, ( 1.0 - offsetY ) - o.getFrontOffsetY() * sc ), Math.min( 1.0, ( 1.0 - offsetZ ) - o.getFrontOffsetZ() * sc ) ) );
	}
	
	@Override
	public void addCollidingBlockToList(
			final World w,
			final BlockPos pos,
			final AxisAlignedBB bb,
			final List<AxisAlignedBB> out,
			final Entity e )
	{
		out.add( AxisAlignedBB.fromBounds( 0.05, 0.05, 0.05, 0.95, 0.95, 0.95 ) );
	}
}
