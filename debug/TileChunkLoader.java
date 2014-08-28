package appeng.debug;

import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;

public class TileChunkLoader extends AEBaseTile
{

	boolean requestTicket = true;
	Ticket ct;

	@TileEvent(TileEventType.TICK)
	public void Tick_TileChunkLoader()
	{
		if ( requestTicket )
		{
			requestTicket = false;
			initTicket();
		}
	}

	void initTicket()
	{
		if ( Platform.isClient() )
			return;

		ct = ForgeChunkManager.requestTicket( AppEng.instance, worldObj, Type.NORMAL );

		if ( ct == null )
		{
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if ( server != null )
			{
				List<EntityPlayerMP> pl = server.getConfigurationManager().playerEntityList;
				for (EntityPlayerMP p : pl)
				{
					p.addChatMessage( new ChatComponentText( "Can't chunk load.." ) );
				}
			}
			return;
		}

		AELog.info( "New Ticket " + ct.toString() );
		ForgeChunkManager.forceChunk( ct, new ChunkCoordIntPair( xCoord >> 4, zCoord >> 4 ) );
	}

	@Override
	public void invalidate()
	{
		if ( Platform.isClient() )
			return;

		AELog.info( "Released Ticket " + ct.toString() );
		ForgeChunkManager.releaseTicket( ct );
	}
}
