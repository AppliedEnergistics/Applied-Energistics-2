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

package appeng.items.storage;


import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.util.WorldCoord;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.worlddata.WorldData;
import appeng.items.AEBaseItem;
import appeng.spatial.StorageHelper;
import appeng.spatial.StorageWorldProvider;
import appeng.util.Platform;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.EnumSet;
import java.util.List;


public class ItemSpatialStorageCell extends AEBaseItem implements ISpatialStorageCell
{
	private final int maxRegion;

	public ItemSpatialStorageCell( final int spatialScale )
	{
		super( Optional.of( spatialScale + "Cubed" ) );
		this.setFeature( EnumSet.of( AEFeature.SpatialIO ) );
		this.setMaxStackSize( 1 );
		this.maxRegion = spatialScale;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		final WorldCoord wc = this.getStoredSize( stack );
		if( wc.x > 0 )
		{
			lines.add( GuiText.StoredSize.getLocal() + ": " + wc.x + " x " + wc.y + " x " + wc.z );
		}
	}

	@Override
	public boolean isSpatialStorage( final ItemStack is )
	{
		return true;
	}

	@Override
	public int getMaxStoredDim( final ItemStack is )
	{
		return this.maxRegion;
	}

	@Override
	public World getWorld( final ItemStack is )
	{
		if( is.hasTagCompound() )
		{
			final NBTTagCompound c = is.getTagCompound();
			final int dim = c.getInteger( "StorageDim" );
			World w = DimensionManager.getWorld( dim );
			if( w == null )
			{
				DimensionManager.initDimension( dim );
				w = DimensionManager.getWorld( dim );
			}

			if( w != null )
			{
				if( w.provider instanceof StorageWorldProvider )
				{
					return w;
				}
			}
		}
		return null;
	}

	@Override
	public WorldCoord getStoredSize( final ItemStack is )
	{
		if( is.hasTagCompound() )
		{
			final NBTTagCompound c = is.getTagCompound();
			if( Platform.isServer() )
			{
				final int dim = c.getInteger( "StorageDim" );
				return WorldData.instance().dimensionData().getStoredSize( dim );
			}
			else
			{
				return new WorldCoord( c.getInteger( "sizeX" ), c.getInteger( "sizeY" ), c.getInteger( "sizeZ" ) );
			}
		}
		return new WorldCoord( 0, 0, 0 );
	}

	@Override
	public WorldCoord getMin( final ItemStack is )
	{
		final World w = this.getWorld( is );
		if( w != null )
		{
			final NBTTagCompound info = (NBTTagCompound) w.getWorldInfo().getAdditionalProperty( "storageCell" );
			if( info != null )
			{
				return new WorldCoord( info.getInteger( "minX" ), info.getInteger( "minY" ), info.getInteger( "minZ" ) );
			}
		}
		return new WorldCoord( 0, 0, 0 );
	}

	@Override
	public WorldCoord getMax( final ItemStack is )
	{
		final World w = this.getWorld( is );
		if( w != null )
		{
			final NBTTagCompound info = (NBTTagCompound) w.getWorldInfo().getAdditionalProperty( "storageCell" );
			if( info != null )
			{
				return new WorldCoord( info.getInteger( "maxX" ), info.getInteger( "maxY" ), info.getInteger( "maxZ" ) );
			}
		}
		return new WorldCoord( 0, 0, 0 );
	}

	@Override
	public TransitionResult doSpatialTransition( final ItemStack is, final World w, final WorldCoord min, final WorldCoord max, final boolean doTransition )
	{
		final WorldCoord scale = this.getStoredSize( is );

		final int targetX = max.x - min.x - 1;
		final int targetY = max.y - min.y - 1;
		final int targetZ = max.z - min.z - 1;
		final int maxSize = this.getMaxStoredDim( is );

		World destination = this.getWorld( is );

		if( ( scale.x == 0 && scale.y == 0 && scale.z == 0 ) || ( scale.x == targetX && scale.y == targetY && scale.z == targetZ ) )
		{
			if( targetX <= maxSize && targetY <= maxSize && targetZ <= maxSize )
			{
				if( destination == null )
				{
					destination = this.createNewWorld( is );
				}

				final int floorBuffer = 64;
				StorageHelper.getInstance().swapRegions( w, destination, min.x + 1, min.y + 1, min.z + 1, 1, floorBuffer + 1, 1, targetX - 1, targetY - 1, targetZ - 1 );
				this.setStoredSize( is, targetX, targetY, targetZ );

				return new TransitionResult( true, 0 );
			}
		}

		return new TransitionResult( false, 0 );
	}

	private World createNewWorld( final ItemStack is )
	{
		final NBTTagCompound c = Platform.openNbtData( is );
		final int newDim = DimensionManager.getNextFreeDimId();
		c.setInteger( "StorageDim", newDim );
		WorldData.instance().dimensionData().addStorageCell( newDim );
		DimensionManager.initDimension( newDim );
		return DimensionManager.getWorld( newDim );
	}

	private void setStoredSize( final ItemStack is, final int targetX, final int targetY, final int targetZ )
	{
		if( is.hasTagCompound() )
		{
			final NBTTagCompound c = is.getTagCompound();
			final int dim = c.getInteger( "StorageDim" );
			c.setInteger( "sizeX", targetX );
			c.setInteger( "sizeY", targetY );
			c.setInteger( "sizeZ", targetZ );
			WorldData.instance().dimensionData().setStoredSize( dim, targetX, targetY, targetZ );
		}
	}
}
