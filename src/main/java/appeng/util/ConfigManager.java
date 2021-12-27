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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;
import appeng.api.util.UnsupportedSettingException;
import appeng.core.AELog;

public final class ConfigManager implements IConfigManager {
    private final Map<Setting<?>, Enum<?>> settings = new IdentityHashMap<>();
    @Nullable
    private final IConfigManagerListener listener;

    public ConfigManager(IConfigManagerListener listener) {
        this.listener = listener;
    }

    public ConfigManager() {
        this.listener = null;
    }

    @Override
    public Set<Setting<?>> getSettings() {
        return this.settings.keySet();
    }

    @Override
    public <T extends Enum<T>> void registerSetting(Setting<T> setting, T defaultValue) {
        this.settings.put(setting, defaultValue);
    }

    @Override
    public <T extends Enum<T>> T getSetting(Setting<T> setting) {
        var oldValue = this.settings.get(setting);

        if (oldValue == null) {
            throw new UnsupportedSettingException("Setting " + setting.getName() + " is not supported.");
        }

        return setting.getEnumClass().cast(oldValue);
    }

    @Override
    public <T extends Enum<T>> void putSetting(Setting<T> setting, T newValue) {
        if (!settings.containsKey(setting)) {
            throw new UnsupportedSettingException("Setting " + setting.getName() + " is not supported.");
        }
        this.settings.put(setting, newValue);
        if (this.listener != null) {
            this.listener.onSettingChanged(this, setting);
        }
    }

    /**
     * save all settings using config manager.
     *
     * @param tagCompound to be written to compound
     */
    @Override
    public void writeToNBT(CompoundTag tagCompound) {
        for (var entry : this.settings.entrySet()) {
            tagCompound.putString(entry.getKey().getName(), this.settings.get(entry.getKey()).toString());
        }
    }

    /**
     * read all settings using config manager.
     *
     * @param tagCompound to be read from compound
     */
    @Override
    public void readFromNBT(CompoundTag tagCompound) {
        for (var setting : this.settings.keySet()) {
            try {
                if (tagCompound.contains(setting.getName(), Tag.TAG_STRING)) {
                    String value = tagCompound.getString(setting.getName());
                    setting.setFromString(this, value);
                }
            } catch (IllegalArgumentException e) {
                AELog.debug(e);
            }
        }
    }
}
