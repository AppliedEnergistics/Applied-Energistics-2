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

package appeng.core.sync.packets;


import java.util.concurrent.Future;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;


public class PacketCraftRequest extends AppEngPacket
{

	public final long amount;
	public final boolean heldShift;

	// automatic.
	public PacketCraftRequest( ByteBuf stream )
	{
		this.heldShift = stream.readBoolean();
		this.amount = stream.readLong();
	}

	public PacketCraftRequest( int craftAmt, boolean shift )
	{
		this.amount = craftAmt;
		this.heldShift = shift;

		ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeBoolean( shift );
		data.writeLong( this.amount );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( INetworkInfo manager, AppEngPacket packet, EntityPlayer player )
	{
		if( player.openContainer instanceof ContainerCraftAmount )
		{
			ContainerCraftAmount cca = (ContainerCraftAmount) player.openContainer;
			Object target = cca.getTarget();
			if( target instanceof IGridHost )
			{
				IGridHost gh = (IGridHost) target;
				IGridNode gn = gh.getGridNode( ForgeDirection.UNKNOWN );
				if( gn == null )
				{
					return;
				}

				IGrid g = gn.getGrid();
				if( g == null || cca.whatToMake == null )
				{
					return;
				}

				cca.whatToMake.setStackSize( this.amount );

				Future<ICraftingJob> futureJob = null;
				try
				{
					ICraftingGrid cg = g.getCache( ICraftingGrid.class );
					futureJob = cg.beginCraftingJob( cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.whatToMake, null );

					ContainerOpenContext context = cca.openContext;
					if( context != null )
					{
						TileEntity te = context.getTile();
						Platform.openGUI( player, te, cca.openContext.side, GuiBridge.GUI_CRAFTING_CONFIRM );

						if( player.openContainer instanceof ContainerCraftConfirm )
						{
							ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
							ccc.autoStart = this.heldShift;
							ccc.job = futureJob;
							cca.detectAndSendChanges();
						}
					}
				}
				catch( Throwable e )
				{
					if( futureJob != null )
					{
						futureJob.cancel( true );
					}
					AELog.error( e );
				}
			}
		}
	}
}
