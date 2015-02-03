/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.server;

import com.google.common.base.Joiner;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

public class AECommand extends CommandBase
{

	final MinecraftServer srv;

	public AECommand(MinecraftServer server) {
		this.srv = server;
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
					throw new WrongUsageException( c.command.getHelp( this.srv ) );
				}
			}
			catch ( WrongUsageException wrong )
			{
				throw wrong;
			}
			catch (Throwable er)
			{
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
				if ( sender.canCommandSenderUseCommand( c.level, this.getCommandName() ) )
					c.command.call( this.srv, args, sender );
				else
					throw new WrongUsageException( "commands.ae2.permissions" );
			}
			catch ( WrongUsageException wrong )
			{
				throw wrong;
			}
			catch (Throwable er)
			{
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
