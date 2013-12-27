package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.core.sync.AppEngPacket;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.network.Player;

public class PacketInventoryAction extends AppEngPacket
{

	final public InventoryAction action;
	final public int slot;
	final public IAEItemStack slotItem;

	// automatic.
	public PacketInventoryAction(DataInputStream stream) throws IOException {
		action = InventoryAction.values()[stream.readInt()];
		slot = stream.readInt();
		boolean hasItem = stream.readBoolean();
		if ( hasItem )
			slotItem = AEItemStack.loadItemStackFromPacket( stream );
		else
			slotItem = null;
	}

	@Override
	public void serverPacketData(INetworkManager manager, AppEngPacket packet, Player player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		AEBaseContainer aebc = (AEBaseContainer) sender.openContainer;
		aebc.doAction( sender, action, slot, slotItem );
	}

	// api
	public PacketInventoryAction(InventoryAction action, int slot, IAEItemStack slotItem) throws IOException {
		this.action = action;
		this.slot = slot;
		this.slotItem = slotItem;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

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

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}

}
