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

package appeng.entity;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import appeng.api.implementations.items.IGrowableCrystal;
import appeng.api.implementations.tiles.ICrystalGrowthAccelerator;
import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.util.Platform;


public final class EntityGrowingCrystal extends EntityItem
{

	private int progress_1000 = 0;

	public EntityGrowingCrystal( final World w )
	{
		super( w );
	}

	public EntityGrowingCrystal( final World w, final double x, final double y, final double z, final ItemStack is )
	{
		super( w, x, y, z, is );
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if( !AEConfig.instance.isFeatureEnabled( AEFeature.InWorldPurification ) )
		{
			return;
		}

		if( this.age > 600 )
		{
			this.age = 100;
		}

		final ItemStack is = this.getEntityItem();
		final Item gc = is.getItem();

		if( gc instanceof IGrowableCrystal ) // if it changes this just stops being an issue...
		{
			final int j = MathHelper.floor_double( this.posX );
			final int i = MathHelper.floor_double( this.posY );
			final int k = MathHelper.floor_double( this.posZ );

			final Block blk = this.worldObj.getBlock( j, i, k );
			final Material mat = blk.getMaterial();
			final IGrowableCrystal cry = (IGrowableCrystal) is.getItem();

			final float multiplier = cry.getMultiplier( blk, mat );
			final int speed = (int) Math.max( 1, this.getSpeed( j, i, k ) * multiplier );

			final boolean isClient = Platform.isClient();

			if( mat.isLiquid() )
			{
				if( isClient )
				{
					this.progress_1000++;
				}
				else
				{
					this.progress_1000 += speed;
				}
			}
			else
			{
				this.progress_1000 = 0;
			}

			if( isClient )
			{
				int len = 40;

				if( speed > 2 )
				{
					len = 20;
				}

				if( speed > 90 )
				{
					len = 15;
				}

				if( speed > 150 )
				{
					len = 10;
				}

				if( speed > 240 )
				{
					len = 7;
				}

				if( speed > 360 )
				{
					len = 3;
				}

				if( speed > 500 )
				{
					len = 1;
				}

				if( this.progress_1000 >= len )
				{
					this.progress_1000 = 0;
					CommonHelper.proxy.spawnEffect( EffectType.Vibrant, this.worldObj, this.posX, this.posY + 0.2, this.posZ, null );
				}
			}
			else
			{
				if( this.progress_1000 > 1000 )
				{
					this.progress_1000 -= 1000;
					this.setEntityItemStack( cry.triggerGrowth( is ) );
				}
			}
		}
	}

	private int getSpeed( final int x, final int y, final int z )
	{
		final int per = 80;
		final float mul = 0.3f;

		int qty = 0;

		if( this.isAccelerated( x + 1, y, z ) )
		{
			qty += per + qty * mul;
		}

		if( this.isAccelerated( x, y + 1, z ) )
		{
			qty += per + qty * mul;
		}

		if( this.isAccelerated( x, y, z + 1 ) )
		{
			qty += per + qty * mul;
		}

		if( this.isAccelerated( x - 1, y, z ) )
		{
			qty += per + qty * mul;
		}

		if( this.isAccelerated( x, y - 1, z ) )
		{
			qty += per + qty * mul;
		}

		if( this.isAccelerated( x, y, z - 1 ) )
		{
			qty += per + qty * mul;
		}

		return qty;
	}

	private boolean isAccelerated( final int x, final int y, final int z )
	{
		final TileEntity te = this.worldObj.getTileEntity( x, y, z );

		return te instanceof ICrystalGrowthAccelerator && ( (ICrystalGrowthAccelerator) te ).isPowered();
	}
}
