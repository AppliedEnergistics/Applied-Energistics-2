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

import java.util.EnumSet;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.menu.AEBaseContainer;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.EnumCycler;

public final class ConfigButtonPacket extends BasePacket {
    private final Settings option;
    private final boolean rotationDirection;

    public ConfigButtonPacket(final FriendlyByteBuf stream) {
        this.option = Settings.values()[stream.readInt()];
        this.rotationDirection = stream.readBoolean();
    }

    // api
    public ConfigButtonPacket(final Settings option, final boolean rotationDirection) {
        this.option = option;
        this.rotationDirection = rotationDirection;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(option.ordinal());
        data.writeBoolean(rotationDirection);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final Player player) {
        final ServerPlayer sender = (ServerPlayer) player;
        if (sender.containerMenu instanceof AEBaseContainer) {
            final AEBaseContainer baseContainer = (AEBaseContainer) sender.containerMenu;
            if (baseContainer.getTarget() instanceof IConfigurableObject) {
                final IConfigManager cm = ((IConfigurableObject) baseContainer.getTarget()).getConfigManager();
                Enum setting = cm.getSetting(this.option);
                Enum newState = EnumCycler.rotateEnum(setting, this.rotationDirection,
                        (EnumSet) this.option.getPossibleValues());
                cm.putSetting(this.option, newState);
            }
        }
    }

}
