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

package appeng.util;


import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.LevelEmitterMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;


public final class ConfigManager implements IConfigManager
{
	private final Map<Settings, Enum<?>> settings = new EnumMap<>( Settings.class );
	private final IConfigManagerHost target;
	private Map<Settings, Enum<?>> oldSettings = new EnumMap<>( Settings.class );

	public ConfigManager( final IConfigManagerHost tile )
	{
		this.target = tile;
	}

	@Override
	public Set<Settings> getSettings()
	{
		return this.settings.keySet();
	}

	@Override
	public void registerSetting( final Settings settingName, final Enum defaultValue )
	{
		this.settings.put( settingName, defaultValue );
	}

	@Override
	public Enum<?> getSetting( final Settings settingName )
	{
		final Enum<?> oldValue = this.settings.get( settingName );

		if( oldValue != null )
		{
			return oldValue;
		}

		throw new IllegalStateException( "Invalid Config setting. Expected a non-null value for " + settingName );
	}

	@Override
	public Enum<?> putSetting( final Settings settingName, final Enum newValue )
	{
		final Enum<?> oldValue = this.getSetting( settingName );
		this.settings.put( settingName, newValue );
		this.oldSettings.put( settingName, oldValue );
		this.target.updateSetting( this, settingName, newValue );
		return oldValue;
	}

	public Enum<?> getOldSetting(final Settings settingName){
		return this.oldSettings.get( settingName );
	}

	/**
	 * save all settings using config manager.
	 *
	 * @param tagCompound to be written to compound
	 */
	@Override
	public void writeToNBT( final NBTTagCompound tagCompound )
	{
		for( final Map.Entry<Settings, Enum<?>> entry : this.settings.entrySet() )
		{
			tagCompound.setString( entry.getKey().name(), this.settings.get( entry.getKey() ).toString() );
		}
	}

	/**
	 * read all settings using config manager.
	 *
	 * @param tagCompound to be read from compound
	 */
	@Override
	public void readFromNBT( final NBTTagCompound tagCompound )
	{
		for( final Map.Entry<Settings, Enum<?>> entry : this.settings.entrySet() )
		{
			try
			{
				if( tagCompound.hasKey( entry.getKey().name() ) )
				{
					String value = tagCompound.getString( entry.getKey().name() );

					// Provides an upgrade path for the rename of this value in the API between rv1 and rv2
					if( value.equals( "EXTACTABLE_ONLY" ) )
					{
						value = StorageFilter.EXTRACTABLE_ONLY.toString();
					}
					else if( value.equals( "STOREABLE_AMOUNT" ) )
					{
						value = LevelEmitterMode.STORABLE_AMOUNT.toString();
					}

					final Enum<?> oldValue = this.settings.get( entry.getKey() );

					final Enum<?> newValue = Enum.valueOf( oldValue.getClass(), value );

					this.putSetting( entry.getKey(), newValue );
				}
			}
			catch( final IllegalArgumentException e )
			{
				AELog.debug( e );
			}
		}
	}
}
