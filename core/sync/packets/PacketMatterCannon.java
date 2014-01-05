package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.INetworkManager;
import net.minecraft.world.World;
import appeng.client.render.effects.MatterCannonEffect;
import appeng.core.sync.AppEngPacket;
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
	public PacketMatterCannon(DataInputStream stream) throws IOException {
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
	public void clientPacketData(INetworkManager network, AppEngPacket packet, EntityPlayer player)
	{
		try
		{

			World world = FMLClientHandler.instance().getClient().theWorld;
			for (int a = 1; a < len; a++)
			{
				MatterCannonEffect fx = new MatterCannonEffect( world, x + dx * a, y + dy * a, z + dz * a, Item.diamond );

				Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
			}
		}
		catch (Exception err)
		{
		}
	}

	// api
	public PacketMatterCannon(double x, double y, double z, float dx, float dy, float dz, byte len) throws IOException {
		float dl = dx * dx + dy * dy + dz * dz;
		float dlz = (float) Math.sqrt( dl );

		this.x = x;
		this.y = y;
		this.z = z;
		this.dx = dx / dlz;
		this.dy = dy / dlz;
		this.dz = dz / dlz;
		this.len = len;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeFloat( (float) x );
		data.writeFloat( (float) y );
		data.writeFloat( (float) z );
		data.writeFloat( (float) this.dx );
		data.writeFloat( (float) this.dy );
		data.writeFloat( (float) this.dz );
		data.writeByte( len );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}

}
