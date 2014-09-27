package appeng.util;

import java.util.HashMap;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;

public class ConfigManager implements IConfigManager
{

	HashMap<Enum, Enum> Settings = new HashMap();
	IConfigManagerHost target;

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
