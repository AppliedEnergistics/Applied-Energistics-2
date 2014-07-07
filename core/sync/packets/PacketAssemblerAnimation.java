package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.render.effects.AssemblerFX;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;

public class PacketAssemblerAnimation extends AppEngPacket
{

	int x, y, z;
	byte rate;
	IAEItemStack is;

	// automatic.
	public PacketAssemblerAnimation(ByteBuf stream) throws IOException {
		x = stream.readInt();
		y = stream.readInt();
		z = stream.readInt();
		rate = stream.readByte();
		is = AEItemStack.loadItemStackFromPacket( stream );
	}

	@Override
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		double d0 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);
		double d1 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);
		double d2 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);

		AssemblerFX fx = new AssemblerFX( Minecraft.getMinecraft().theWorld, x + d0, y + d1, z + d2, 0.0D, 0.0D, 0.0D, rate, is );
		Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
	}

	// api
	public PacketAssemblerAnimation(int x, int y, int z, byte rate, IAEItemStack is) throws IOException {

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeInt( this.x = x );
		data.writeInt( this.y = y );
		data.writeInt( this.z = z );
		data.writeByte( this.rate = rate );
		is.writeToPacket( data );
		this.is = is;

		configureWrite( data );
	}
}
