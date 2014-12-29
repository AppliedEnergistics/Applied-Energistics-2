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

package appeng.helpers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.parts.automation.NonNullArrayIterator;
import appeng.util.InventoryAdaptor;

import com.google.common.collect.ImmutableSet;

public class MultiCraftingTracker
{

	final int size;
	final ICraftingRequester owner;

	Future<ICraftingJob>[] jobs = null;
	ICraftingLink[] links = null;

	public MultiCraftingTracker(ICraftingRequester o, int size) {
		this.owner = o;
		this.size = size;
	}

	public void readFromNBT(NBTTagCompound extra)
	{
		for (int x = 0; x < this.size; x++)
		{
			NBTTagCompound link = extra.getCompoundTag( "links-" + x );
			if ( link != null && !link.hasNoTags() )
				this.setLink( x, AEApi.instance().storage().loadCraftingLink( link, this.owner ) );
		}
	}

	public void writeToNBT(NBTTagCompound extra)
	{
		for (int x = 0; x < this.size; x++)
		{
			ICraftingLink link = this.getLink( x );
			if ( link != null )
			{
				NBTTagCompound ln = new NBTTagCompound();
				link.writeToNBT( ln );
				extra.setTag( "links-" + x, ln );
			}
		}
	}

	public boolean handleCrafting(int x, long itemToCraft, IAEItemStack ais, InventoryAdaptor d, World w, IGrid g, ICraftingGrid cg, BaseActionSource mySrc)
	{
		if ( ais != null && d.simulateAdd( ais.getItemStack() ) == null )
		{
			Future<ICraftingJob> craftingJob = this.getJob( x );
			if ( this.getLink( x ) != null )
			{
				return false;
			}
			else if ( craftingJob != null )
			{
				ICraftingJob job = null;
				try
				{
					if ( craftingJob.isDone() )
						job = craftingJob.get();

					if ( job != null )
					{
						this.setJob( x, null );
						this.setLink( x, cg.submitJob( job, this.owner, null, false, mySrc ) );
						return true;
					}
				}
				catch (InterruptedException e)
				{
					// :P
				}
				catch (ExecutionException e)
				{
					// :P
				}
			}
			else
			{
				if ( this.getLink( x ) == null )
				{
					IAEItemStack aisC = ais.copy();
					aisC.setStackSize( itemToCraft );
					this.setJob( x, cg.beginCraftingJob( w, g, mySrc, aisC, null ) );
				}
			}
		}
		return false;
	}

	ICraftingLink getLink(int slot)
	{
		if ( this.links == null )
			return null;

		return this.links[slot];
	}

	void setLink(int slot, ICraftingLink l)
	{
		if ( this.links == null )
			this.links = new ICraftingLink[this.size];

		this.links[slot] = l;

		boolean hasStuff = false;
		for (int x = 0; x < this.links.length; x++)
		{
			ICraftingLink g = this.links[x];

			if ( g == null || g.isCanceled() || g.isDone() )
				this.links[x] = null;
			else
				hasStuff = true;
		}

		if ( !hasStuff )
			this.links = null;
	}

	Future<ICraftingJob> getJob(int slot)
	{
		if ( this.jobs == null )
			return null;

		return this.jobs[slot];
	}

	void setJob(int slot, Future<ICraftingJob> l)
	{
		if ( this.jobs == null )
			this.jobs = new Future[this.size];

		this.jobs[slot] = l;

		boolean hasStuff = false;
		for (Future<ICraftingJob> job : this.jobs)
		{
			if ( job != null )
			{
				hasStuff = true;
			}
		}

		if ( !hasStuff )
			this.jobs = null;
	}

	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		if ( this.links == null )
			return ImmutableSet.of();

		return ImmutableSet.copyOf( new NonNullArrayIterator( this.links ) );
	}

	public void jobStateChange(ICraftingLink link)
	{
		if ( this.links != null )
		{
			for (int x = 0; x < this.links.length; x++)
			{
				if ( this.links[x] == link )
				{
					this.setLink( x, null );
					return;
				}
			}
		}
	}

	public int getSlot(ICraftingLink link)
	{
		if ( this.links != null )
		{
			for (int x = 0; x < this.links.length; x++)
			{
				if ( this.links[x] == link )
					return x;
			}
		}

		return -1;
	}

	public void cancel()
	{
		if ( this.links != null )
		{
			for (ICraftingLink l : this.links)
			{
				if ( l != null )
					l.cancel();
			}

			this.links = null;
		}

		if ( this.jobs != null )
		{
			for (Future<ICraftingJob> l : this.jobs)
			{
				if ( l != null )
					l.cancel( true );
			}

			this.jobs = null;
		}
	}

	public boolean isBusy(int slot)
	{
		return this.getLink( slot ) != null || this.getJob( slot ) != null;
	}
}
