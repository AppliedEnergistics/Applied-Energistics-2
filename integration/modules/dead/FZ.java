package appeng.integration.modules.dead;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IFZ;
import appeng.integration.modules.helpers.dead.FactorizationBarrel;
import appeng.integration.modules.helpers.dead.FactorizationHandler;
import appeng.util.Platform;

/**
 * 100% Hacks.
 */
public class FZ implements IFZ, IIntegrationModule
{

	public static FZ instance;

	private static Class day_BarrelClass;
	private static Method day_getItemCount;
	private static Method day_setItemCount;
	private static Method day_getMaxSize;
	private static Field day_item;

	private static Class barrelClass;
	private static Method getItemCount;
	private static Method setItemCount;
	private static Method getMaxSize;
	private static Field item;

	@Override
	public ItemStack barrelGetItem(TileEntity te)
	{
		try
		{
			ItemStack i;

			if ( day_BarrelClass.isInstance( te ) )
				i = (ItemStack) day_item.get( te );
			else
				i = (ItemStack) item.get( te );

			if ( i != null )
				i = Platform.cloneItemStack( i );
			return i;
		}
		catch (IllegalArgumentException e)
		{
		}
		catch (IllegalAccessException e)
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
			else
				return (Integer) getMaxSize.invoke( te );
		}
		catch (IllegalAccessException e)
		{
		}
		catch (IllegalArgumentException e)
		{
		}
		catch (InvocationTargetException e)
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
			else
				return (Integer) getItemCount.invoke( te );
		}
		catch (IllegalAccessException e)
		{
		}
		catch (IllegalArgumentException e)
		{
		}
		catch (InvocationTargetException e)
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
			else
				item.set( te, input == null ? null : input.copy() );

			te.markDirty();
		}
		catch (IllegalArgumentException e)
		{
		}
		catch (IllegalAccessException e)
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
			else
				setItemCount.invoke( te, max );

			te.markDirty();
		}
		catch (IllegalAccessException e)
		{
		}
		catch (IllegalArgumentException e)
		{
		}
		catch (InvocationTargetException e)
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
		if ( day_BarrelClass.isAssignableFrom( te.getClass() ) )
			return true;
		if ( barrelClass.isAssignableFrom( te.getClass() ) )
			return true;
		return false;
	}

	@Override
	public void Init() throws Throwable
	{
		barrelClass = Class.forName( "factorization.weird.TileEntityBarrel" );
		day_BarrelClass = Class.forName( "factorization.weird.TileEntityDayBarrel" );

		getItemCount = barrelClass.getDeclaredMethod( "getItemCount", new Class[] {} );
		setItemCount = barrelClass.getDeclaredMethod( "setItemCount", new Class[] { int.class } );
		getMaxSize = barrelClass.getDeclaredMethod( "getMaxSize", new Class[] {} );
		item = barrelClass.getDeclaredField( "item" );

		day_getItemCount = day_BarrelClass.getDeclaredMethod( "getItemCount", new Class[] {} );
		day_setItemCount = day_BarrelClass.getDeclaredMethod( "setItemCount", new Class[] { int.class } );
		day_getMaxSize = day_BarrelClass.getDeclaredMethod( "getMaxSize", new Class[] {} );
		day_item = day_BarrelClass.getDeclaredField( "item" );
	}

	@Override
	public void PostInit()
	{
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new FactorizationHandler() );

		// certus quartz
		grinderRecipe( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ), AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		grinderRecipe( AEApi.instance().materials().materialCertusQuartzCrystalCharged.stack( 1 ),
				AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ) );

		// fluix
		grinderRecipe( AEApi.instance().materials().materialFluixCrystal.stack( 1 ), AEApi.instance().materials().materialFluixDust.stack( 1 ) );

		// nether quartz
		grinderRecipe( new ItemStack( Item.netherQuartz ), AEApi.instance().materials().materialNetherQuartzDust.stack( 1 ) );
	}

	private void grinderRecipe(ItemStack in, ItemStack out)
	{
		try
		{
			Class c = Class.forName( "factorization.common.TileEntityGrinder" );
			Method m = c.getMethod( "addRecipe", Object.class, ItemStack.class, float.class );
			m.invoke( c, in, out, 1.0 );
		}
		catch (Throwable t)
		{
			// AELog.info( "" );
			// throw new RuntimeException( t );
		}
	}
}
