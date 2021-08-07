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

package appeng.menu.guisync;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

/**
 * This class is responsible for synchronizing menu-fields from server to client.
 */
public abstract class SynchronizedField<T> {

    private final Object source;
    protected final MethodHandle getter;
    protected final MethodHandle setter;
    protected T clientVersion;

    private SynchronizedField(Object source, Field field) {
        this.clientVersion = null;
        this.source = source;
        field.setAccessible(true);
        try {
            this.getter = MethodHandles.publicLookup().unreflectGetter(field);
            this.setter = MethodHandles.publicLookup().unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Failed to get accessor for field " + field + ". Did you forget to make it public?");
        }
    }

    @SuppressWarnings("unchecked")
    private T getCurrentValue() {
        try {
            return (T) this.getter.invoke(source);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public boolean hasChanges() {
        return !Objects.equals(getCurrentValue(), this.clientVersion);
    }

    public final void write(FriendlyByteBuf data) {
        T currentValue = getCurrentValue();
        this.clientVersion = currentValue;
        this.writeValue(data, currentValue);
    }

    public final void read(FriendlyByteBuf data) {
        T value = readValue(data);
        try {
            setter.invoke(source, value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    protected abstract void writeValue(FriendlyByteBuf data, T value);

    protected abstract T readValue(FriendlyByteBuf data);

    public static SynchronizedField<?> create(Object source, Field field) {
        Class<?> fieldType = field.getType();

        if (fieldType.isAssignableFrom(Component.class)) {
            return new TextComponentField(source, field);
        } else if (fieldType == String.class) {
            return new StringField(source, field);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            return new IntegerField(source, field);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return new LongField(source, field);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return new BooleanField(source, field);
        } else if (fieldType.isEnum()) {
            return createEnumField(source, field, fieldType.asSubclass(Enum.class));
        } else {
            throw new IllegalArgumentException("Cannot synchronize field " + field);
        }
    }

    private static <T extends Enum<T>> EnumField<T> createEnumField(Object source, Field field, Class<T> fieldType) {
        return new EnumField<>(source, field, fieldType.getEnumConstants());
    }

    private static class StringField extends SynchronizedField<String> {
        private StringField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(FriendlyByteBuf data, String value) {
            data.writeUtf(value);
        }

        @Override
        protected String readValue(FriendlyByteBuf data) {
            return data.readUtf();
        }
    }

    private static class IntegerField extends SynchronizedField<Integer> {
        private IntegerField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(FriendlyByteBuf data, Integer value) {
            data.writeInt(value);
        }

        @Override
        protected Integer readValue(FriendlyByteBuf data) {
            return data.readInt();
        }
    }

    private static class LongField extends SynchronizedField<Long> {
        private LongField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(FriendlyByteBuf data, Long value) {
            data.writeLong(value);
        }

        @Override
        protected Long readValue(FriendlyByteBuf data) {
            return data.readLong();
        }
    }

    private static class BooleanField extends SynchronizedField<Boolean> {
        private BooleanField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(FriendlyByteBuf data, Boolean value) {
            data.writeBoolean(value);
        }

        @Override
        protected Boolean readValue(FriendlyByteBuf data) {
            return data.readBoolean();
        }
    }

    private static class EnumField<T extends Enum<T>> extends SynchronizedField<T> {
        private final T[] values;

        private EnumField(Object source, Field field, T[] values) {
            super(source, field);
            this.values = values;
        }

        @Override
        protected void writeValue(FriendlyByteBuf data, T value) {
            data.writeVarInt(value.ordinal());
        }

        @Override
        protected T readValue(FriendlyByteBuf data) {
            int ordinal = data.readVarInt();
            return values[ordinal];
        }
    }

    private static class TextComponentField extends SynchronizedField<Component> {
        private TextComponentField(Object source, Field field) {
            super(source, field);
        }

        @Override
        protected void writeValue(FriendlyByteBuf data, Component value) {
            if (value == null) {
                data.writeBoolean(false);
            } else {
                data.writeBoolean(true);
                data.writeComponent(value);
            }
        }

        @Override
        protected Component readValue(FriendlyByteBuf data) {
            if (data.readBoolean()) {
                return data.readComponent();
            } else {
                return null;
            }
        }
    }

}
