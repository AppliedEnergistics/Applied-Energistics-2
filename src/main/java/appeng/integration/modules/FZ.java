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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IFZ;
import appeng.integration.modules.helpers.FactorizationBarrel;
import appeng.integration.modules.helpers.FactorizationHandler;
import appeng.util.Platform;

/**
 * 100% Hacks.
 */
public class FZ implements IFZ, IIntegrationModule
{

	public static FZ instance;

	private static Class<?> day_BarrelClass;
	private static Method day_getItemCount;
	private static Method day_setItemCount;
	private static Method day_getMaxSize;
	private static Field day_item;

	@Override
	public ItemStack barrelGetItem(TileEntity te)
	{
		try
		{
			ItemStack i = null;

			if ( day_BarrelClass.isInstance( te ) )
				i = (ItemStack) day_item.get( te );

			if ( i != null )
				i = Platform.cloneItemStack( i );

			return i;
		}
		catch (IllegalArgumentException ignored)
		{
		}
		catch (IllegalAccessException ignored)
		{
		}
		return null;
	}

	@Override
	public int barrelGetMaxItemCount(TileEntity te)
	{
		try
		{
			if ( day_BarrelClass.isInstance( te ) )
				return (Integer) day_getMaxSize.invoke( te );
		}
		catch (IllegalAccessException ignored)
		{
		}
		catch (IllegalArgumentException ignored)
		{
		}
		catch (InvocationTargetException ignored)
		{
		}
		return 0;
	}

	@Override
	public int barrelGetItemCount(TileEntity te)
	{
		try
		{
			if ( day_BarrelClass.isInstance( te ) )
				return (Integer) day_getItemCount.invoke( te );
		}
		catch (IllegalAccessException ignored)
		{
		}
		catch (IllegalArgumentException ignored)
		{
		}
		catch (InvocationTargetException ignored)
		{
		}
		return 0;
	}

	@Override
	public void setItemType(TileEntity te, ItemStack input)
	{
		try
		{
			if ( day_BarrelClass.isInstance( te ) )
				day_item.set( te, input == null ? null : input.copy() );
		}
		catch (IllegalArgumentException ignored)
		{
		}
		catch (IllegalAccessException ignored)
		{
		}
	}

	@Override
	public void barrelSetCount(TileEntity te, int max)
	{
		try
		{
			if ( day_BarrelClass.isInstance( te ) )
				day_setItemCount.invoke( te, max );

			te.markDirty();
		}
		catch (IllegalAccessException ignored)
		{
		}
		catch (IllegalArgumentException ignored)
		{
		}
		catch (InvocationTargetException ignored)
		{
		}
	}

	@Override
	public IMEInventory getFactorizationBarrel(TileEntity te)
	{
		return new FactorizationBarrel( this, te );
	}

	@Override
	public boolean isBarrel(TileEntity te)
	{
		return day_BarrelClass.isAssignableFrom( te.getClass() );
	}

	@Override
	public void Init() throws Throwable
	{
		day_BarrelClass = Class.forName( "factorization.weird.TileEntityDayBarrel" );

		day_getItemCount = day_BarrelClass.getDeclaredMethod( "getItemCount" );
		day_setItemCount = day_BarrelClass.getDeclaredMethod( "setItemCount", int.class );
		day_getMaxSize = day_BarrelClass.getDeclaredMethod( "getMaxSize" );
		day_item = day_BarrelClass.getDeclaredField( "item" );
	}

	@Override
	public void PostInit()
	{
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new FactorizationHandler() );
	}

	@Override
	public void grinderRecipe(ItemStack in, ItemStack out)
	{
		try
		{
			Class<?> c = Class.forName( "factorization.oreprocessing.TileEntityGrinder" );
			Method m = c.getMethod( "addRecipe", Object.class, ItemStack.class, float.class );

			float amt = out.stackSize;
			out.stackSize = 1;

			m.invoke( c, in, out, amt );
		}
		catch (Throwable t)
		{
			// AELog.info( "" );
			// throw new RuntimeException( t );
		}
	}
}
