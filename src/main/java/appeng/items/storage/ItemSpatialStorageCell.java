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

package appeng.items.storage;


import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.util.WorldCoord;
import appeng.core.WorldSettings;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.materials.MaterialType;
import appeng.spatial.StorageHelper;
import appeng.spatial.StorageWorldProvider;
import appeng.util.Platform;


public class ItemSpatialStorageCell extends AEBaseItem implements ISpatialStorageCell
{

	final MaterialType component;
	final int maxRegion;

	public ItemSpatialStorageCell( MaterialType whichCell, int spatialScale )
	{
		super( ItemSpatialStorageCell.class, Optional.of( spatialScale + "Cubed" ) );
		setFeature( EnumSet.of( AEFeature.SpatialIO ) );
		setMaxStackSize( 1 );
		maxRegion = spatialScale;
		component = whichCell;
	}

	@Override
	public void addCheckedInformation( ItemStack stack, EntityPlayer player, List<String> lines, boolean displayAdditionalInformation )
	{
		WorldCoord wc = getStoredSize( stack );
		if ( wc.x > 0 )
			lines.add( GuiText.StoredSize.getLocal() + ": " + wc.x + " x " + wc.y + " x " + wc.z );
	}

	@Override
	public boolean isSpatialStorage( ItemStack is )
	{
		return true;
	}

	@Override
	public int getMaxStoredDim( ItemStack is )
	{
		return maxRegion;
	}

	@Override
	public World getWorld( ItemStack is )
	{
		if ( is.hasTagCompound() )
		{
			NBTTagCompound c = is.getTagCompound();
			int dim = c.getInteger( "StorageDim" );
			World w = DimensionManager.getWorld( dim );
			if ( w == null )
			{
				DimensionManager.initDimension( dim );
				w = DimensionManager.getWorld( dim );
			}

			if ( w != null )
			{
				if ( w.provider instanceof StorageWorldProvider )
				{
					return w;
				}
			}
		}
		return null;
	}

	@Override
	public WorldCoord getStoredSize( ItemStack is )
	{
		if ( is.hasTagCompound() )
		{
			NBTTagCompound c = is.getTagCompound();
			if ( Platform.isServer() )
			{
				int dim = c.getInteger( "StorageDim" );
				return WorldSettings.getInstance().getStoredSize( dim );
			}
			else
				return new WorldCoord( c.getInteger( "sizeX" ), c.getInteger( "sizeY" ), c.getInteger( "sizeZ" ) );
		}
		return new WorldCoord( 0, 0, 0 );
	}

	@Override
	public WorldCoord getMin( ItemStack is )
	{
		World w = getWorld( is );
		if ( w != null )
		{
			NBTTagCompound info = ( NBTTagCompound ) w.getWorldInfo().getAdditionalProperty( "storageCell" );
			if ( info != null )
			{
				return new WorldCoord( info.getInteger( "minX" ), info.getInteger( "minY" ), info.getInteger( "minZ" ) );
			}
		}
		return new WorldCoord( 0, 0, 0 );
	}

	@Override
	public WorldCoord getMax( ItemStack is )
	{
		World w = getWorld( is );
		if ( w != null )
		{
			NBTTagCompound info = ( NBTTagCompound ) w.getWorldInfo().getAdditionalProperty( "storageCell" );
			if ( info != null )
			{
				return new WorldCoord( info.getInteger( "maxX" ), info.getInteger( "maxY" ), info.getInteger( "maxZ" ) );
			}
		}
		return new WorldCoord( 0, 0, 0 );
	}

	@Override
	public TransitionResult doSpatialTransition( ItemStack is, World w, WorldCoord min, WorldCoord max, boolean doTransition )
	{
		WorldCoord scale = getStoredSize( is );

		int targetX = max.x - min.x - 1;
		int targetY = max.y - min.y - 1;
		int targetZ = max.z - min.z - 1;
		int maxSize = getMaxStoredDim( is );

		int floorBuffer = 64;
		World destination = getWorld( is );

		if ( ( scale.x == 0 && scale.y == 0 && scale.z == 0 ) || ( scale.x == targetX && scale.y == targetY && scale.z == targetZ ) )
		{
			if ( targetX <= maxSize && targetY <= maxSize && targetZ <= maxSize )
			{
				if ( destination == null )
					destination = createNewWorld( is );

				StorageHelper.getInstance()
						.swapRegions( w, destination, min.x + 1, min.y + 1, min.z + 1, 1, floorBuffer + 1, 1, targetX - 1, targetY - 1, targetZ - 1 );
				setStoredSize( is, targetX, targetY, targetZ );

				return new TransitionResult( true, 0 );
			}
		}

		return new TransitionResult( false, 0 );
	}

	public World createNewWorld( ItemStack is )
	{
		NBTTagCompound c = Platform.openNbtData( is );
		int newDim = DimensionManager.getNextFreeDimId();
		c.setInteger( "StorageDim", newDim );
		WorldSettings.getInstance().addStorageCellDim( newDim );
		DimensionManager.initDimension( newDim );
		return DimensionManager.getWorld( newDim );
	}

	private void setStoredSize( ItemStack is, int targetX, int targetY, int targetZ )
	{
		if ( is.hasTagCompound() )
		{
			NBTTagCompound c = is.getTagCompound();
			int dim = c.getInteger( "StorageDim" );
			c.setInteger( "sizeX", targetX );
			c.setInteger( "sizeY", targetY );
			c.setInteger( "sizeZ", targetZ );
			WorldSettings.getInstance().setStoredSize( dim, targetX, targetY, targetZ );
		}
	}

}
