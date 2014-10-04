package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import appeng.client.render.effects.MatterCannonFX;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketMatterCannon extends AppEngPacket
{

	final double x;
	final double y;
	final double z;
	final double dx;
	final double dy;
	final double dz;
	final byte len;

	// automatic.
	public PacketMatterCannon(ByteBuf stream)
	{
		x = stream.readFloat();
		y = stream.readFloat();
		z = stream.readFloat();
		dx = stream.readFloat();
		dy = stream.readFloat();
		dz = stream.readFloat();
		len = stream.readByte();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		try
		{

			World world = FMLClientHandler.instance().getClient().theWorld;
			for (int a = 1; a < len; a++)
			{
				MatterCannonFX fx = new MatterCannonFX( world, x + dx * a, y + dy * a, z + dz * a, Items.diamond );

				Minecraft.getMinecraft().effectRenderer.addEffect( fx );
			}
		}
		catch (Exception ignored)
		{
		}
	}

	// api
	public PacketMatterCannon(double x, double y, double z, float dx, float dy, float dz, byte len)
	{
		float dl = dx * dx + dy * dy + dz * dz;
		float dlz = (float) Math.sqrt( dl );

		this.x = x;
		this.y = y;
		this.z = z;
		this.dx = dx / dlz;
		this.dy = dy / dlz;
		this.dz = dz / dlz;
		this.len = len;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeFloat( (float) x );
		data.writeFloat( (float) y );
		data.writeFloat( (float) z );
		data.writeFloat( (float) this.dx );
		data.writeFloat( (float) this.dy );
		data.writeFloat( (float) this.dz );
		data.writeByte( len );

		configureWrite( data );
	}

}
