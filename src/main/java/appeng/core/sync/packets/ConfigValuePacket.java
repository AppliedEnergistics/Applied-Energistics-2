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

package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class ConfigValuePacket extends BasePacket {

    private final String name;
    private final String value;

    public ConfigValuePacket(final FriendlyByteBuf stream) {
        this.name = stream.readUtf();
        this.value = stream.readUtf();
    }

    // api
    private ConfigValuePacket(final String name, final String value) {
        this.name = name;
        this.value = value;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());

        data.writeUtf(name);
        data.writeUtf(value);

        this.configureWrite(data);
    }

    public <T extends Enum<T>> ConfigValuePacket(Setting<T> setting, T value) {
        this(setting.getName(), value.name());
        if (!setting.getValues().contains(value)) {
            throw new IllegalStateException(value + " not a valid value for " + setting);
        }
    }

    public <T extends Enum<T>> ConfigValuePacket(Setting<T> setting, IConfigManager configManager) {
        this(setting, setting.getValue(configManager));
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final ServerPlayer player) {
        final AbstractContainerMenu c = player.containerMenu;
        if (c instanceof IConfigurableObject configurableObject) {
            loadSetting(configurableObject);
        }
    }

    @Override
    public void clientPacketData(final INetworkInfo network, final Player player) {
        final AbstractContainerMenu c = player.containerMenu;

        if (c instanceof IConfigurableObject configurableObject) {
            loadSetting(configurableObject);
        }
    }

    private void loadSetting(IConfigurableObject configurableObject) {
        var cm = configurableObject.getConfigManager();

        for (var setting : cm.getSettings()) {
            if (setting.getName().equals(this.name)) {
                setting.setFromString(cm, value);
                break;
            }
        }
    }

}
