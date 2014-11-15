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

final public class EntityGrowingCrystal extends EntityItem
{

	private int progress_1000 = 0;

	public EntityGrowingCrystal(World w) {
		super( w );
	}

	public EntityGrowingCrystal(World w, double x, double y, double z, ItemStack is) {
		super( w, x, y, z, is );
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if ( !AEConfig.instance.isFeatureEnabled( AEFeature.inWorldPurification ) )
			return;

		if ( age > 600 )
			age = 100;

		ItemStack is = this.getEntityItem();
		Item gc = is.getItem();

		if ( gc instanceof IGrowableCrystal ) // if it changes this just stops being an issue...
		{
			int j = MathHelper.floor_double( this.posX );
			int i = MathHelper.floor_double( this.posY );
			int k = MathHelper.floor_double( this.posZ );

			Block blk = worldObj.getBlock( j, i, k );
			Material mat = blk.getMaterial();
			IGrowableCrystal cry = (IGrowableCrystal) is.getItem();

			float multiplier = cry.getMultiplier( blk, mat );
			int speed = (int) Math.max( 1, getSpeed( j, i, k ) * multiplier );

			boolean isClient = Platform.isClient();

			if ( mat.isLiquid() )
			{
				if ( isClient )
					progress_1000++;
				else
					progress_1000 += speed;

			}
			else
				progress_1000 = 0;

			if ( isClient )
			{
				int len = 40;

				if ( speed > 2 )
					len = 20;

				if ( speed > 90 )
					len = 15;

				if ( speed > 150 )
					len = 10;

				if ( speed > 240 )
					len = 7;

				if ( speed > 360 )
					len = 3;

				if ( speed > 500 )
					len = 1;

				if ( progress_1000 >= len )
				{
					progress_1000 = 0;
					CommonHelper.proxy.spawnEffect( EffectType.Vibrant, worldObj, posX, posY + 0.2, posZ, null );
				}
			}
			else
			{
				if ( progress_1000 > 1000 )
				{
					progress_1000 -= 1000;
					setEntityItemStack( cry.triggerGrowth( is ) );
				}
			}
		}
	}

	private int getSpeed(int x, int y, int z)
	{
		final int per = 80;
		final float mul = 0.3f;

		int qty = 0;

		if ( isAccelerated( x + 1, y, z ) )
			qty += per + qty * mul;

		if ( isAccelerated( x, y + 1, z ) )
			qty += per + qty * mul;

		if ( isAccelerated( x, y, z + 1 ) )
			qty += per + qty * mul;

		if ( isAccelerated( x - 1, y, z ) )
			qty += per + qty * mul;

		if ( isAccelerated( x, y - 1, z ) )
			qty += per + qty * mul;

		if ( isAccelerated( x, y, z - 1 ) )
			qty += per + qty * mul;

		return qty;
	}

	private boolean isAccelerated(int x, int y, int z)
	{
		TileEntity te = worldObj.getTileEntity( x, y, z );

		return te instanceof ICrystalGrowthAccelerator && ( ( ICrystalGrowthAccelerator ) te ).isPowered();
	}

}
