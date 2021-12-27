package appeng.api.util;

import appeng.api.config.Setting;

/**
 * Thrown if {@link appeng.util.ConfigManager} is used with a {@link appeng.api.config.Setting} that was not previously
 * {@link appeng.util.ConfigManager#registerSetting(Setting, Enum) registered}.
 */
public class UnsupportedSettingException extends RuntimeException {
    public UnsupportedSettingException(String message) {
        super(message);
    }
}
