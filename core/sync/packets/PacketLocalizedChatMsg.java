package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.util.ChatMessageComponent;
import appeng.core.sync.AppEngPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketLocalizedChatMsg extends AppEngPacket
{

	final public String msg;

	// automatic.
	public PacketLocalizedChatMsg(DataInputStream stream) throws IOException {
		msg = stream.readUTF();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkManager network, AppEngPacket packet, EntityPlayer player)
	{
		((EntityPlayer) player).sendChatToPlayer( ChatMessageComponent.createFromTranslationWithSubstitutions( msg ) );
	}

	// api
	public PacketLocalizedChatMsg(String msg) throws IOException {
		this.msg = msg;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeUTF( msg );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}

}
