/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.server.ISubCommand;


public class ChunkLogger implements ISubCommand
{

	boolean enabled = false;

	@SubscribeEvent
	public void onChunkLoadEvent( ChunkEvent.Load event )
	{
		if( !event.world.isRemote )
		{
			AELog.info( "Chunk Loaded:   " + event.getChunk().xPosition + ", " + event.getChunk().zPosition );
			this.displayStack();
		}
	}

	private void displayStack()
	{
		if( AEConfig.instance.isFeatureEnabled( AEFeature.ChunkLoggerTrace ) )
		{
			boolean output = false;
			for( StackTraceElement e : Thread.currentThread().getStackTrace() )
			{
				if( output )
				{
					AELog.info( "		" + e.getClassName() + '.' + e.getMethodName() + " (" + e.getLineNumber() + ')' );
				}
				else
				{
					output = e.getClassName().contains( "EventBus" ) && e.getMethodName().contains( "post" );
				}
			}
		}
	}

	@SubscribeEvent
	public void onChunkUnloadEvent( ChunkEvent.Unload unload )
	{
		if( !unload.world.isRemote )
		{
			AELog.info( "Chunk Unloaded: " + unload.getChunk().xPosition + ", " + unload.getChunk().zPosition );
			this.displayStack();
		}
	}

	@Override
	public String getHelp( MinecraftServer srv )
	{
		return "commands.ae2.ChunkLogger";
	}

	@Override
	public void call( MinecraftServer srv, String[] data, ICommandSender sender )
	{
		this.enabled = !this.enabled;

		if( this.enabled )
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
}
