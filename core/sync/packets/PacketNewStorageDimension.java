package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;
import appeng.core.AEConfig;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketNewStorageDimension extends AppEngPacket
{

	final int newDim;

	// automatic.
	public PacketNewStorageDimension(ByteBuf stream) throws IOException {
		newDim = stream.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		DimensionManager.registerDimension( newDim, AEConfig.instance.storageProviderID );
	}

	// api
	public PacketNewStorageDimension(int newDim) throws IOException {

		this.newDim = newDim;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( newDim );

		configureWrite( data );
	}

}
