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


import appeng.api.AEApi;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderBlockCharger;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileCharger;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;


public class BlockCharger extends AEBaseTileBlock implements ICustomCollision
{

	public BlockCharger()
	{
		super( Material.iron );

		this.setTileEntity( TileCharger.class );
		this.setLightOpacity( 2 );
		this.isFullSize = this.isOpaque = false;
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderBlockCharger getRenderer()
	{
		return new RenderBlockCharger();
	}

	@Override
	public boolean onActivated( final World w, final int x, final int y, final int z, final EntityPlayer player, final int side, final float hitX, final float hitY, final float hitZ )
	{
		if( player.isSneaking() )
		{
			return false;
		}

		if( Platform.isServer() )
		{
			final TileCharger tc = this.getTileEntity( w, x, y, z );
			if( tc != null )
			{
				tc.activate( player );
			}
		}

		return true;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( final World w, final int x, final int y, final int z, final Random r )
	{
		if( !AEConfig.instance.enableEffects )
		{
			return;
		}

		if( r.nextFloat() < 0.98 )
		{
			return;
		}

		final AEBaseTile tile = this.getTileEntity( w, x, y, z );
		if( tile instanceof TileCharger )
		{
			final TileCharger tc = (TileCharger) tile;

			if( AEApi.instance().definitions().materials().certusQuartzCrystalCharged().isSameAs( tc.getStackInSlot( 0 ) ) )
			{
				final double xOff = 0.0;
				final double yOff = 0.0;
				final double zOff = 0.0;

				for( int bolts = 0; bolts < 3; bolts++ )
				{
					if( CommonHelper.proxy.shouldAddParticles( r ) )
					{
						final LightningFX fx = new LightningFX( w, xOff + 0.5 + x, yOff + 0.5 + y, zOff + 0.5 + z, 0.0D, 0.0D, 0.0D );
						Minecraft.getMinecraft().effectRenderer.addEffect( fx );
					}
				}
			}
		}
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final int x, final int y, final int z, final Entity e, final boolean isVisual )
	{
		final TileCharger tile = this.getTileEntity( w, x, y, z );
		if( tile != null )
		{
			final double twoPixels = 2.0 / 16.0;
			final ForgeDirection up = tile.getUp();
			final ForgeDirection forward = tile.getForward();
			final AxisAlignedBB bb = AxisAlignedBB.getBoundingBox( twoPixels, twoPixels, twoPixels, 1.0 - twoPixels, 1.0 - twoPixels, 1.0 - twoPixels );

			if( up.offsetX != 0 )
			{
				bb.minX = 0;
				bb.maxX = 1;
			}
			if( up.offsetY != 0 )
			{
				bb.minY = 0;
				bb.maxY = 1;
			}
			if( up.offsetZ != 0 )
			{
				bb.minZ = 0;
				bb.maxZ = 1;
			}

			switch( forward )
			{
				case DOWN:
					bb.maxY = 1;
					break;
				case UP:
					bb.minY = 0;
					break;
				case NORTH:
					bb.maxZ = 1;
					break;
				case SOUTH:
					bb.minZ = 0;
					break;
				case EAST:
					bb.minX = 0;
					break;
				case WEST:
					bb.maxX = 1;
					break;
				default:
					break;
			}

			return Collections.singletonList( bb );
		}
		return Collections.singletonList( AxisAlignedBB.getBoundingBox( 0.0, 0, 0.0, 1.0, 1.0, 1.0 ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final int x, final int y, final int z, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		out.add( AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 ) );
	}
}
