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


import java.util.List;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


public class TileChunkLoader extends AEBaseTile implements ITickableTileEntity
{

	private boolean requestTicket = true;
	private Ticket ct = null;

	@Override
	public void tick()
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

		this.ct = ForgeChunkManager.requestTicket( AppEng.instance(), this.world, Type.NORMAL );

		if( this.ct == null )
		{
			final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if( server != null )
			{
				final List<ServerPlayerEntity> pl = server.getPlayerList().getPlayers();
				for( final ServerPlayerEntity p : pl )
				{
					p.sendMessage( new StringTextComponent( "Can't chunk load.." ) );
				}
			}
			return;
		}

		AELog.info( "New Ticket " + this.ct.toString() );
		ForgeChunkManager.forceChunk( this.ct, new ChunkPos( this.pos.getX() >> 4, this.pos.getZ() >> 4 ) );
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
