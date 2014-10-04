package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.client.ClientHelper;
import appeng.client.render.effects.EnergyFx;
import appeng.core.CommonHelper;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketTransitionEffect extends AppEngPacket
{

	final double x;
	final double y;
	final double z;
	final ForgeDirection d;
	final public boolean mode;

	// automatic.
	public PacketTransitionEffect(ByteBuf stream)
	{
		x = stream.readFloat();
		y = stream.readFloat();
		z = stream.readFloat();
		d = ForgeDirection.getOrientation( stream.readByte() );
		mode = stream.readBoolean();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void clientPacketData(INetworkInfo network, AppEngPacket packet, EntityPlayer player)
	{
		World world = ClientHelper.proxy.getWorld();

		for (int zz = 0; zz < (mode ? 32 : 8); zz++)
			if ( CommonHelper.proxy.shouldAddParticles( Platform.getRandom() ) )
			{
				EnergyFx fx = new EnergyFx( world, x + (mode ? (Platform.getRandomInt() % 100) * 0.01 : (Platform.getRandomInt() % 100) * 0.005 - 0.25), y
						+ (mode ? (Platform.getRandomInt() % 100) * 0.01 : (Platform.getRandomInt() % 100) * 0.005 - 0.25), z
						+ (mode ? (Platform.getRandomInt() % 100) * 0.01 : (Platform.getRandomInt() % 100) * 0.005 - 0.25), Items.diamond );

				if ( !mode )
					fx.fromItem( d );

				fx.motionX = -0.1 * d.offsetX;
				fx.motionY = -0.1 * d.offsetY;
				fx.motionZ = -0.1 * d.offsetZ;

				Minecraft.getMinecraft().effectRenderer.addEffect( fx );
			}

		if ( mode )
		{
			Block block = world.getBlock( (int) x, (int) y, (int) z );

			Minecraft
					.getMinecraft()
					.getSoundHandler()
					.playSound(
							new PositionedSoundRecord( new ResourceLocation( block.stepSound.getBreakSound() ), (block.stepSound.getVolume() + 1.0F) / 2.0F,
									block.stepSound.getPitch() * 0.8F, (float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F ) );
		}
	}

	// api
	public PacketTransitionEffect(double x, double y, double z, ForgeDirection dir, boolean wasBlock)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.d = dir;
		this.mode = wasBlock;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( getPacketID() );
		data.writeFloat( (float) x );
		data.writeFloat( (float) y );
		data.writeFloat( (float) z );
		data.writeByte( this.d.ordinal() );
		data.writeBoolean( wasBlock );

		configureWrite( data );
	}

}
