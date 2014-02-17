package appeng.util;

import appeng.api.util.IConfigManager;

public interface IConfigManagerHost
{

	void updateSetting(IConfigManager manager, Enum settingName, Enum newValue);

}
