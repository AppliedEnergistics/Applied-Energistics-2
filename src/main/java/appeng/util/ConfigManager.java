package appeng.util;

import java.util.HashMap;
import java.util.Set;

import appeng.api.config.LevelEmitterMode;
import appeng.api.config.StorageFilter;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;

public class ConfigManager implements IConfigManager
{

	final HashMap<Enum, Enum> Settings = new HashMap<Enum, Enum>();
	final IConfigManagerHost target;

	public ConfigManager(IConfigManagerHost tile) {
		target = tile;
	}

	/**
	 * read all settings using config manager.
	 * 
	 * @param tagCompound to be read from compound
	 */
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		for (Enum key : Settings.keySet())
		{
			try
			{
				if ( tagCompound.hasKey( key.name() ) )
				{
					String value = tagCompound.getString( key.name() );

					// Provides an upgrade path for the rename of this value in the API between rv1 and rv2
					if( value.equals( "EXTACTABLE_ONLY" ) ){
						value = StorageFilter.EXTRACTABLE_ONLY.toString();
					} else if( value.equals( "STOREABLE_AMOUNT" ) ) {
						value = LevelEmitterMode.STORABLE_AMOUNT.toString();
					}

					Enum oldValue = Settings.get( key );

					Enum newValue = Enum.valueOf( oldValue.getClass(), value );

					putSetting( key, newValue );
				}
			}
			catch (IllegalArgumentException e)
			{
				AELog.error( e );
			}
		}
	}

	/**
	 * save all settings using config manager.
	 * 
	 * @param tagCompound to be written to compound
	 */
	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{

		for (Enum e : Settings.keySet())
		{
			tagCompound.setString( e.name(), Settings.get( e ).toString() );
		}

	}

	@Override
	public Set<Enum> getSettings()
	{
		return Settings.keySet();
	}

	@Override
	public void registerSetting(Enum settingName, Enum defaultValue)
	{
		Settings.put( settingName, defaultValue );
	}

	@Override
	public Enum getSetting(Enum settingName)
	{
		Enum oldValue = Settings.get( settingName );

		if ( oldValue != null )
			return oldValue;

		throw new RuntimeException( "Invalid Config setting" );
	}

	@Override
	public Enum putSetting(Enum settingName, Enum newValue)
	{
		Enum oldValue = getSetting( settingName );
		Settings.put( settingName, newValue );
		target.updateSetting( this, settingName, newValue );
		return oldValue;
	}

}
