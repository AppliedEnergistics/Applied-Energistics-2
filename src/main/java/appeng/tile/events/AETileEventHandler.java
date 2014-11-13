package appeng.tile.events;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.nbt.NBTTagCompound;
import appeng.tile.AEBaseTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AETileEventHandler
{

	private final Method method;

	public AETileEventHandler(Method m, TileEventType which)
	{
		method = m;
	}

	// TICK
	public void Tick(AEBaseTile tile)
	{
		try
		{
			method.invoke( tile );
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException( e );
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException( e );
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException( e );
		}
	}

	// WORLD_NBT
	public void writeToNBT(AEBaseTile tile, NBTTagCompound data)
	{
		try
		{
			method.invoke( tile, data );
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException( e );
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException( e );
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException( e );
		}
	}

	// WORLD NBT
	public void readFromNBT(AEBaseTile tile, NBTTagCompound data)
	{
		try
		{
			method.invoke( tile, data );
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException( e );
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException( e );
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException( e );
		}
	}

	// NETWORK
	public void writeToStream(AEBaseTile tile, ByteBuf data)
	{
		try
		{
			method.invoke( tile, data );
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException( e );
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException( e );
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException( e );
		}
	}

	// NETWORK
	/**
	 * returning true from this method, will update the block's render
	 * 
	 * @param data data of stream
	 * @return true of method could be invoked
	 */
	@SideOnly(Side.CLIENT)
	public boolean readFromStream(AEBaseTile tile, ByteBuf data)
	{
		try
		{
			return (Boolean) method.invoke( tile, data );
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException( e );
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException( e );
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException( e );
		}
	}

}
