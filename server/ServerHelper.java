package appeng.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import appeng.core.CommonHelper;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;

public class ServerHelper extends CommonHelper
{

	@Override
	public List<EntityPlayer> getPlayers()
	{
		if ( !Platform.isClient() )
		{
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

			if ( server != null )
				return server.getConfigurationManager().playerEntityList;
		}

		return new ArrayList();
	}

	@Override
	public void sendToAllNearExcept(EntityPlayer p, double x, double y, double z, double dist, World w, Packet packet)
	{
		if ( Platform.isClient() )
			return;

		for (EntityPlayer o : getPlayers())
		{
			EntityPlayerMP entityplayermp = (EntityPlayerMP) o;

			if ( entityplayermp != p && entityplayermp.worldObj == w )
			{
				double dX = x - entityplayermp.posX;
				double dY = y - entityplayermp.posY;
				double dZ = z - entityplayermp.posZ;

				if ( dX * dX + dY * dY + dZ * dZ < dist * dist )
				{
					entityplayermp.playerNetServerHandler.sendPacketToPlayer( packet );
				}
			}
		}
	}

	@Override
	public void init()
	{

	}

	@Override
	public World getWorld()
	{
		throw new RuntimeException( "This is a server..." );
	}

	@Override
	public void bindTileEntitySpecialRenderer(Class tile, AEBaseBlock blk)
	{
		throw new RuntimeException( "This is a server..." );
	}

}
