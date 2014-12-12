package appeng.api.util;

/**
 * Implemented by various Tiles or Parts in AE
 */
public interface IConfigurableObject
{

	/**
	 * get the config manager for the object.
	 */
	IConfigManager getConfigManager();

}
