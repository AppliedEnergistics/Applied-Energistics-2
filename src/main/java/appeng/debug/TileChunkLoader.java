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

package appeng.debug;


import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

import java.util.List;


public class TileChunkLoader extends AEBaseTile
{

	private boolean requestTicket = true;
	private Ticket ct = null;

	@TileEvent( TileEventType.TICK )
	public void onTickEvent()
	{
		if( this.requestTicket )
		{
			this.requestTicket = false;
			this.initTicket();
		}
	}

	private void initTicket()
	{
		if( Platform.isClient() )
		{
			return;
		}

		this.ct = ForgeChunkManager.requestTicket( AppEng.instance(), this.worldObj, Type.NORMAL );

		if( this.ct == null )
		{
			final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if( server != null )
			{
				final List<EntityPlayerMP> pl = server.getConfigurationManager().playerEntityList;
				for( final EntityPlayerMP p : pl )
				{
					p.addChatMessage( new ChatComponentText( "Can't chunk load.." ) );
				}
			}
			return;
		}

		AELog.info( "New Ticket " + this.ct.toString() );
		ForgeChunkManager.forceChunk( this.ct, new ChunkCoordIntPair( this.xCoord >> 4, this.zCoord >> 4 ) );
	}

	@Override
	public void invalidate()
	{
		if( Platform.isClient() )
		{
			return;
		}

		AELog.info( "Released Ticket " + this.ct.toString() );
		ForgeChunkManager.releaseTicket( this.ct );
	}
}
