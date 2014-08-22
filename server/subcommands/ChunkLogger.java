package appeng.server.subcommands;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import appeng.core.AELog;
import appeng.server.ISubCommand;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ChunkLogger implements ISubCommand
{

	boolean enabled = false;

	@SubscribeEvent
	public void ChunkLoad(ChunkEvent.Load load)
	{
		if ( !load.world.isRemote )
		{
			AELog.info( "Chunk Loaded:   " + load.getChunk().xPosition + ", " + load.getChunk().zPosition );
		}
	}

	@SubscribeEvent
	public void ChunkLoad(ChunkEvent.Unload unload)
	{
		if ( !unload.world.isRemote )
		{
			AELog.info( "Chunk Unloaded: " + unload.getChunk().xPosition + ", " + unload.getChunk().zPosition );
		}
	}

	@Override
	public void call(MinecraftServer srv, String[] data, ICommandSender sender)
	{
		enabled = !enabled;

		if ( enabled )
		{
			MinecraftForge.EVENT_BUS.register( this );
			sender.addChatMessage( new ChatComponentTranslation( "commands.ae2.ChunkLoggerOn" ) );
		}
		else
		{
			MinecraftForge.EVENT_BUS.unregister( this );
			sender.addChatMessage( new ChatComponentTranslation( "commands.ae2.ChunkLoggerOff" ) );
		}
	}

	@Override
	public String getHelp(MinecraftServer srv)
	{
		return "commands.ae2.ChunkLogger";
	}

}
