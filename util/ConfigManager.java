package appeng.util;

import java.util.HashMap;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.util.IConfigManager;

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
	 * @param tagCompound
	 */
	@SuppressWarnings("static-access")
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		for (Enum key : Settings.keySet())
		{
			String value = tagCompound.getString( key.name() );

			Enum oldValue = Settings.get( key );
			Enum newValue = oldValue.valueOf( oldValue.getClass(), value );

			putSetting( key, newValue );
		}
	}

	/**
	 * save all settings using config manager.
	 * 
	 * @param tagCompound
	 */
	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{

		for (Enum e : Settings.keySet())
		{
			tagCompound.setString( e.name(), e.name() );
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
		target.updateSetting( settingName, newValue );
		return oldValue;
	}

}
