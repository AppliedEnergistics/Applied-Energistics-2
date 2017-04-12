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


import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.blocks.RenderBlockQuartzAccelerator;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TileQuartzGrowthAccelerator;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.Random;


public class BlockQuartzGrowthAccelerator extends AEBaseTileBlock implements IOrientableBlock
{

	public BlockQuartzGrowthAccelerator()
	{
		super( Material.rock );
		this.setStepSound( Block.soundTypeMetal );
		this.setTileEntity( TileQuartzGrowthAccelerator.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderBlockQuartzAccelerator getRenderer()
	{
		return new RenderBlockQuartzAccelerator();
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( final World w, final int x, final int y, final int z, final Random r )
	{
		if( !AEConfig.instance.enableEffects )
		{
			return;
		}

		final TileQuartzGrowthAccelerator cga = this.getTileEntity( w, x, y, z );

		if( cga != null && cga.isPowered() && CommonHelper.proxy.shouldAddParticles( r ) )
		{
			final double d0 = r.nextFloat() - 0.5F;
			final double d1 = r.nextFloat() - 0.5F;

			final ForgeDirection up = cga.getUp();
			final ForgeDirection forward = cga.getForward();
			final ForgeDirection west = Platform.crossProduct( forward, up );

			double rx = 0.5 + x;
			double ry = 0.5 + y;
			double rz = 0.5 + z;

			rx += up.offsetX * d0;
			ry += up.offsetY * d0;
			rz += up.offsetZ * d0;

			double dz = 0;
			double dx = 0;
			switch( r.nextInt( 4 ) )
			{
				case 0:
					dx = 0.6;
					dz = d1;
					if( !w.getBlock( x + west.offsetX, y + west.offsetY, z + west.offsetZ ).isAir( w, x + west.offsetX, y + west.offsetY, z + west.offsetZ ) )
					{
						return;
					}
					break;
				case 1:
					dx = d1;
					dz += 0.6;
					if( !w.getBlock( x + forward.offsetX, y + forward.offsetY, z + forward.offsetZ ).isAir( w, x + forward.offsetX, y + forward.offsetY, z + forward.offsetZ ) )
					{
						return;
					}
					break;
				case 2:
					dx = d1;
					dz = -0.6;
					if( !w.getBlock( x - forward.offsetX, y - forward.offsetY, z - forward.offsetZ ).isAir( w, x - forward.offsetX, y - forward.offsetY, z - forward.offsetZ ) )
					{
						return;
					}
					break;
				case 3:
					dx = -0.6;
					dz = d1;
					if( !w.getBlock( x - west.offsetX, y - west.offsetY, z - west.offsetZ ).isAir( w, x - west.offsetX, y - west.offsetY, z - west.offsetZ ) )
					{
						return;
					}
					break;
			}

			rx += dx * west.offsetX;
			ry += dx * west.offsetY;
			rz += dx * west.offsetZ;

			rx += dz * forward.offsetX;
			ry += dz * forward.offsetY;
			rz += dz * forward.offsetZ;

			final LightningFX fx = new LightningFX( w, rx, ry, rz, 0.0D, 0.0D, 0.0D );
			Minecraft.getMinecraft().effectRenderer.addEffect( fx );
		}
	}

	@Override
	/**
	 * TODO: remove with 1.8 or later
	 *
	 * @Deprecated no longer true, only kept to prevent missing blocks.
	 */
	@Deprecated
	public boolean usesMetadata()
	{
		return true;
	}
}
