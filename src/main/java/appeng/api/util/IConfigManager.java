/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.util;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import appeng.api.config.Setting;
import appeng.api.ids.AEComponents;
import appeng.util.ConfigManager;

/**
 * Used to adjust settings on an object,
 * <p>
 * Obtained via {@link IConfigurableObject}
 */
public interface IConfigManager {
    /**
     * get a list of different settings
     *
     * @return enum set of settings
     */
    Set<Setting<?>> getSettings();

    /**
     * Checks if this config manager supports the given setting.
     */
    default boolean hasSetting(Setting<?> setting) {
        return getSettings().contains(setting);
    }

    /**
     * Get Value of a particular setting
     *
     * @param setting the setting
     * @return value of setting
     * @throws UnsupportedSettingException if setting has not been registered before
     */
    <T extends Enum<T>> T getSetting(Setting<T> setting);

    /**
     * Change setting
     *
     * @param setting  to be changed setting
     * @param newValue new value for setting
     * @throws UnsupportedSettingException if setting has not been registered before
     */
    <T extends Enum<T>> void putSetting(Setting<T> setting, T newValue);

    /**
     * write all settings to the NBT Tag so they can be read later.
     *
     * @param output to be written nbt tag
     */
    void writeToNBT(ValueOutput output);

    /**
     * Only works after settings have been registered
     *
     * @param input to be read nbt tag
     * @return true if any configuration was loaded from src
     */
    boolean readFromNBT(ValueInput input);

    /**
     * Import settings that were previously exported from {@link #exportSettings()}. Unparsable or unknown settings are
     * ignored.
     *
     * @return true if any of the settings were successfully imported
     */
    boolean importSettings(Map<String, String> settings);

    /**
     * Exports all settings.
     */
    Map<String, String> exportSettings();

    /**
     * Get a builder for configuration manager that stores its settings in a block entity.
     */
    static IConfigManagerBuilder builder(ItemStack stack) {
        return builder(() -> stack);
    }

    /**
     * Get a builder for configuration manager that stores its settings in a block entity.
     */
    static IConfigManagerBuilder builder(Supplier<ItemStack> stack) {
        var manager = new ConfigManager((mgr, settingName) -> {
            stack.get().set(AEComponents.EXPORTED_SETTINGS, mgr.exportSettings());
        });

        return new IConfigManagerBuilder() {
            @Override
            public <T extends Enum<T>> IConfigManagerBuilder registerSetting(Setting<T> setting, T defaultValue) {
                manager.registerSetting(setting, defaultValue);
                return this;
            }

            @Override
            public IConfigManager build() {
                manager.importSettings(stack.get().getOrDefault(AEComponents.EXPORTED_SETTINGS, Map.of()));
                return manager;
            }
        };
    }

    static IConfigManagerBuilder builder(Runnable changeListener) {
        return builder((manager, setting) -> changeListener.run());
    }

    static IConfigManagerBuilder builder(IConfigManagerListener changeListener) {
        var manager = new ConfigManager(changeListener);
        return new IConfigManagerBuilder() {
            @Override
            public <T extends Enum<T>> IConfigManagerBuilder registerSetting(Setting<T> setting, T defaultValue) {
                manager.registerSetting(setting, defaultValue);
                return this;
            }

            @Override
            public IConfigManager build() {
                return manager;
            }
        };
    }
}
