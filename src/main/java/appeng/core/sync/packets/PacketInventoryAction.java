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


import java.io.IOException;
import java.util.Collections;

import appeng.api.storage.data.IAEFluidStack;
import appeng.container.slot.IJEITargetSlot;
import appeng.container.slot.SlotFake;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.container.ContainerFluidConfigurable;
import appeng.fluids.util.AEFluidStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.core.AppEng;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraftforge.fluids.FluidStack;


public class PacketInventoryAction extends AppEngPacket
{

	private final InventoryAction action;
	private final int slot;
	private final long id;
	private final IAEItemStack slotItem;

	// automatic.
	public PacketInventoryAction( final ByteBuf stream ) throws IOException
	{
		this.action = InventoryAction.values()[stream.readInt()];
		this.slot = stream.readInt();
		this.id = stream.readLong();
		final boolean hasItem = stream.readBoolean();

		if( hasItem )
		{
			this.slotItem = AEItemStack.fromPacket( stream );
		}
		else
		{
			this.slotItem = null;
		}
	}

	// api
	public PacketInventoryAction( final InventoryAction action, final int slot, final IAEItemStack slotItem ) throws IOException
	{
		if( Platform.isClient() )
		{
			throw new IllegalStateException( "invalid packet, client cannot post inv actions with stacks." );
		}

		this.action = action;
		this.slot = slot;
		this.id = 0;
		this.slotItem = slotItem;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );
		data.writeLong( this.id );

		if( slotItem == null )
		{
			data.writeBoolean( false );
		}
		else
		{
			data.writeBoolean( true );
			slotItem.writeToPacket( data );
		}

		this.configureWrite( data );
	}

	public PacketInventoryAction(final InventoryAction action, final IJEITargetSlot slot, final IAEItemStack slotItem ) throws IOException
	{

		this.action = action;
		if (slot instanceof SlotFake)
		this.slot = ((SlotFake) slot).slotNumber;
		else this.slot = ((GuiFluidSlot) slot).getId();
		this.id = 0;
		this.slotItem = slotItem;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( this.slot );
		data.writeLong( this.id );

		if( slotItem == null )
		{
			data.writeBoolean( false );
		}
		else
		{
			data.writeBoolean( true );
			slotItem.writeToPacket( data );
		}

		this.configureWrite( data );
	}

	// api
	public PacketInventoryAction( final InventoryAction action, final int slot, final long id )
	{
		this.action = action;
		this.slot = slot;
		this.id = id;
		this.slotItem = null;

		final ByteBuf data = Unpooled.buffer();

		data.writeInt( this.getPacketID() );
		data.writeInt( action.ordinal() );
		data.writeInt( slot );
		data.writeLong( id );
		data.writeBoolean( false );

		this.configureWrite( data );
	}

	@Override
	public void serverPacketData( final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player )
	{
		final EntityPlayerMP sender = (EntityPlayerMP) player;
		if( sender.openContainer instanceof AEBaseContainer )
		{
			final AEBaseContainer baseContainer = (AEBaseContainer) sender.openContainer;
			if( this.action == InventoryAction.AUTO_CRAFT )
			{
				final ContainerOpenContext context = baseContainer.getOpenContext();
				if( context != null )
				{
					final TileEntity te = context.getTile();
					Platform.openGUI( sender, te, baseContainer.getOpenContext().getSide(), GuiBridge.GUI_CRAFTING_AMOUNT );

					if( sender.openContainer instanceof ContainerCraftAmount )
					{
						final ContainerCraftAmount cca = (ContainerCraftAmount) sender.openContainer;

						if( baseContainer.getTargetStack() != null )
						{
							cca.getCraftingItem().putStack( baseContainer.getTargetStack().asItemStackRepresentation() );
							// This is the *actual* item that matters, not the display item above
							cca.setItemToCraft( baseContainer.getTargetStack() );
						}

						cca.detectAndSendChanges();
					}
				}
			}
			else if( this.action == InventoryAction.PLACE_JEI_GHOST_ITEM )
			{
				if( sender.openContainer instanceof ContainerFluidConfigurable )
				{
					if( this.slotItem != null )
					{
						IAEFluidStack aefs = AEFluidStack.fromNBT( this.slotItem.getDefinition().getTagCompound() );
						if( aefs != null )
						{
							aefs.setStackSize( 1000 );
							( (ContainerFluidConfigurable) sender.openContainer ).getFluidConfigInventory().setFluidInSlot( this.slot, aefs );
							NetworkHandler.instance().sendToServer( new PacketFluidSlot( Collections.singletonMap( this.slot, aefs ) ) );
						}
					}
				}
				else if( this.slot < sender.openContainer.inventorySlots.size() && sender.openContainer.inventorySlots.get( this.slot ) instanceof SlotFake )
				{
					if( this.slotItem != null )
					{
						sender.openContainer.inventorySlots.get( this.slot ).putStack( this.slotItem.createItemStack() );
						if( sender.openContainer.inventorySlots.get( this.slot ).getStack().isEmpty() )
						{
							IAEFluidStack aefs = AEFluidStack.fromNBT( this.slotItem.getDefinition().getTagCompound() );
							if( aefs != null )
							{
								FluidStack fluid = aefs.getFluidStack();
								sender.openContainer.inventorySlots.get( this.slot ).putStack( AEFluidStack.fromFluidStack( fluid ).asItemStackRepresentation() );
							}
						}
					}
					else sender.openContainer.inventorySlots.get( this.slot ).putStack( ItemStack.EMPTY );
					try
					{
						NetworkHandler.instance().sendTo( new PacketInventoryAction( InventoryAction.UPDATE_HAND, 0, AEItemStack.fromItemStack( ItemStack.EMPTY ) ), sender );
					}
					catch( final IOException e )
					{
						AELog.debug( e );
					}
				}
			}
			else
			{
				baseContainer.doAction( sender, this.action, this.slot, this.id );
			}
		}
	}

	@Override
	public void clientPacketData( final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player )
	{
		if( this.action == InventoryAction.UPDATE_HAND )
		{
			if( this.slotItem == null )
			{
				AppEng.proxy.getPlayers().get( 0 ).inventory.setItemStack( ItemStack.EMPTY );
			}
			else
			{
				AppEng.proxy.getPlayers().get( 0 ).inventory.setItemStack( this.slotItem.createItemStack() );
			}
		}
	}
}
