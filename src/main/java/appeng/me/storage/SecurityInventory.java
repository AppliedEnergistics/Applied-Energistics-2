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

package appeng.me.storage;


import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.tile.misc.TileSecurity;

import com.mojang.authlib.GameProfile;


public class SecurityInventory implements IMEInventoryHandler<IAEItemStack>
{

	public final IItemList<IAEItemStack> storedItems = AEApi.instance().storage().createItemList();
	final TileSecurity securityTile;

	public SecurityInventory( TileSecurity ts )
	{
		this.securityTile = ts;
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack input, Actionable type, BaseActionSource src )
	{
		if( this.hasPermission( src ) )
		{
			if( AEApi.instance().definitions().items().biometricCard().isSameAs( input.getItemStack() ) )
			{
				if( this.canAccept( input ) )
				{
					if( type == Actionable.SIMULATE )
					{
						return null;
					}

					this.storedItems.add( input );
					this.securityTile.inventoryChanged();
					return null;
				}
			}
		}
		return input;
	}

	private boolean hasPermission( BaseActionSource src )
	{
		if( src.isPlayer() )
		{
			try
			{
				return this.securityTile.getProxy().getSecurity().hasPermission( ( (PlayerSource) src ).player, SecurityPermissions.SECURITY );
			}
			catch( GridAccessException e )
			{
				// :P
			}
		}
		return false;
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		if( this.hasPermission( src ) )
		{
			IAEItemStack target = this.storedItems.findPrecise( request );
			if( target != null )
			{
				IAEItemStack output = target.copy();

				if( mode == Actionable.SIMULATE )
				{
					return output;
				}

				target.setStackSize( 0 );
				this.securityTile.inventoryChanged();
				return output;
			}
		}
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList out )
	{
		for( IAEItemStack ais : this.storedItems )
		{
			out.add( ais );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public boolean isPrioritized( IAEItemStack input )
	{
		return false;
	}

	@Override
	public boolean canAccept( IAEItemStack input )
	{
		if( input.getItem() instanceof IBiometricCard )
		{
			IBiometricCard tbc = (IBiometricCard) input.getItem();
			GameProfile newUser = tbc.getProfile( input.getItemStack() );

			int PlayerID = AEApi.instance().registries().players().getID( newUser );
			if( this.securityTile.getOwner() == PlayerID )
			{
				return false;
			}

			for( IAEItemStack ais : this.storedItems )
			{
				if( ais.isMeaningful() )
				{
					GameProfile thisUser = tbc.getProfile( ais.getItemStack() );
					if( thisUser == newUser )
					{
						return false;
					}

					if( thisUser != null && thisUser.equals( newUser ) )
					{
						return false;
					}
				}
			}

			return true;
		}
		return false;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public boolean validForPass( int i )
	{
		return true;
	}
}
