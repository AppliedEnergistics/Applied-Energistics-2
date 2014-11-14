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
		owner = o;
		this.size = size;
	}

	public void readFromNBT(NBTTagCompound extra)
	{
		for (int x = 0; x < size; x++)
		{
			NBTTagCompound link = extra.getCompoundTag( "links-" + x );
			if ( link != null && !link.hasNoTags() )
				setLink( x, AEApi.instance().storage().loadCraftingLink( link, owner ) );
		}
	}

	public void writeToNBT(NBTTagCompound extra)
	{
		for (int x = 0; x < size; x++)
		{
			ICraftingLink link = getLink( x );
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
			Future<ICraftingJob> craftingJob = getJob( x );
			if ( getLink( x ) != null )
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
						setJob( x, null );
						setLink( x, cg.submitJob( job, owner, null, false, mySrc ) );
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
				if ( getLink( x ) == null )
				{
					IAEItemStack aisC = ais.copy();
					aisC.setStackSize( itemToCraft );
					setJob( x, cg.beginCraftingJob( w, g, mySrc, aisC, null ) );
				}
			}
		}
		return false;
	}

	ICraftingLink getLink(int slot)
	{
		if ( links == null )
			return null;

		return links[slot];
	}

	void setLink(int slot, ICraftingLink l)
	{
		if ( links == null )
			links = new ICraftingLink[size];

		links[slot] = l;

		boolean hasStuff = false;
		for (int x = 0; x < links.length; x++)
		{
			ICraftingLink g = links[x];

			if ( g == null || g.isCanceled() || g.isDone() )
				links[x] = null;
			else
				hasStuff = true;
		}

		if ( !hasStuff )
			links = null;
	}

	Future<ICraftingJob> getJob(int slot)
	{
		if ( jobs == null )
			return null;

		return jobs[slot];
	}

	void setJob(int slot, Future<ICraftingJob> l)
	{
		if ( jobs == null )
			jobs = new Future[size];

		jobs[slot] = l;

		boolean hasStuff = false;
		for (Future<ICraftingJob> job : jobs)
		{
			if ( job != null )
			{
				hasStuff = true;
			}
		}

		if ( !hasStuff )
			jobs = null;
	}

	public ImmutableSet<ICraftingLink> getRequestedJobs()
	{
		if ( links == null )
			return ImmutableSet.of();

		return ImmutableSet.copyOf( new NonNullArrayIterator( links ) );
	}

	public void jobStateChange(ICraftingLink link)
	{
		if ( links != null )
		{
			for (int x = 0; x < links.length; x++)
			{
				if ( links[x] == link )
				{
					setLink( x, null );
					return;
				}
			}
		}
	}

	public int getSlot(ICraftingLink link)
	{
		if ( links != null )
		{
			for (int x = 0; x < links.length; x++)
			{
				if ( links[x] == link )
					return x;
			}
		}

		return -1;
	}

	public void cancel()
	{
		if ( links != null )
		{
			for (ICraftingLink l : links)
			{
				if ( l != null )
					l.cancel();
			}

			links = null;
		}

		if ( jobs != null )
		{
			for (Future<ICraftingJob> l : jobs)
			{
				if ( l != null )
					l.cancel( true );
			}

			jobs = null;
		}
	}

	public boolean isBusy(int slot)
	{
		return getLink( slot ) != null || getJob( slot ) != null;
	}
}
