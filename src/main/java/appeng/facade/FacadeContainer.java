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


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IBuildCraftTransport;
import appeng.items.parts.ItemFacade;
import appeng.parts.CableBusStorage;


public class FacadeContainer implements IFacadeContainer
{

	final int facades = 6;
	final CableBusStorage storage;

	public FacadeContainer( CableBusStorage cbs )
	{
		this.storage = cbs;
	}

	@Override
	public boolean addFacade( IFacadePart a )
	{
		if( this.getFacade( a.getSide() ) == null )
		{
			this.storage.setFacade( a.getSide().ordinal(), a );
			return true;
		}
		return false;
	}

	@Override
	public void removeFacade( IPartHost host, AEPartLocation side )
	{
		if( side != null && side != AEPartLocation.INTERNAL )
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
	public IFacadePart getFacade( AEPartLocation s )
	{
		return this.storage.getFacade( s.ordinal() );
	}

	@Override
	public void rotateLeft()
	{
		IFacadePart[] newFacades = new FacadePart[6];

		newFacades[AEPartLocation.UP.ordinal()] = this.storage.getFacade( AEPartLocation.UP.ordinal() );
		newFacades[AEPartLocation.DOWN.ordinal()] = this.storage.getFacade( AEPartLocation.DOWN.ordinal() );

		newFacades[AEPartLocation.EAST.ordinal()] = this.storage.getFacade( AEPartLocation.NORTH.ordinal() );
		newFacades[AEPartLocation.SOUTH.ordinal()] = this.storage.getFacade( AEPartLocation.EAST.ordinal() );

		newFacades[AEPartLocation.WEST.ordinal()] = this.storage.getFacade( AEPartLocation.SOUTH.ordinal() );
		newFacades[AEPartLocation.NORTH.ordinal()] = this.storage.getFacade( AEPartLocation.WEST.ordinal() );

		for( int x = 0; x < this.facades; x++ )
		{
			this.storage.setFacade( x, newFacades[x] );
		}
	}

	@Override
	public void writeToNBT( NBTTagCompound c )
	{
		for( int x = 0; x < this.facades; x++ )
		{
			if( this.storage.getFacade( x ) != null )
			{
				NBTTagCompound data = new NBTTagCompound();
				this.storage.getFacade( x ).getItemStack().writeToNBT( data );
				c.setTag( "facade:" + x, data );
			}
		}
	}

	@Override
	public boolean readFromStream( ByteBuf out ) throws IOException
	{
		int facadeSides = out.readByte();

		boolean changed = false;

		int[] ids = new int[2];
		for( int x = 0; x < this.facades; x++ )
		{
			AEPartLocation side = AEPartLocation.fromOrdinal( x );
			int ix = ( 1 << x );
			if( ( facadeSides & ix ) == ix )
			{
				ids[0] = out.readInt();
				ids[1] = out.readInt();
				boolean isBC = ids[0] < 0;
				ids[0] = Math.abs( ids[0] );

				if( isBC && IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
				{
					final IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
					final IBlockState state = Block.getStateById( ids[0] );
					final IFacadePart created = bc.createFacadePart( state, side );
					changed = changed || this.storage.getFacade( x ) == null;

					this.storage.setFacade( x, created );
				}
				else 
				if( !isBC )
				{
					for( Item facadeItem : AEApi.instance().definitions().items().facade().maybeItem().asSet() )
					{
						ItemFacade ifa = (ItemFacade) facadeItem;
						ItemStack facade = ifa.createFromIDs( ids );
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
	public void readFromNBT( NBTTagCompound c )
	{
		for( int x = 0; x < this.facades; x++ )
		{
			this.storage.setFacade( x, null );

			NBTTagCompound t = c.getCompoundTag( "facade:" + x );
			if( t != null )
			{
				ItemStack is = ItemStack.loadItemStackFromNBT( t );
				if( is != null )
				{
					Item i = is.getItem();
					if( i instanceof IFacadeItem )
					{
						this.storage.setFacade( x, ( (IFacadeItem) i ).createPartFromItemStack( is, AEPartLocation.fromOrdinal( x ) ) );
					}
					else
					{
						if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.BuildCraftTransport ) )
						{
							final IBuildCraftTransport bc = (IBuildCraftTransport) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.BuildCraftTransport );
							if( bc.isFacade( is ) )
							{
								this.storage.setFacade( x, bc.createFacadePart( is, AEPartLocation.fromOrdinal( x ) ) );
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void writeToStream( ByteBuf out ) throws IOException
	{
		int facadeSides = 0;
		for( int x = 0; x < this.facades; x++ )
		{
			if( this.getFacade( AEPartLocation.fromOrdinal( x ) ) != null )
			{
				facadeSides |= ( 1 << x );
			}
		}
		out.writeByte( (byte) facadeSides );

		for( int x = 0; x < this.facades; x++ )
		{
			IFacadePart part = this.getFacade( AEPartLocation.fromOrdinal( x ) );
			if( part != null )
			{
				int itemID = Item.getIdFromItem( part.getItem() );
				int dmgValue = part.getItemDamage();
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
