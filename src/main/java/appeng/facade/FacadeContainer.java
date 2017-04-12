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

package appeng.facade;


import appeng.api.AEApi;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import appeng.items.parts.ItemFacade;
import appeng.parts.CableBusStorage;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;


public class FacadeContainer implements IFacadeContainer
{

	private final int facades = 6;
	private final CableBusStorage storage;

	public FacadeContainer( final CableBusStorage cbs )
	{
		this.storage = cbs;
	}

	@Override
	public boolean addFacade( final IFacadePart a )
	{
		if( this.getFacade( a.getSide() ) == null )
		{
			this.storage.setFacade( a.getSide().ordinal(), a );
			return true;
		}
		return false;
	}

	@Override
	public void removeFacade( final IPartHost host, final ForgeDirection side )
	{
		if( side != null && side != ForgeDirection.UNKNOWN )
		{
			if( this.storage.getFacade( side.ordinal() ) != null )
			{
				this.storage.setFacade( side.ordinal(), null );
				if( host != null )
				{
					host.markForUpdate();
				}
			}
		}
	}

	@Override
	public IFacadePart getFacade( final ForgeDirection s )
	{
		return this.storage.getFacade( s.ordinal() );
	}

	@Override
	public void rotateLeft()
	{
		final IFacadePart[] newFacades = new FacadePart[6];

		newFacades[ForgeDirection.UP.ordinal()] = this.storage.getFacade( ForgeDirection.UP.ordinal() );
		newFacades[ForgeDirection.DOWN.ordinal()] = this.storage.getFacade( ForgeDirection.DOWN.ordinal() );

		newFacades[ForgeDirection.EAST.ordinal()] = this.storage.getFacade( ForgeDirection.NORTH.ordinal() );
		newFacades[ForgeDirection.SOUTH.ordinal()] = this.storage.getFacade( ForgeDirection.EAST.ordinal() );

		newFacades[ForgeDirection.WEST.ordinal()] = this.storage.getFacade( ForgeDirection.SOUTH.ordinal() );
		newFacades[ForgeDirection.NORTH.ordinal()] = this.storage.getFacade( ForgeDirection.WEST.ordinal() );

		for( int x = 0; x < this.facades; x++ )
		{
			this.storage.setFacade( x, newFacades[x] );
		}
	}

	@Override
	public void writeToNBT( final NBTTagCompound c )
	{
		for( int x = 0; x < this.facades; x++ )
		{
			if( this.storage.getFacade( x ) != null )
			{
				final NBTTagCompound data = new NBTTagCompound();
				this.storage.getFacade( x ).getItemStack().writeToNBT( data );
				c.setTag( "facade:" + x, data );
			}
		}
	}

	@Override
	public boolean readFromStream( final ByteBuf out ) throws IOException
	{
		final int facadeSides = out.readByte();

		boolean changed = false;

		final int[] ids = new int[2];
		for( int x = 0; x < this.facades; x++ )
		{
			final ForgeDirection side = ForgeDirection.getOrientation( x );
			final int ix = ( 1 << x );
			if( ( facadeSides & ix ) == ix )
			{
				ids[0] = out.readInt();
				ids[1] = out.readInt();
				final boolean isBC = ids[0] < 0;
				ids[0] = Math.abs( ids[0] );

				if( isBC && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
				{
					final IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
					final IFacadePart created = bc.createFacadePart( (Block) Block.blockRegistry.getObjectById( ids[0] ), ids[1], side );
					changed = changed || this.storage.getFacade( x ) == null;

					this.storage.setFacade( x, created );
				}
				else if( !isBC )
				{
					for( final Item facadeItem : AEApi.instance().definitions().items().facade().maybeItem().asSet() )
					{
						final ItemFacade ifa = (ItemFacade) facadeItem;
						final ItemStack facade = ifa.createFromIDs( ids );
						if( facade != null )
						{
							changed = changed || this.storage.getFacade( x ) == null;
							this.storage.setFacade( x, ifa.createPartFromItemStack( facade, side ) );
						}
					}
				}
			}
			else
			{
				changed = changed || this.storage.getFacade( x ) != null;
				this.storage.setFacade( x, null );
			}
		}

		return changed;
	}

	@Override
	public void readFromNBT( final NBTTagCompound c )
	{
		for( int x = 0; x < this.facades; x++ )
		{
			this.storage.setFacade( x, null );

			final NBTTagCompound t = c.getCompoundTag( "facade:" + x );
			if( t != null )
			{
				final ItemStack is = ItemStack.loadItemStackFromNBT( t );
				if( is != null )
				{
					final Item i = is.getItem();
					if( i instanceof IFacadeItem )
					{
						this.storage.setFacade( x, ( (IFacadeItem) i ).createPartFromItemStack( is, ForgeDirection.getOrientation( x ) ) );
					}
					else
					{
						if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
						{
							final IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
							if( bc.isFacade( is ) )
							{
								this.storage.setFacade( x, bc.createFacadePart( is, ForgeDirection.getOrientation( x ) ) );
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void writeToStream( final ByteBuf out ) throws IOException
	{
		int facadeSides = 0;
		for( int x = 0; x < this.facades; x++ )
		{
			if( this.getFacade( ForgeDirection.getOrientation( x ) ) != null )
			{
				facadeSides |= ( 1 << x );
			}
		}
		out.writeByte( (byte) facadeSides );

		for( int x = 0; x < this.facades; x++ )
		{
			final IFacadePart part = this.getFacade( ForgeDirection.getOrientation( x ) );
			if( part != null )
			{
				final int itemID = Item.getIdFromItem( part.getItem() );
				final int dmgValue = part.getItemDamage();
				out.writeInt( itemID * ( part.notAEFacade() ? -1 : 1 ) );
				out.writeInt( dmgValue );
			}
		}
	}

	@Override
	public boolean isEmpty()
	{
		for( int x = 0; x < this.facades; x++ )
		{
			if( this.storage.getFacade( x ) != null )
			{
				return false;
			}
		}
		return true;
	}
}
