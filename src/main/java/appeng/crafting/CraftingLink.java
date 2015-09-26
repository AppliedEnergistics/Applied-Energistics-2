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

package appeng.crafting;


import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.storage.data.IAEItemStack;


public class CraftingLink implements ICraftingLink
{

	final ICraftingRequester req;
	final ICraftingCPU cpu;
	final String CraftID;
	final boolean standalone;
	boolean canceled = false;
	boolean done = false;
	CraftingLinkNexus tie;

	public CraftingLink( final NBTTagCompound data, final ICraftingRequester req )
	{
		this.CraftID = data.getString( "CraftID" );
		this.canceled = data.getBoolean( "canceled" );
		this.done = data.getBoolean( "done" );
		this.standalone = data.getBoolean( "standalone" );

		if( !data.hasKey( "req" ) || !data.getBoolean( "req" ) )
		{
			throw new IllegalStateException( "Invalid Crafting Link for Object" );
		}

		this.req = req;
		this.cpu = null;
	}

	public CraftingLink( final NBTTagCompound data, final ICraftingCPU cpu )
	{
		this.CraftID = data.getString( "CraftID" );
		this.canceled = data.getBoolean( "canceled" );
		this.done = data.getBoolean( "done" );
		this.standalone = data.getBoolean( "standalone" );

		if( !data.hasKey( "req" ) || data.getBoolean( "req" ) )
		{
			throw new IllegalStateException( "Invalid Crafting Link for Object" );
		}

		this.cpu = cpu;
		this.req = null;
	}

	@Override
	public boolean isCanceled()
	{
		if( this.canceled )
		{
			return true;
		}

		if( this.done )
		{
			return false;
		}

		if( this.tie == null )
		{
			return false;
		}

		return this.tie.isCanceled();
	}

	@Override
	public boolean isDone()
	{
		if( this.done )
		{
			return true;
		}

		if( this.canceled )
		{
			return false;
		}

		if( this.tie == null )
		{
			return false;
		}

		return this.tie.isDone();
	}

	@Override
	public void cancel()
	{
		if( this.done )
		{
			return;
		}

		this.canceled = true;

		if( this.tie != null )
		{
			this.tie.cancel();
		}

		this.tie = null;
	}

	@Override
	public boolean isStandalone()
	{
		return this.standalone;
	}

	@Override
	public void writeToNBT( final NBTTagCompound tag )
	{
		tag.setString( "CraftID", this.CraftID );
		tag.setBoolean( "canceled", this.canceled );
		tag.setBoolean( "done", this.done );
		tag.setBoolean( "standalone", this.standalone );
		tag.setBoolean( "req", this.req != null );
	}

	@Override
	public String getCraftingID()
	{
		return this.CraftID;
	}

	public void setNexus( final CraftingLinkNexus n )
	{
		if( this.tie != null )
		{
			this.tie.remove( this );
		}

		if( this.canceled && n != null )
		{
			n.cancel();
			this.tie = null;
			return;
		}

		this.tie = n;

		if( n != null )
		{
			n.add( this );
		}
	}

	public IAEItemStack injectItems( final IAEItemStack input, final Actionable mode )
	{
		if( this.tie == null || this.tie.req == null || this.tie.req.req == null )
		{
			return input;
		}

		return this.tie.req.req.injectCraftedItems( this.tie.req, input, mode );
	}

	public void markDone()
	{
		if( this.tie != null )
		{
			this.tie.markDone();
		}
	}
}
