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

package appeng.server.subcommands;


import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import appeng.core.AEConfig;
import appeng.server.ISubCommand;


public class TimeDilation implements ISubCommand
{

	@Override
	public void call( MinecraftServer srv, String[] data, ICommandSender sender )
	{
		boolean mode = AEConfig.instance.timeDilationEnabled;
		double time = AEConfig.instance.timeDilationTimePerTick;
		int multiplier = AEConfig.instance.timeDilationGridSizeMultiplier;

		try
		{
			mode = Boolean.valueOf( data[1] );
		}
		catch ( Exception e )
		{}

		try
		{
			time = Double.valueOf( data[2] );
			if ( time < 0.01 )
			{
				time = 0.01;
			}
		}
		catch ( Exception e )
		{}

		try
		{
			multiplier = Math.abs( Integer.valueOf( data[3] ) );
			if ( multiplier < 1 )
			{
				multiplier = 1;
			}
		}
		catch ( Exception e )
		{}

		AEConfig.instance.timeDilationEnabled = mode;
		AEConfig.instance.timeDilationTimePerTick = time;
		AEConfig.instance.timeDilationGridSizeMultiplier = multiplier;

		sender.addChatMessage( new ChatComponentText( "Time Dilation is now: " + AEConfig.instance.timeDilationEnabled + ", time per Tick: " + AEConfig.instance.timeDilationTimePerTick + ", size multiplier: " + AEConfig.instance.timeDilationGridSizeMultiplier ) );
	}

	@Override
	public String getHelp( MinecraftServer srv )
	{
		return "commands.ae2.TimeDilation";
	}

}
