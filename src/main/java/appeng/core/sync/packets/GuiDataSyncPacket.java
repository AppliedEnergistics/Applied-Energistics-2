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

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

import appeng.container.AEBaseContainer;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

/**
 * This packet is used for two purposes: Server->Client to synchronize GUI data. Client->Server to synchronize GUI
 * actions.
 */
public class GuiDataSyncPacket extends BasePacket {
    private final int windowId;

    private final PacketBuffer data;

    public GuiDataSyncPacket(int windowId, Consumer<PacketBuffer> writer) {
        this.windowId = 0;
        this.data = null;

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(getPacketID());
        data.writeVarInt(windowId);
        writer.accept(data);
        configureWrite(data);
    }

    public GuiDataSyncPacket(PacketBuffer data) {
        this.windowId = data.readVarInt();
        this.data = new PacketBuffer(data.copy());
    }

    public PacketBuffer getData() {
        return data;
    }

    @Override
    public void clientPacketData(final INetworkInfo manager, final PlayerEntity player) {
        Container c = player.containerMenu;
        if (c instanceof AEBaseContainer && c.containerId == this.windowId) {
            ((AEBaseContainer) c).receiveServerSyncData(this);
        }
    }

    @Override
    public void serverPacketData(INetworkInfo manager, PlayerEntity player) {
        Container c = player.containerMenu;
        if (c instanceof AEBaseContainer && c.containerId == this.windowId) {
            ((AEBaseContainer) c).receiveClientAction(this);
        }
    }

}
