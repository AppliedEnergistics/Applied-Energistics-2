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
