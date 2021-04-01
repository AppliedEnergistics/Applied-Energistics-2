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

package appeng.container.guisync;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.util.text.ITextComponent;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.ProgressBarPacket;

/**
 * This class is responsible for synchronizing Container-fields from server to client.
 */
public class SyncData {

    private final AEBaseContainer source;
    private final Field field;
    private final Class<?> fieldType;
    private final int channel;
    private final MethodHandle getter;
    private final MethodHandle setter;
    private Object clientVersion;

    public SyncData(final AEBaseContainer container, final Field field, final GuiSync annotation) {
        this.clientVersion = null;
        this.source = container;
        this.channel = annotation.value();
        this.field = field;
        this.fieldType = field.getType();
        try {
            this.getter = MethodHandles.publicLookup().unreflectGetter(field);
            this.setter = MethodHandles.publicLookup().unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Failed to get accessor for field " + field + ". Did you forget to make it public?");
        }
    }

    public int getChannel() {
        return this.channel;
    }

    public void tick(final IContainerListener c) {

        try {
            final Object val = this.getter.invoke(source);
            if (!Objects.equals(val, this.clientVersion)) {
                this.send(c, val);
            }
        } catch (Throwable e) {
            AELog.debug(e);
        }

    }

    private void send(final IContainerListener o, Object val) {
        if (fieldType.isAssignableFrom(ITextComponent.class)) {
            if (o instanceof ServerPlayerEntity) {
                String json = "";
                if (val != null) {
                    json = ITextComponent.Serializer.toJson((ITextComponent) val);
                }
                NetworkHandler.instance().sendTo(new ConfigValuePacket("SyncDat." + this.channel, json),
                        (ServerPlayerEntity) o);
            }
            this.clientVersion = val;
            return;
        }

        // Types other than Text must be non-null
        if (val == null) {
            return;
        }

        if (fieldType.equals(String.class)) {
            if (o instanceof ServerPlayerEntity) {
                NetworkHandler.instance().sendTo(new ConfigValuePacket("SyncDat." + this.channel, (String) val),
                        (ServerPlayerEntity) o);
            }
        } else if (this.fieldType.isEnum()) {
            o.sendWindowProperty(this.source, this.channel, ((Enum<?>) val).ordinal());
        } else if (val instanceof Long) {
            if (o instanceof ServerPlayerEntity) {
                NetworkHandler.instance().sendTo(new ProgressBarPacket(this.channel, (Long) val),
                        (ServerPlayerEntity) o);
            }
        } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
            o.sendWindowProperty(this.source, this.channel, ((Boolean) val) ? 1 : 0);
        } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
            o.sendWindowProperty(this.source, this.channel, (Integer) val);
        } else {
            throw new IllegalStateException("Unknown field type: " + fieldType);
        }

        this.clientVersion = val;
    }

    public void update(Object val) {
        try {
            final Object oldValue = this.getter.invoke(source);
            if (val instanceof String) {
                if (this.fieldType.isAssignableFrom(ITextComponent.class)) {
                    String json = (String) val;
                    ITextComponent text = null;
                    if (!json.isEmpty()) {
                        text = ITextComponent.Serializer.getComponentFromJson((String) val);
                    }
                    this.updateTextComponent(text);
                } else {
                    this.updateString((String) val);
                }
            } else {
                this.updateValue(oldValue, (Long) val);
            }
        } catch (Throwable e) {
            AELog.debug(e);
        }
    }

    private void updateString(final String val) {
        try {
            this.setter.invoke(source, val);
        } catch (Throwable e) {
            AELog.debug(e);
        }
    }

    private void updateTextComponent(final ITextComponent val) {
        try {
            this.setter.invoke(source, val);
        } catch (Throwable e) {
            AELog.debug(e);
        }
    }

    private void updateValue(final Object oldValue, final long val) {
        try {
            if (this.fieldType.isEnum()) {
                Object e = this.fieldType.getEnumConstants()[(int) val];
                this.setter.invoke(source, e);
            } else {
                if (this.fieldType.equals(int.class)) {
                    this.setter.invoke(source, (int) val);
                } else if (this.fieldType.equals(long.class)) {
                    this.setter.invoke(source, val);
                } else if (this.fieldType.equals(boolean.class)) {
                    this.setter.invoke(source, val == 1);
                } else if (this.fieldType.equals(Integer.class)) {
                    this.setter.invoke(source, (int) val);
                } else if (this.fieldType.equals(Long.class)) {
                    this.setter.invoke(source, val);
                } else if (this.fieldType.equals(Boolean.class)) {
                    this.setter.invoke(source, val == 1);
                }
            }

            this.source.onUpdate(this.field.getName(), oldValue, this.getter.invoke(source));
        } catch (Throwable e) {
            AELog.debug(e);
        }
    }
}
