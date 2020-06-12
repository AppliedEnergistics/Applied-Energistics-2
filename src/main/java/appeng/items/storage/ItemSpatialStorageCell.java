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

import appeng.core.AELog;
import appeng.core.worlddata.SpatialDimensionManager;
import appeng.spatial.StorageHelper;
import javafx.animation.Transition;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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
		this.maxRegion = spatialScale;
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines, final ITooltipFlag advancedTooltips )
	{
		final DimensionType dimType = this.getStoredDimension( stack );
		if( dimType != null )
		{
			lines.add( GuiText.CellId.textComponent().appendText( ": " + dimType.getRegistryName() ) );
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
	public WorldCoord getStoredSize( final ItemStack is )
	{
		final CompoundNBT c = is.getTag();
		if( c != null )
		{
			return new WorldCoord( c.getInt( NBT_SIZE_X_KEY ), c.getInt( NBT_SIZE_Y_KEY ), c.getInt( NBT_SIZE_Z_KEY ) );
		}
		return new WorldCoord( 0, 0, 0 );
	}

	@Override
	public DimensionType getStoredDimension(final ItemStack is )
	{
		final CompoundNBT c = is.getTag();
		if( c != null && c.contains(NBT_CELL_ID_KEY) )
		{
			try {
				ResourceLocation dimTypeId = new ResourceLocation(c.getString( NBT_CELL_ID_KEY) );
				return DimensionType.byName(dimTypeId);
			} catch (Exception e) {
				AELog.warn("Failed to retrieve storage cell dimension.", e);
			}
		}
		return null;
	}

	@Override
	public TransitionResult doSpatialTransition( final ItemStack is, final World w, final WorldCoord min, final WorldCoord max, int playerId )
	{
		final int targetX = max.x - min.x - 1;
		final int targetY = max.y - min.y - 1;
		final int targetZ = max.z - min.z - 1;
		final int maxSize = this.getMaxStoredDim( is );

		final BlockPos targetSize = new BlockPos( targetX, targetY, targetZ );

		ISpatialDimension manager = new SpatialDimensionManager();

		DimensionType storedDim = this.getStoredDimension( is );
		if( storedDim == null )
		{
			storedDim = manager.createNewCellDimension( targetSize, playerId );
		}

		if (storedDim == null) {
			// Failed to create the dimension
			return new TransitionResult(false, 0);
		}

		try
		{
			if(  manager.isCellDimension( storedDim ) )
			{
				World cellWorld = manager.getWorld(storedDim);

				BlockPos scale = manager.getCellContentSize( storedDim );

				if( scale.equals( targetSize ) )
				{
					if( targetX <= maxSize && targetY <= maxSize && targetZ <= maxSize )
					{
						BlockPos offset = manager.getCellDimensionOrigin( storedDim );

						this.setStorageCell( is, storedDim, targetSize );
						StorageHelper.getInstance()
								.swapRegions( w, min.x + 1, min.y + 1, min.z + 1, cellWorld, offset.getX(), offset.getY(),
										offset.getZ(), targetX - 1, targetY - 1,
										targetZ - 1 );

						return new TransitionResult( true, 0 );
					}
				}
			}
			return new TransitionResult( false, 0 );
		}
		finally
		{
			// clean up newly created dimensions that failed transfer
			if( manager.isCellDimension( storedDim ) && this.getStoredDimension( is ) == null )
			{
				manager.deleteCellDimension( storedDim );
			}
		}
	}

	private void setStorageCell( final ItemStack is, DimensionType dim, BlockPos size )
	{
        final CompoundNBT c = is.getOrCreateTag();

		c.putString( NBT_CELL_ID_KEY, dim.getRegistryName().toString() );
		c.putInt( NBT_SIZE_X_KEY, size.getX() );
		c.putInt( NBT_SIZE_Y_KEY, size.getY() );
		c.putInt( NBT_SIZE_Z_KEY, size.getZ() );
	}
}
