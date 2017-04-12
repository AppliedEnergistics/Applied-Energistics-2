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

package appeng.fmp;


import appeng.api.parts.*;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.facade.FacadeContainer;
import appeng.parts.CableBusStorage;
import appeng.util.Platform;
import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;
import java.util.Set;


public class FMPPlacementHelper implements IPartHost
{

	private static final CableBusStorage NULL_STORAGE = new NullStorage();
	private boolean hasPart = false;
	private TileMultipart myMP;
	private CableBusPart myPart;

	public FMPPlacementHelper( final TileMultipart mp )
	{
		this.myMP = mp;
	}

	@Override
	public IFacadeContainer getFacadeContainer()
	{
		if( this.myPart == null )
		{
			return new FacadeContainer( NULL_STORAGE );
		}
		return this.myPart.getFacadeContainer();
	}

	@Override
	public boolean canAddPart( final ItemStack part, final ForgeDirection side )
	{
		final CableBusPart myPart = this.getPart();

		final boolean returnValue = this.hasPart && myPart.canAddPart( part, side );

		this.removePart();

		return returnValue;
	}

	private CableBusPart getPart()
	{
		final scala.collection.Iterator<TMultiPart> i = this.myMP.partList().iterator();
		while( i.hasNext() )
		{
			final TMultiPart p = i.next();
			if( p instanceof CableBusPart )
			{
				this.myPart = (CableBusPart) p;
			}
		}

		if( this.myPart == null )
		{
			this.myPart = (CableBusPart) PartRegistry.CableBusPart.construct( 0 );
		}

		final BlockCoord loc = new BlockCoord( this.myMP.xCoord, this.myMP.yCoord, this.myMP.zCoord );

		if( this.myMP.canAddPart( this.myPart ) && Platform.isServer() )
		{
			this.myMP = TileMultipart.addPart( this.myMP.getWorldObj(), loc, this.myPart );
			this.hasPart = true;
		}

		return this.myPart;
	}

	private void removePart()
	{
		if( this.myPart.isEmpty() )
		{
			final scala.collection.Iterator<TMultiPart> i = this.myMP.partList().iterator();
			while( i.hasNext() )
			{
				final TMultiPart p = i.next();
				if( p == this.myPart )
				{
					this.myMP = this.myMP.remPart( this.myPart );
					break;
				}
			}
			this.hasPart = false;
			this.myPart = null;
		}
	}

	@Override
	public ForgeDirection addPart( final ItemStack is, final ForgeDirection side, final EntityPlayer owner )
	{
		final CableBusPart myPart = this.getPart();

		final ForgeDirection returnValue = this.hasPart ? myPart.addPart( is, side, owner ) : null;

		this.removePart();

		return returnValue;
	}

	@Override
	public IPart getPart( final ForgeDirection side )
	{
		if( this.myPart == null )
		{
			return null;
		}
		return this.myPart.getPart( side );
	}

	@Override
	public void removePart( final ForgeDirection side, final boolean suppressUpdate )
	{
		if( this.myPart == null )
		{
			return;
		}
		this.myPart.removePart( side, suppressUpdate );
	}

	@Override
	public void markForUpdate()
	{
		if( this.myPart == null )
		{
			return;
		}
		this.myPart.markForUpdate();
	}

	@Override
	public DimensionalCoord getLocation()
	{
		if( this.myPart == null )
		{
			return new DimensionalCoord( this.myMP );
		}
		return this.myPart.getLocation();
	}

	@Override
	public TileEntity getTile()
	{
		return this.myMP;
	}

	@Override
	public AEColor getColor()
	{
		if( this.myPart == null )
		{
			return AEColor.Transparent;
		}
		return this.myPart.getColor();
	}

	@Override
	public void clearContainer()
	{
		if( this.myPart == null )
		{
			return;
		}
		this.myPart.clearContainer();
	}

	@Override
	public boolean isBlocked( final ForgeDirection side )
	{
		this.getPart();

		final boolean returnValue = this.myPart.isBlocked( side );

		this.removePart();

		return returnValue;
	}

	@Override
	public SelectedPart selectPart( final Vec3 pos )
	{
		if( this.myPart == null )
		{
			return new SelectedPart();
		}
		return this.myPart.selectPart( pos );
	}

	@Override
	public void markForSave()
	{
		if( this.myPart == null )
		{
			return;
		}
		this.myPart.markForSave();
	}

	@Override
	public void partChanged()
	{
		if( this.myPart == null )
		{
			return;
		}
		this.myPart.partChanged();
	}

	@Override
	public boolean hasRedstone( final ForgeDirection side )
	{
		if( this.myPart == null )
		{
			return false;
		}
		return this.myPart.hasRedstone( side );
	}

	@Override
	public boolean isEmpty()
	{
		if( this.myPart == null )
		{
			return true;
		}
		return this.myPart.isEmpty();
	}

	@Override
	public Set<LayerFlags> getLayerFlags()
	{
		if( this.myPart == null )
		{
			return EnumSet.noneOf( LayerFlags.class );
		}
		return this.myPart.getLayerFlags();
	}

	@Override
	public void cleanup()
	{
		if( this.myPart == null )
		{
			return;
		}
		this.myPart.cleanup();
	}

	@Override
	public void notifyNeighbors()
	{
		if( this.myPart == null )
		{
			return;
		}
		this.myPart.notifyNeighbors();
	}

	@Override
	public boolean isInWorld()
	{
		if( this.myPart == null )
		{
			return this.myMP.getWorldObj() != null;
		}
		return this.myPart.isInWorld();
	}

	private static class NullStorage extends CableBusStorage
	{

		@Override
		public IFacadePart getFacade( final int x )
		{
			return null;
		}

		@Override
		public void setFacade( final int x, final IFacadePart facade )
		{

		}
	}
}
