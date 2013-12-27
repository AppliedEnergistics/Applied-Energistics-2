package appeng.tile.events;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class AETileEventHandler
{

	final EnumSet<TileEventType> supportedEvents;

	public AETileEventHandler(EnumSet<TileEventType> events) {
		supportedEvents = events;
	}

	public EnumSet<TileEventType> getSubscribedEvents()
	{
		return supportedEvents;
	}

	// TICK
	public void Tick()
	{
	}

	// WORLD_NBT
	public void writeToNBT(NBTTagCompound data)
	{
	}

	// WORLD NBT
	public void readFromNBT(NBTTagCompound data)
	{
	}

	// NETWORK
	public void writeToStream(DataOutputStream data) throws IOException
	{
	}

	// NETWORK
	/**
	 * returning true from this method, will update the block's render
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	@SideOnly(Side.CLIENT)
	public boolean readFromStream(DataInputStream data) throws IOException
	{
		return false;
	}

}
