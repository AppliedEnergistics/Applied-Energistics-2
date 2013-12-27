package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.INetworkManager;
import net.minecraft.world.World;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketMockExplosion extends AppEngPacket
{

	final public double x;
	final public double y;
	final public double z;

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkManager network, AppEngPacket packet, Player player)
	{
		World world = CommonHelper.proxy.getWorld();
		world.spawnParticle( "largeexplode", this.x, this.y, this.z, 1.0D, 0.0D, 0.0D );
	}

	// automatic.
	public PacketMockExplosion(DataInputStream stream) throws IOException {
		x = stream.readDouble();
		y = stream.readDouble();
		z = stream.readDouble();
	}

	// api
	public PacketMockExplosion(double x, double y, double z) throws IOException {
		this.x = x;
		this.y = y;
		this.z = z;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeDouble( x );
		data.writeDouble( y );
		data.writeDouble( z );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}

}
