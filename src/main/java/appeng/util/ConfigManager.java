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
	private final Map<Settings, Enum<?>> settings = new EnumMap<Settings, Enum<?>>( Settings.class );
	private final IConfigManagerHost target;

	public ConfigManager( IConfigManagerHost tile )
	{
		this.target = tile;
	}

	@Override
	public Set<Settings> getSettings()
	{
		return this.settings.keySet();
	}

	@Override
	public void registerSetting( Settings settingName, Enum defaultValue )
	{
		this.settings.put( settingName, defaultValue );
	}

	@Override
	public Enum<?> getSetting( Settings settingName )
	{
		Enum<?> oldValue = this.settings.get( settingName );

		if ( oldValue != null )
			return oldValue;

		throw new RuntimeException( "Invalid Config setting" );
	}

	@Override
	public Enum<?> putSetting( Settings settingName, Enum newValue )
	{
		Enum<?> oldValue = this.getSetting( settingName );
		this.settings.put( settingName, newValue );
		this.target.updateSetting( this, settingName, newValue );
		return oldValue;
	}

	/**
	 * save all settings using config manager.
	 *
	 * @param tagCompound to be written to compound
	 */
	@Override
	public void writeToNBT( NBTTagCompound tagCompound )
	{

		for ( Settings setting : this.settings.keySet() )
		{
			tagCompound.setString( setting.name(), this.settings.get( setting ).toString() );
		}
	}

	/**
	 * read all settings using config manager.
	 *
	 * @param tagCompound to be read from compound
	 */
	@Override
	public void readFromNBT( NBTTagCompound tagCompound )
	{
		for ( Settings key : this.settings.keySet() )
		{
			try
			{
				if ( tagCompound.hasKey( key.name() ) )
				{
					String value = tagCompound.getString( key.name() );

					// Provides an upgrade path for the rename of this value in the API between rv1 and rv2
					if ( value.equals( "EXTACTABLE_ONLY" ) )
					{
						value = StorageFilter.EXTRACTABLE_ONLY.toString();
					}
					else if ( value.equals( "STOREABLE_AMOUNT" ) )
					{
						value = LevelEmitterMode.STORABLE_AMOUNT.toString();
					}

					Enum<?> oldValue = this.settings.get( key );

					Enum<?> newValue = Enum.valueOf( oldValue.getClass(), value );

					this.putSetting( key, newValue );
				}
			}
			catch ( IllegalArgumentException e )
			{
				AELog.error( e );
			}
		}
	}
}
