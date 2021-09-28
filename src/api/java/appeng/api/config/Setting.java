/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

package appeng.api.config;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import appeng.api.util.IConfigManager;

public final class Setting<T extends Enum<T>> {

    private final String name;
    private final Class<T> enumClass;
    private final ImmutableSet<T> values;

    public Setting(String name, Class<T> enumClass) {
        this(name, enumClass, EnumSet.allOf(enumClass));
    }

    public Setting(String name, Class<T> enumClass, EnumSet<T> values) {
        this.name = name;
        this.enumClass = enumClass;
        this.values = ImmutableSet.copyOf(values);
    }

    public String getName() {
        return name;
    }

    public Set<T> getValues() {
        return values;
    }

    public T getValue(IConfigManager configManager) {
        return enumClass.cast(configManager.getSetting(this));
    }

    public Class<T> getEnumClass() {
        return enumClass;
    }

    public void setFromString(IConfigManager cm, String value) {
        for (T allowedValue : values) {
            if (allowedValue.name().equals(value)) {
                cm.putSetting(this, allowedValue);
                return;
            }
        }

        throw new IllegalArgumentException("Received invalid value '" + value + "' for setting '" + name + "'");
    }

    public void copy(IConfigManager from, IConfigManager to) {
        to.putSetting(this, from.getSetting(this));
    }

}
