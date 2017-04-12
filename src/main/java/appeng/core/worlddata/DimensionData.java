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

package appeng.core.worlddata;


import appeng.api.util.WorldCoord;
import appeng.core.AEConfig;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNewStorageDimension;
import appeng.hooks.TickHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


/**
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
final class DimensionData implements IWorldDimensionData, IOnWorldStartable, IOnWorldStoppable
{
	private static final String CONFIG_CATEGORY = "DimensionManager";
	private static final String CONFIG_KEY = "StorageCells";
	private static final int[] STORAGE_CELLS_DEFAULT = new int[0];

	private static final String STORAGE_CELL_CATEGORY = "StorageCell";

	private static final String STORAGE_CELL_SCALE_X_KEY = "scaleX";
	private static final String STORAGE_CELL_SCALE_Y_KEY = "scaleY";
	private static final String STORAGE_CELL_SCALE_Z_KEY = "scaleZ";

	private static final String PACKAGE_DEST_CATEGORY = "DimensionManager";
	private static final String PACKAGE_KEY_CATEGORY = "StorageCells";
	private static final int[] PACKAGE_DEF_CATEGORY = new int[0];

	private final Configuration config;
	private final List<Integer> storageCellDimensionIDs;

	DimensionData( @Nonnull final Configuration parentFile )
	{
		Preconditions.checkNotNull( parentFile );

		this.config = parentFile;

		final int[] storageCellIDs = this.storageCellIDsProperty().getIntList();

		this.storageCellDimensionIDs = Lists.newArrayList();
		for( final int storageCellID : storageCellIDs )
		{
			this.storageCellDimensionIDs.add( storageCellID );
		}
	}

	private Property storageCellIDsProperty()
	{
		return this.config.get( CONFIG_CATEGORY, CONFIG_KEY, STORAGE_CELLS_DEFAULT );
	}

	@Override
	public void onWorldStart()
	{
		for( final Integer storageCellDimID : this.storageCellDimensionIDs )
		{
			DimensionManager.registerDimension( storageCellDimID, AEConfig.instance.storageProviderID );
		}

		this.config.save();
	}

	@Override
	public void onWorldStop()
	{
		this.config.save();

		for( final Integer storageCellDimID : this.storageCellDimensionIDs )
		{
			DimensionManager.unregisterDimension( storageCellDimID );
		}

		this.storageCellDimensionIDs.clear();
	}

	@Override
	public void addStorageCell( final int newStorageCellID )
	{
		this.storageCellDimensionIDs.add( newStorageCellID );
		DimensionManager.registerDimension( newStorageCellID, AEConfig.instance.storageProviderID );

		NetworkHandler.instance.sendToAll( new PacketNewStorageDimension( newStorageCellID ) );

		final String[] values = new String[this.storageCellDimensionIDs.size()];

		for( int x = 0; x < values.length; x++ )
		{
			values[x] = String.valueOf( this.storageCellDimensionIDs.get( x ) );
		}

		this.storageCellIDsProperty().set( values );
		this.config.save();
	}

	@Override
	public WorldCoord getStoredSize( final int dim )
	{
		final String category = STORAGE_CELL_CATEGORY + dim;

		final int x = this.config.get( category, STORAGE_CELL_SCALE_X_KEY, 0 ).getInt();
		final int y = this.config.get( category, STORAGE_CELL_SCALE_Y_KEY, 0 ).getInt();
		final int z = this.config.get( category, STORAGE_CELL_SCALE_Z_KEY, 0 ).getInt();

		return new WorldCoord( x, y, z );
	}

	@Override
	public void setStoredSize( final int dim, final int targetX, final int targetY, final int targetZ )
	{
		final String category = STORAGE_CELL_CATEGORY + dim;

		this.config.get( category, STORAGE_CELL_SCALE_X_KEY, 0 ).set( targetX );
		this.config.get( category, STORAGE_CELL_SCALE_Y_KEY, 0 ).set( targetY );
		this.config.get( category, STORAGE_CELL_SCALE_Z_KEY, 0 ).set( targetZ );

		this.config.save();
	}

	@Override
	public void sendToPlayer( @Nullable final NetworkManager manager )
	{
		if( manager != null )
		{
			for( final int newDim : this.config.get( PACKAGE_DEST_CATEGORY, PACKAGE_KEY_CATEGORY, PACKAGE_DEF_CATEGORY ).getIntList() )
			{
				manager.scheduleOutboundPacket( ( new PacketNewStorageDimension( newDim ) ).getProxy() );
			}
		}
		else
		{
			for( final TickHandler.PlayerColor pc : TickHandler.INSTANCE.getPlayerColors().values() )
			{
				NetworkHandler.instance.sendToAll( pc.getPacket() );
			}
		}
	}
}
