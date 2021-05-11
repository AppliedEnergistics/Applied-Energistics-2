/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.container.me.networktool.NetworkStatus;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class NetworkStatusPacket extends BasePacket {

    private final NetworkStatus status;

    public NetworkStatusPacket(PacketBuffer data) {
        this.status = NetworkStatus.read(data);
    }

    public NetworkStatusPacket(NetworkStatus status) {
        this.status = null;

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        status.write(data);
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().currentScreen;

        if (gs instanceof NetworkStatusScreen) {
            ((NetworkStatusScreen) gs).processServerUpdate(status);
        }
    }

}
