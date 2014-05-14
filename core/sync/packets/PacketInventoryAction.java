package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.ClientHelper;
import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PacketInventoryAction extends AppEngPacket
{

	final public InventoryAction action;
	final public int slot;
	final public IAEItemStack slotItem;

	// automatic.
	public PacketInventoryAction(ByteBuf stream) throws IOException {
		action = InventoryAction.values()[stream.readInt()];
		slot = stream.readInt();
		boolean hasItem = stream.readBoolean();
		if ( hasItem )
			slotItem = AEItemStack.loadItemStackFromPacket( stream );
		else
			slotItem = null;
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		if ( sender.openContainer instanceof AEBaseContainer )
		{
			AEBaseContainer aebc = (AEBaseContainer) sender.openContainer;
			aebc.doAction( sender, action, slot );
		}
	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		if ( action == InventoryAction.UPDATE_HAND )
		{
			if ( slotItem == null )
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( null );
			else
				ClientHelper.proxy.getPlayers().get( 0 ).inventory.setItemStack( slotItem.getItemStack() );
		}
	}

	// api
	public PacketInventoryAction(InventoryAction action, int slot, IAEItemStack slotItem) throws IOException {

		if ( Platform.isClient() && slotItem != null )
			throw new RuntimeException( "invalid packet, client cannot post inv actions with stacks." );

		this.action = action;
		this.slot = slot;
		this.slotItem = slotItem;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );

		if ( slotItem == null )
			data.writeBoolean( false );
		else
		{
			data.writeBoolean( true );
			slotItem.writeToPacket( data );
		}

		configureWrite( data );
	}

}
