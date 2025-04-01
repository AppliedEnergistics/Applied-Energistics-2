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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import appeng.api.stacks.GenericStack;
import appeng.util.AECodecs;

/**
 * This class is responsible for synchronizing menu-fields from server to client.
 */
public class SynchronizedField<T> {
    private static final Map<Class<?>, StreamCodec<? super RegistryFriendlyByteBuf, ?>> CODECS = new HashMap<>();
    private static final Map<Class<? extends PacketWritable>, StreamCodec<? super RegistryFriendlyByteBuf, ? extends PacketWritable>> PACKET_WRITABLE_CODECS = new HashMap<>();

    static {
        CODECS.put(String.class, ByteBufCodecs.STRING_UTF8.apply(AECodecs::nullable));
        CODECS.put(Component.class, ComponentSerialization.TRUSTED_STREAM_CODEC.apply(AECodecs::nullable));
        CODECS.put(GenericStack.class, GenericStack.STREAM_CODEC.apply(AECodecs::nullable));
        CODECS.put(ResourceLocation.class, ResourceLocation.STREAM_CODEC.apply(AECodecs::nullable));
        CODECS.put(int.class, ByteBufCodecs.INT);
        CODECS.put(Integer.class, ByteBufCodecs.INT.apply(AECodecs::nullable));
        CODECS.put(long.class, ByteBufCodecs.LONG);
        CODECS.put(Long.class, ByteBufCodecs.LONG.apply(AECodecs::nullable));
        CODECS.put(double.class, ByteBufCodecs.DOUBLE);
        CODECS.put(Double.class, ByteBufCodecs.DOUBLE.apply(AECodecs::nullable));
        CODECS.put(float.class, ByteBufCodecs.FLOAT);
        CODECS.put(Float.class, ByteBufCodecs.FLOAT.apply(AECodecs::nullable));
        CODECS.put(boolean.class, ByteBufCodecs.BOOL);
        CODECS.put(Boolean.class, ByteBufCodecs.BOOL.apply(AECodecs::nullable));
    }

    private final Object source;
    protected final MethodHandle getter;
    protected final MethodHandle setter;
    protected T clientVersion;
    private final StreamCodec<? super RegistryFriendlyByteBuf, T> codec;

    private SynchronizedField(Object source, Field field, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        this.codec = codec;
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

    public final void write(RegistryFriendlyByteBuf data) {
        T currentValue = getCurrentValue();
        this.clientVersion = currentValue;
        codec.encode(data, currentValue);
    }

    public final void read(RegistryFriendlyByteBuf data) {
        T value = codec.decode(data);
        try {
            setter.invoke(source, value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public StreamCodec<? super RegistryFriendlyByteBuf, T> codec() {
        return codec;
    }

    public static SynchronizedField<?> create(Object source, Field field) {
        Class<?> fieldType = field.getType();

        var codec = CODECS.get(fieldType);
        if (codec == null) {
            if (PacketWritable.class.isAssignableFrom(fieldType)) {
                if (!fieldType.isRecord()) {
                    throw new RuntimeException("Use records to synchronize custom class on " + field
                            + " to enable easier equals comparisons");
                }

                codec = PACKET_WRITABLE_CODECS.computeIfAbsent(fieldType.asSubclass(PacketWritable.class),
                        PacketWritable::streamCodec);
            } else if (fieldType.isEnum()) {
                codec = NeoForgeStreamCodecs.enumCodec(fieldType.asSubclass(Enum.class));
            } else {
                throw new IllegalArgumentException("Cannot synchronize field " + field);
            }
        }

        return new SynchronizedField<>(source, field, codec);
    }
}
