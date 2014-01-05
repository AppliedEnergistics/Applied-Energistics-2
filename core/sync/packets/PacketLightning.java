package appeng.core.sync.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import appeng.client.ClientHelper;
import appeng.client.render.effects.LightningEffect;
import appeng.core.Configuration;
import appeng.core.sync.AppEngPacket;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketLightning extends AppEngPacket
{

	final double x;
	final double y;
	final double z;

	// automatic.
	public PacketLightning(DataInputStream stream) throws IOException {
		x = stream.readFloat();
		y = stream.readFloat();
		z = stream.readFloat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkManager network, AppEngPacket packet, EntityPlayer player)
	{
		try
		{
			if ( Platform.isClient() && Configuration.instance.enableEffects )
			{
				LightningEffect fx = new LightningEffect( ClientHelper.proxy.getWorld(), x, y, z, 0.0f, 0.0f, 0.0f );
				Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
			}
		}
		catch (Exception err)
		{
		}
	}

	// api
	public PacketLightning(double x, double y, double z) throws IOException {

		this.x = x;
		this.y = y;
		this.z = z;

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );
		data.writeFloat( (float) x );
		data.writeFloat( (float) y );
		data.writeFloat( (float) z );

		isChunkDataPacket = false;
		configureWrite( bytes.toByteArray() );
	}

}
