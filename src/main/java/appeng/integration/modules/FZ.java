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

package appeng.integration.modules;


import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IFZ;
import appeng.integration.modules.helpers.FactorizationBarrel;
import appeng.integration.modules.helpers.FactorizationHandler;
import appeng.util.Platform;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 100% Hacks.
 */
public class FZ implements IFZ, IIntegrationModule
{
	@Reflected
	public static FZ instance;

	private static Class<?> day_BarrelClass;
	private static Method day_getItemCount;
	private static Method day_setItemCount;
	private static Method day_getMaxSize;
	private static Field day_item;

	@Override
	public ItemStack barrelGetItem( final TileEntity te )
	{
		try
		{
			ItemStack i = null;

			if( day_BarrelClass.isInstance( te ) )
			{
				i = (ItemStack) day_item.get( te );
			}

			if( i != null )
			{
				i = Platform.cloneItemStack( i );
			}

			return i;
		}
		catch( final IllegalArgumentException ignored )
		{
		}
		catch( final IllegalAccessException ignored )
		{
		}
		return null;
	}

	@Override
	public int barrelGetMaxItemCount( final TileEntity te )
	{
		try
		{
			if( day_BarrelClass.isInstance( te ) )
			{
				return (Integer) day_getMaxSize.invoke( te );
			}
		}
		catch( final IllegalAccessException ignored )
		{
		}
		catch( final IllegalArgumentException ignored )
		{
		}
		catch( final InvocationTargetException ignored )
		{
		}
		return 0;
	}

	@Override
	public int barrelGetItemCount( final TileEntity te )
	{
		try
		{
			if( day_BarrelClass.isInstance( te ) )
			{
				return (Integer) day_getItemCount.invoke( te );
			}
		}
		catch( final IllegalAccessException ignored )
		{
		}
		catch( final IllegalArgumentException ignored )
		{
		}
		catch( final InvocationTargetException ignored )
		{
		}
		return 0;
	}

	@Override
	public void setItemType( final TileEntity te, final ItemStack input )
	{
		try
		{
			if( day_BarrelClass.isInstance( te ) )
			{
				day_item.set( te, input == null ? null : input.copy() );
			}
		}
		catch( final IllegalArgumentException ignored )
		{
		}
		catch( final IllegalAccessException ignored )
		{
		}
	}

	@Override
	public void barrelSetCount( final TileEntity te, final int max )
	{
		try
		{
			if( day_BarrelClass.isInstance( te ) )
			{
				day_setItemCount.invoke( te, max );
			}

			te.markDirty();
		}
		catch( final IllegalAccessException ignored )
		{
		}
		catch( final IllegalArgumentException ignored )
		{
		}
		catch( final InvocationTargetException ignored )
		{
		}
	}

	@Override
	public IMEInventory getFactorizationBarrel( final TileEntity te )
	{
		return new FactorizationBarrel( this, te );
	}

	@Override
	public boolean isBarrel( final TileEntity te )
	{
		return day_BarrelClass.isAssignableFrom( te.getClass() );
	}

	@Override
	public void grinderRecipe( final ItemStack in, final ItemStack out )
	{
		try
		{
			final Class<?> c = Class.forName( "factorization.oreprocessing.TileEntityGrinder" );
			final Method m = c.getMethod( "addRecipe", Object.class, ItemStack.class, float.class );

			final float amt = out.stackSize;
			out.stackSize = 1;

			m.invoke( c, in, out, amt );
		}
		catch( final Throwable t )
		{
			// AELog.info( "" );
			// throw new RuntimeException( t );
		}
	}

	@Override
	public void init() throws Throwable
	{
		day_BarrelClass = Class.forName( "factorization.weird.TileEntityDayBarrel" );

		day_getItemCount = day_BarrelClass.getDeclaredMethod( "getItemCount" );
		day_setItemCount = day_BarrelClass.getDeclaredMethod( "setItemCount", int.class );
		day_getMaxSize = day_BarrelClass.getDeclaredMethod( "getMaxSize" );
		day_item = day_BarrelClass.getDeclaredField( "item" );
	}

	@Override
	public void postInit()
	{
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new FactorizationHandler() );
	}
}
