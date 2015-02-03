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


import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockCharger;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileCharger;
import appeng.util.Platform;

public class BlockCharger extends AEBaseBlock implements ICustomCollision
{

	public BlockCharger() {
		super( BlockCharger.class, Material.iron );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
		this.setTileEntity( TileCharger.class );
		this.setLightOpacity( 2 );
		this.isFullSize = this.isOpaque = false;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( player.isSneaking() )
			return false;

		if ( Platform.isServer() )
		{
			TileCharger tc = this.getTileEntity( w, x, y, z );
			if ( tc != null )
			{
				tc.activate( player );
			}
		}

		return true;
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockCharger.class;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		if ( !AEConfig.instance.enableEffects )
			return;

		if ( r.nextFloat() < 0.98 )
			return;

		AEBaseTile tile = this.getTileEntity( w, x, y, z );
		if ( tile instanceof TileCharger )
		{
			TileCharger tc = (TileCharger) tile;
			if ( AEApi.instance().definitions().materials().certusQuartzCrystalCharged().get().sameAsStack( tc.getStackInSlot( 0 ) ) )
			{

				double xOff = 0.0;
				double yOff = 0.0;
				double zOff = 0.0;

				for (int bolts = 0; bolts < 3; bolts++)
				{
					if ( CommonHelper.proxy.shouldAddParticles( r ) )
					{
						LightningFX fx = new LightningFX( w, xOff + 0.5 + x, yOff + 0.5 + y, zOff + 0.5 + z, 0.0D, 0.0D, 0.0D );
						Minecraft.getMinecraft().effectRenderer.addEffect( fx );
					}
				}

			}
		}
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(World w, int x, int y, int z, Entity e, boolean isVisual)
	{
		TileCharger tile = this.getTileEntity( w, x, y, z );
		if ( tile != null )
		{
			double twoPixels = 2.0 / 16.0;
			ForgeDirection up = tile.getUp();
			ForgeDirection forward = tile.getForward();
			AxisAlignedBB bb = AxisAlignedBB.getBoundingBox( twoPixels, twoPixels, twoPixels, 1.0 - twoPixels, 1.0 - twoPixels, 1.0 - twoPixels );

			if ( up.offsetX != 0 )
			{
				bb.minX = 0;
				bb.maxX = 1;
			}
			if ( up.offsetY != 0 )
			{
				bb.minY = 0;
				bb.maxY = 1;
			}
			if ( up.offsetZ != 0 )
			{
				bb.minZ = 0;
				bb.maxZ = 1;
			}

			switch (forward)
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
	public void addCollidingBlockToList(World w, int x, int y, int z, AxisAlignedBB bb, List<AxisAlignedBB> out, Entity e)
	{
		out.add( AxisAlignedBB.getBoundingBox( 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 ) );
	}
}
