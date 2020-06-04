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


import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.storage.ISpatialDimension;
import appeng.api.util.WorldCoord;
import appeng.capabilities.Capabilities;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import net.minecraftforge.common.util.LazyOptional;


public class ItemSpatialStorageCell extends AEBaseItem implements ISpatialStorageCell
{
	private static final String NBT_CELL_ID_KEY = "StorageCellID";
	private static final String NBT_SIZE_X_KEY = "sizeX";
	private static final String NBT_SIZE_Y_KEY = "sizeY";
	private static final String NBT_SIZE_Z_KEY = "sizeZ";

	private final int maxRegion;

	public ItemSpatialStorageCell( Properties props, final int spatialScale )
	{
		super(props);
		// FIXME this.setMaxStackSize( 1 );
		this.maxRegion = spatialScale;
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips )
	{
		final int id = this.getStoredDimensionID( stack );
		if( id >= 0 )
		{
			lines.add( GuiText.CellId.textComponent().appendText( ": " + id ) );
		}

		final WorldCoord wc = this.getStoredSize( stack );
		if( wc.x > 0 )
		{
			lines.add( GuiText.StoredSize.textComponent().appendText( ": " + wc.x + " x " + wc.y + " x " + wc.z ) );
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
	public ISpatialDimension getSpatialDimension()
	{
		World w = null;
// FIXME		final int id = AppEng.instance().getStorageDimensionID();
// FIXME		World w = DimensionManager.getWorld( id );
// FIXME		if( w == null )
// FIXME		{
// FIXME			DimensionManager.initDimension( id );
// FIXME			w = DimensionManager.getWorld( id );
// FIXME		}

		if( w != null )
		{
			LazyOptional<ISpatialDimension> spatialCap = w.getCapability(Capabilities.SPATIAL_DIMENSION, null);
			return spatialCap.orElse(null);
		}
		return null;
	}

	@Override
	public WorldCoord getStoredSize( final ItemStack is )
	{
		if( is.hasTag() )
		{
			final CompoundNBT c = is.getTag();
			return new WorldCoord( c.getInt( NBT_SIZE_X_KEY ), c.getInt( NBT_SIZE_Y_KEY ), c.getInt( NBT_SIZE_Z_KEY ) );
		}
		return new WorldCoord( 0, 0, 0 );
	}

	@Override
	public int getStoredDimensionID( final ItemStack is )
	{
		if( is.hasTag() )
		{
			final CompoundNBT c = is.getTag();
			return c.getInt( NBT_CELL_ID_KEY );
		}
		return -1;
	}

	@Override
	public TransitionResult doSpatialTransition( final ItemStack is, final World w, final WorldCoord min, final WorldCoord max, int playerId )
	{
		final int targetX = max.x - min.x - 1;
		final int targetY = max.y - min.y - 1;
		final int targetZ = max.z - min.z - 1;
		final int maxSize = this.getMaxStoredDim( is );

		final BlockPos targetSize = new BlockPos( targetX, targetY, targetZ );

		ISpatialDimension manager = this.getSpatialDimension();

		int cellid = this.getStoredDimensionID( is );
		if( cellid < 0 )
		{
			cellid = manager.createNewCellDimension( targetSize, playerId );
		}

		try
		{
			if( manager.isCellDimension( cellid ) )
			{
				BlockPos scale = manager.getCellContentSize( cellid );

				if( scale.equals( targetSize ) )
				{
					if( targetX <= maxSize && targetY <= maxSize && targetZ <= maxSize )
					{
						BlockPos offset = manager.getCellDimensionOrigin( cellid );

						this.setStorageCell( is, cellid, targetSize );
						// FIXME StorageHelper.getInstance()
						// FIXME 		.swapRegions( w, min.x + 1, min.y + 1, min.z + 1, manager.getWorld(), offset.getX(), offset.getY(),
						// FIXME 				offset.getZ(), targetX - 1, targetY - 1,
						// FIXME 				targetZ - 1 );

						return new TransitionResult( true, 0 );
					}
				}
			}
			return new TransitionResult( false, 0 );
		}
		finally
		{
			// clean up newly created dimensions that failed transfer
			if( manager.isCellDimension( cellid ) && this.getStoredDimensionID( is ) < 0 )
			{
				manager.deleteCellDimension( cellid );
			}
		}
	}

	private void setStorageCell( final ItemStack is, int id, BlockPos size )
	{
        final CompoundNBT c = is.getOrCreateTag();

		c.putInt( NBT_CELL_ID_KEY, id );
		c.putInt( NBT_SIZE_X_KEY, size.getX() );
		c.putInt( NBT_SIZE_Y_KEY, size.getY() );
		c.putInt( NBT_SIZE_Z_KEY, size.getZ() );
	}
}
