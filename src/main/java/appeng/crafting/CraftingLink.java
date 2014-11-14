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

	boolean canceled = false;
	boolean done = false;

	CraftingLinkNexus tie;

	final ICraftingRequester req;
	final ICraftingCPU cpu;

	final String CraftID;
	final boolean standalone;

	public CraftingLink(NBTTagCompound data, ICraftingRequester req) {
		CraftID = data.getString( "CraftID" );
		canceled = data.getBoolean( "canceled" );
		done = data.getBoolean( "done" );
		standalone = data.getBoolean( "standalone" );

		if ( !data.hasKey( "req" ) || !data.getBoolean( "req" ) )
			throw new RuntimeException( "Invalid Crafting Link for Object" );

		this.req = req;
		cpu = null;
	}

	public CraftingLink(NBTTagCompound data, ICraftingCPU cpu) {
		CraftID = data.getString( "CraftID" );
		canceled = data.getBoolean( "canceled" );
		done = data.getBoolean( "done" );
		standalone = data.getBoolean( "standalone" );

		if ( !data.hasKey( "req" ) || data.getBoolean( "req" ) )
			throw new RuntimeException( "Invalid Crafting Link for Object" );

		this.cpu = cpu;
		req = null;
	}

	@Override
	public boolean isCanceled()
	{
		if ( canceled )
			return true;

		if ( done )
			return false;

		if ( tie == null )
			return false;

		return tie.isCanceled();
	}

	@Override
	public boolean isDone()
	{
		if ( done )
			return true;

		if ( canceled )
			return false;

		if ( tie == null )
			return false;

		return tie.isDone();
	}

	@Override
	public void cancel()
	{
		if ( done )
			return;

		canceled = true;

		if ( tie != null )
			tie.cancel();

		tie = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		tag.setString( "CraftID", CraftID );
		tag.setBoolean( "canceled", canceled );
		tag.setBoolean( "done", done );
		tag.setBoolean( "standalone", standalone );
		tag.setBoolean( "req", req != null );
	}

	public void setNexus(CraftingLinkNexus n)
	{
		if ( tie != null )
			tie.remove( this );

		if ( canceled && n != null )
		{
			n.cancel();
			tie = null;
			return;
		}

		tie = n;

		if ( n != null )
			n.add( this );
	}

	@Override
	public String getCraftingID()
	{
		return CraftID;
	}

	@Override
	public boolean isStandalone()
	{
		return standalone;
	}

	public IAEItemStack injectItems(IAEItemStack input, Actionable mode)
	{
		if ( tie == null || tie.req == null || tie.req.req == null )
			return input;

		return tie.req.req.injectCraftedItems( tie.req, input, mode );
	}

	public void markDone()
	{
		if ( tie != null )
			tie.markDone();
	}
}
