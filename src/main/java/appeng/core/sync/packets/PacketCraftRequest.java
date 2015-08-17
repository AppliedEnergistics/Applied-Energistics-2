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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

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
import appeng.core.sync.AppEngPacketHandler;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;


public class PacketCraftRequest implements AppEngPacket, AppEngPacketHandler<PacketCraftRequest, IMessage>
{

	private long amount;
	private boolean heldShift;

	// automatic.
	public PacketCraftRequest()
	{
	}

	public PacketCraftRequest( final int craftAmt, final boolean shift )
	{
		this.amount = craftAmt;
		this.heldShift = shift;
	}

	@Override
	public IMessage onMessage( PacketCraftRequest message, MessageContext ctx )
	{
		final EntityPlayer player = ctx.getServerHandler().playerEntity;

		if( player.openContainer instanceof ContainerCraftAmount )
		{
			final ContainerCraftAmount cca = (ContainerCraftAmount) player.openContainer;
			final Object target = cca.getTarget();
			if( target instanceof IGridHost )
			{
				final IGridHost gh = (IGridHost) target;
				final IGridNode gn = gh.getGridNode( ForgeDirection.UNKNOWN );
				if( gn == null )
				{
					return null;
				}

				final IGrid g = gn.getGrid();
				if( g == null || cca.getItemToCraft() == null )
				{
					return null;
				}

				cca.getItemToCraft().setStackSize( this.amount );

				Future<ICraftingJob> futureJob = null;

				try
				{
					final ICraftingGrid cg = g.getCache( ICraftingGrid.class );
					futureJob = cg.beginCraftingJob( cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.getItemToCraft(), null );

					final ContainerOpenContext context = cca.getOpenContext();
					if( context != null )
					{
						final TileEntity te = context.getTile();
						Platform.openGUI( player, te, cca.getOpenContext().getSide(), GuiBridge.GUI_CRAFTING_CONFIRM );

						if( player.openContainer instanceof ContainerCraftConfirm )
						{
							final ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;

							ccc.setAutoStart( this.heldShift );
							ccc.setJob( futureJob );
							cca.detectAndSendChanges();
						}
					}
				}
				catch( final Throwable e )
				{
					if( futureJob != null )
					{
						futureJob.cancel( true );
					}
					AELog.error( e );
				}
			}
		}

		return null;
	}

	@Override
	public void fromBytes( ByteBuf buf )
	{
		this.heldShift = buf.readBoolean();
		this.amount = buf.readLong();
	}

	@Override
	public void toBytes( ByteBuf buf )
	{
		buf.writeBoolean( this.heldShift );
		buf.writeLong( this.amount );
	}
}
