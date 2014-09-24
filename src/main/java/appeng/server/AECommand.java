package appeng.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import com.google.common.base.Joiner;

public class AECommand extends CommandBase
{

	final MinecraftServer srv;

	public AECommand(MinecraftServer server) {
		srv = server;
	}

	@Override
	public String getCommandName()
	{
		return "ae2";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return "commands.ae2.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if ( args.length == 0 )
		{
			throw new WrongUsageException( "commands.ae2.usage" );
		}
		else if ( "help".equals( args[0] ) )
		{
			try
			{
				if ( args.length > 1 )
				{
					Commands c = Commands.valueOf( args[1] );
					throw new WrongUsageException( c.command.getHelp( srv ) );
				}
			}
			catch (Throwable er)
			{
				if ( er instanceof WrongUsageException )
					throw (WrongUsageException) er;
				throw new WrongUsageException( "commands.ae2.usage" );
			}
		}
		else if ( "list".equals( args[0] ) )
		{
			throw new WrongUsageException( Joiner.on( ", " ).join( Commands.values() ) );
		}
		else
		{
			try
			{
				Commands c = Commands.valueOf( args[0] );
				if ( sender.canCommandSenderUseCommand( c.level, getCommandName() ) )
					c.command.call( srv, args, sender );
				else
					throw new WrongUsageException( "commands.ae2.permissions" );
			}
			catch (Throwable er)
			{
				if ( er instanceof WrongUsageException )
					throw (WrongUsageException) er;
				throw new WrongUsageException( "commands.ae2.usage" );
			}
		}
	}

	/**
	 * wtf?
	 */
	@Override
	public int compareTo(Object arg0)
	{
		return 1;
	}
}
