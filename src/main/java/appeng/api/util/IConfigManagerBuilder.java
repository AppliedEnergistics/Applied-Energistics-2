package appeng.api.util;

import appeng.api.config.Setting;

public interface IConfigManagerBuilder {
    /**
     * used to initialize the configuration manager, should be called for all settings.
     *
     * @param setting      the setting
     * @param defaultValue default value of setting
     */
    <T extends Enum<T>> IConfigManagerBuilder registerSetting(Setting<T> setting, T defaultValue);

    IConfigManager build();
}
