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

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;

public final class ConfigManager implements IConfigManager {
    private final Map<Setting<?>, Enum<?>> settings = new IdentityHashMap<>();
    @Nullable
    private final IConfigManagerListener listener;

    public ConfigManager(IConfigManagerListener blockEntity) {
        this.listener = blockEntity;
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
        final Enum<?> oldValue = this.settings.get(setting);

        if (oldValue != null) {
            return setting.getEnumClass().cast(oldValue);
        }

        throw new IllegalStateException("Invalid Config setting. Expected a non-null value for " + setting.getName());
    }

    @Override
    public <T extends Enum<T>> void putSetting(Setting<T> setting, T newValue) {
        Preconditions.checkState(settings.containsKey(setting), "Setting %s not supported", setting);
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
    public void writeToNBT(final CompoundTag tagCompound) {
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
    public void readFromNBT(final CompoundTag tagCompound) {
        for (var entry : this.settings.entrySet()) {
            try {
                if (tagCompound.contains(entry.getKey().getName(), Tag.TAG_STRING)) {
                    String value = tagCompound.getString(entry.getKey().getName());
                    entry.getKey().setFromString(this, value);
                }
            } catch (final IllegalArgumentException e) {
                AELog.debug(e);
            }
        }
    }
}
