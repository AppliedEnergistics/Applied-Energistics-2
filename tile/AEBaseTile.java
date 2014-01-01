package appeng.tile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.util.ICommonTile;
import appeng.api.util.IOrientable;
import appeng.core.AELog;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.util.SettingsFrom;

public class AEBaseTile extends TileEntity implements IOrientable, ICommonTile
{

	private final EnumMap<TileEventType, List<AETileEventHandler>> handlers = new EnumMap<TileEventType, List<AETileEventHandler>>( TileEventType.class );

	private ForgeDirection forward = ForgeDirection.UNKNOWN;
	private ForgeDirection up = ForgeDirection.UNKNOWN;

	public boolean dropItems = true;
	public int renderFragment = 0;

	/**
	 * isRedstonePowerd has already changed.
	 */
	public void onRedstoneEvent()
	{

	}

	protected boolean hasHandlerFor(TileEventType type)
	{
		List<AETileEventHandler> list = handlers.get( type );
		return list != null;
	}

	protected List<AETileEventHandler> getHandlerListFor(TileEventType type)
	{
		List<AETileEventHandler> list = handlers.get( type );

		if ( list == null )
			handlers.put( type, list = new LinkedList<AETileEventHandler>() );

		return list;
	}

	protected void addNewHandler(AETileEventHandler handler)
	{
		EnumSet<TileEventType> types = handler.getSubscribedEvents();

		for (TileEventType type : types)
			getHandlerListFor( type ).add( handler );
	}

	@Override
	final public boolean canUpdate()
	{
		return hasHandlerFor( TileEventType.TICK );
	}

	@Override
	final public void updateEntity()
	{
		for (AETileEventHandler h : getHandlerListFor( TileEventType.TICK ))
			h.Tick();
	}

	@Override
	final public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );

		if ( canBeRotated() )
		{
			data.setString( "orientation_forward", forward.name() );
			data.setString( "orientation_up", up.name() );
		}

		for (AETileEventHandler h : getHandlerListFor( TileEventType.WORLD_NBT ))
			h.writeToNBT( data );
	}

	@Override
	final public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );

		try
		{
			if ( canBeRotated() )
			{
				forward = ForgeDirection.valueOf( data.getString( "orientation_forward" ) );
				up = ForgeDirection.valueOf( data.getString( "orientation_up" ) );
			}
		}
		catch (IllegalArgumentException iae)
		{
		}

		for (AETileEventHandler h : getHandlerListFor( TileEventType.WORLD_NBT ))
		{
			h.readFromNBT( data );
		}
	}

	final public void writeToStream(DataOutputStream data)
	{
		try
		{
			if ( canBeRotated() )
			{
				byte orientation = (byte) ((up.ordinal() << 3) | forward.ordinal());
				data.writeByte( orientation );
			}

			for (AETileEventHandler h : getHandlerListFor( TileEventType.NETWORK ))
				h.writeToStream( data );
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	final public boolean readfromStream(DataInputStream data)
	{
		boolean output = false;

		try
		{

			if ( canBeRotated() )
			{
				ForgeDirection old_Forward = forward;
				ForgeDirection old_Up = up;

				byte orientation = data.readByte();
				forward = ForgeDirection.getOrientation( orientation & 0x7 );
				up = ForgeDirection.getOrientation( orientation >> 3 );

				output = !forward.equals( old_Forward ) || !up.equals( old_Up );
			}

			renderFragment = 100;
			for (AETileEventHandler h : getHandlerListFor( TileEventType.NETWORK ))
				if ( h.readFromStream( data ) )
					output = true;

			if ( (renderFragment & 1) == 1 )
				output = true;
			renderFragment = 0;
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}

		return output;
	}

	/**
	 * By default all blocks can have orientation, this handles saving, and loading, as well as synchronization.
	 * 
	 * @return
	 */
	@Override
	public boolean canBeRotated()
	{
		return true;
	}

	@Override
	public ForgeDirection getForward()
	{
		return forward;
	}

	@Override
	public ForgeDirection getUp()
	{
		return up;
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		forward = inForward;
		up = inUp;
		markForUpdate();
		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, 0 );
	}

	public void onPlacement(ItemStack stack, EntityPlayer player, int side)
	{
		if ( stack.hasTagCompound() )
		{
			uploadSettings( SettingsFrom.DISMANTLE_ITEM, stack.getTagCompound() );
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound data = new NBTTagCompound();

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream( bytes );

		try
		{
			writeToStream( stream );
			if ( bytes.size() == 0 )
				return null;
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}

		data.setByteArray( "X", bytes.toByteArray() );
		return new Packet132TileEntityData( xCoord, yCoord, zCoord, 64, data );
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
	{
		if ( pkt.actionType == 64 )
		{
			DataInputStream stream = new DataInputStream( new ByteArrayInputStream( pkt.data.getByteArray( "X" ) ) );
			if ( readfromStream( stream ) )
				markForUpdate();
		}
	}

	public void markForUpdate()
	{
		if ( renderFragment > 0 )
			renderFragment = renderFragment | 1;
		else
		{
			if ( worldObj != null )
				worldObj.markBlockForUpdate( xCoord, yCoord, zCoord );
		}
	}

	/**
	 * returns the contents of the tile entity, into the world, defaults to dropping everything in the inventory.
	 * 
	 * @param w
	 * @param x
	 * @param y
	 * @param z
	 * @param drops
	 */
	@Override
	public void getDrops(World w, int x, int y, int z, ArrayList<ItemStack> drops)
	{
		if ( this instanceof IInventory )
		{
			IInventory inv = (IInventory) this;

			for (int l = 0; l < inv.getSizeInventory(); l++)
			{
				ItemStack is = inv.getStackInSlot( l );

				if ( is != null )
				{
					drops.add( is );
					inv.setInventorySlotContents( l, (ItemStack) null );
				}
			}
		}

	}

	public void onReady()
	{

	}

	/**
	 * depending on the from, diffrent settings will be accepted, don't call this with null
	 * 
	 * @param from
	 * @param compound
	 */
	public void uploadSettings(SettingsFrom from, NBTTagCompound compound)
	{

	}

	/**
	 * null means nothing to store...
	 * 
	 * @param from
	 * @return
	 */
	public NBTTagCompound downloadSettings(SettingsFrom from)
	{
		return null;
	}

}
