package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import appeng.core.sync.AppEngPacket;
import appeng.helpers.PartPlacement;

public class PacketPartPlacement extends AppEngPacket
{

	int x, y, z, face;

	// automatic.
	public PacketPartPlacement(DataInputStream stream) throws IOException {
		x = stream.readInt();
		y = stream.readInt();
		z = stream.readInt();
		face = stream.readByte();
	}

	@Override
	public void serverPacketData(INetworkManager manager, AppEngPacket packet, EntityPlayer player)
	{
		EntityPlayerMP sender = (EntityPlayerMP) player;
		PartPlacement.place( sender.getHeldItem(), x, y, z, face, sender, sender.worldObj, PartPlacement.PlaceType.INTERACT_FIRST_PASS, 0 );
	}

	// api
	public PacketPartPlacement(int x, int y, int z, int face) throws IOException {

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeInt( x );
		data.writeInt( y );
		data.writeInt( z );
		data.writeByte( face );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}

}
