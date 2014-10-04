package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketMockExplosion extends AppEngPacket
{

	final public double x;
	final public double y;
	final public double z;

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		World world = CommonHelper.proxy.getWorld();
		world.spawnParticle( "largeexplode", this.x, this.y, this.z, 1.0D, 0.0D, 0.0D );
	}

	// automatic.
	public PacketMockExplosion(ByteBuf stream)
	{
		x = stream.readDouble();
		y = stream.readDouble();
		z = stream.readDouble();
	}

	// api
	public PacketMockExplosion(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeDouble( x );
		data.writeDouble( y );
		data.writeDouble( z );

		configureWrite( data );
	}

}
