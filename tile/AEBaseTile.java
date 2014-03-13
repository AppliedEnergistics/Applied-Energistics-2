package appeng.tile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.util.ICommonTile;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.api.util.IOrientable;
import appeng.core.AELog;
import appeng.core.features.ItemStackSrc;
import appeng.helpers.IPriorityHost;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public class AEBaseTile extends TileEntity implements IOrientable, ICommonTile
{

	private final EnumMap<TileEventType, List<AETileEventHandler>> handlers = new EnumMap<TileEventType, List<AETileEventHandler>>( TileEventType.class );
	private final static HashMap<Class, ItemStackSrc> myItem = new HashMap();

	private ForgeDirection forward = ForgeDirection.UNKNOWN;
	private ForgeDirection up = ForgeDirection.UNKNOWN;

	public boolean dropItems = true;
	public int renderFragment = 0;

	public TileEntity getTile()
	{
		return this;
	}

	static public void registerTileItem(Class c, ItemStackSrc wat)
	{
		myItem.put( c, wat );
	}

	protected ItemStack getItemFromTile(Object obj)
	{
		ItemStackSrc src = myItem.get( obj.getClass() );
		if ( src == null )
			return null;
		return src.stack( 1 );
	}

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

	final public void writeToStream(ByteBuf data)
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
			AELog.error( t );
		}
	}

	final public boolean readfromStream(ByteBuf data)
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
			AELog.error( t );
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
		worldObj.notifyBlocksOfNeighborChange( xCoord, yCoord, zCoord, Platform.air );
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

		ByteBuf stream = Unpooled.buffer();

		try
		{
			writeToStream( stream );
			if ( stream.readableBytes() == 0 )
				return null;
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}

		data.setByteArray( "X", stream.array() );
		return new S35PacketUpdateTileEntity( xCoord, yCoord, zCoord, 64, data );
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		// / pkt.actionType
		if ( pkt.func_148853_f() == 64 )
		{
			ByteBuf stream = Unpooled.copiedBuffer( pkt.func_148857_g().getByteArray( "X" ) );
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
			// TODO: Optimize Network Load
			if ( worldObj != null )
			{
				AELog.blockUpdate( xCoord, yCoord, zCoord, this );
				worldObj.markBlockForUpdate( xCoord, yCoord, zCoord );
			}
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
		if ( compound != null && this instanceof IConfigureableObject )
		{
			IConfigManager cm = ((IConfigureableObject) this).getConfigManager();
			if ( cm != null )
				cm.readFromNBT( compound );
		}

		if ( this instanceof IPriorityHost )
		{
			IPriorityHost pHost = (IPriorityHost) this;
			pHost.setPriority( compound.getInteger( "priority" ) );
		}

		if ( this instanceof ISegmentedInventory )
		{
			IInventory inv = ((ISegmentedInventory) this).getInventoryByName( "config" );
			if ( inv != null && inv instanceof AppEngInternalAEInventory )
			{
				AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
				AppEngInternalAEInventory tmp = new AppEngInternalAEInventory( null, target.getSizeInventory() );
				tmp.readFromNBT( compound, "config" );
				for (int x = 0; x < tmp.getSizeInventory(); x++)
					target.setInventorySlotContents( x, tmp.getStackInSlot( x ) );
			}
		}
	}

	/**
	 * null means nothing to store...
	 * 
	 * @param from
	 * @return
	 */
	public NBTTagCompound downloadSettings(SettingsFrom from)
	{
		NBTTagCompound output = new NBTTagCompound();

		if ( this instanceof IConfigureableObject )
		{
			IConfigManager cm = ((IConfigureableObject) this).getConfigManager();
			if ( cm != null )
				cm.writeToNBT( output );
		}

		if ( this instanceof IPriorityHost )
		{
			IPriorityHost pHost = (IPriorityHost) this;
			output.setInteger( "priority", pHost.getPriority() );
		}

		if ( this instanceof ISegmentedInventory )
		{
			IInventory inv = ((ISegmentedInventory) this).getInventoryByName( "config" );
			if ( inv != null && inv instanceof AppEngInternalAEInventory )
			{
				((AppEngInternalAEInventory) inv).writeToNBT( output, "config" );
			}
		}

		return output.hasNoTags() ? null : output;
	}

	public void securityBreak()
	{
		worldObj.func_147480_a( xCoord, yCoord, zCoord, true ); // worldObj.destroyBlock( xCoord, yCoord, zCoord, true
																// );
		dropItems = false;
	}

	public void saveChanges()
	{
		super.markDirty();
	}

	public boolean requiresTESR()
	{
		return false;
	}

}
