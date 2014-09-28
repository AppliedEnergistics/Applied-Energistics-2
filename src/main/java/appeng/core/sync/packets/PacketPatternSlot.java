package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.implementations.ContainerPatternTerm;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;

public class PacketPatternSlot extends AppEngPacket
{

	final public IAEItemStack slotItem;

	final public IAEItemStack pattern[] = new IAEItemStack[9];

	final public boolean shift;

	public IAEItemStack readItem(ByteBuf stream) throws IOException
	{
		boolean hasItem = stream.readBoolean();

		if ( hasItem )
			return AEItemStack.loadItemStackFromPacket( stream );

		return null;
	}

	// automatic.
	public PacketPatternSlot(ByteBuf stream) throws IOException {

		shift = stream.readBoolean();

		slotItem = readItem( stream );

		for (int x = 0; x < 9; x++)
			pattern[x] = readItem( stream );
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		if ( sender.openContainer instanceof ContainerPatternTerm )
		{
			ContainerPatternTerm patternTerminal = (ContainerPatternTerm) sender.openContainer;
			patternTerminal.craftOrGetItem( this );
		}
	}

	private void writeItem(IAEItemStack slotItem, ByteBuf data) throws IOException
	{
		if ( slotItem == null )
			data.writeBoolean( false );
		else
		{
			data.writeBoolean( true );
			slotItem.writeToPacket( data );
		}
	}

	// api
	public PacketPatternSlot(IInventory pat, IAEItemStack slotItem, boolean shift) throws IOException {

		this.slotItem = slotItem;
		this.shift = shift;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );

		data.writeBoolean( shift );

		writeItem( slotItem, data );
		for (int x = 0; x < 9; x++)
		{
			pattern[x] = AEApi.instance().storage().createItemStack( pat.getStackInSlot( x ) );
			writeItem( pattern[x], data );
		}

		configureWrite( data );
	}

}
