package appeng.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import appeng.services.Profiler;

public class AECommand extends CommandBase
{

	public AECommand(MinecraftServer server) {
	}

	@Override
	public String getCommandName()
	{
		return "ae";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return "commands.ae.usage";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if ( args.length == 0 )
		{
			throw new WrongUsageException( "commands.ae.usage" );
		}
		else if ( "help".equals( args[0] ) )
		{
			throw new WrongUsageException( "commands.ae.usage" );
		}
		else if ( "profile".equals( args[0] ) )
		{
			if ( args.length > 1 && "start".equals( args[1] ) )
				beginProfile( sender, args );
			else if ( args.length > 1 && "stop".equals( args[1] ) )
				endProfile( sender, args );
			else
				throw new WrongUsageException( "commands.ae.profile.usage" );
		}
		else
		{
			throw new WrongUsageException( "commands.ae.usage" );
		}
	}

	private void endProfile(ICommandSender sender, String[] args)
	{
		if ( Profiler.instance == null )
		{
			throw new CommandException( "commands.ae.profiling.nojdk" );
		}

		if ( Profiler.instance.isRunning )
		{
			if ( Profiler.instance.profile )
			{
				Profiler.instance.profile = false;
				synchronized (Profiler.instance.lock)
				{
					Profiler.instance.lock.notify();
				}
			}
			else
				throw new CommandException( "commands.ae.profiling.inactive" );
		}
		else
			throw new CommandException( "commands.ae.profiling.nojdk" );
	}

	private void beginProfile(ICommandSender sender, String[] args)
	{
		if ( Profiler.instance == null )
		{
			throw new CommandException( "commands.ae.profiling.nojdk" );
		}

		if ( Profiler.instance.isRunning )
		{
			if ( Profiler.instance.profile )
				throw new CommandException( "commands.ae.profiling.active" );
			else
			{
				Profiler.instance.profile = true;
				synchronized (Profiler.instance.lock)
				{
					Profiler.instance.lock.notify();
				}
			}
		}
		else
			throw new CommandException( "commands.ae.profiling.nojdk" );
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
