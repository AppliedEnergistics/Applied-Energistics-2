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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.menu.AEBaseMenu;

/**
 * This packet is used for two purposes: Server->Client to synchronize GUI data. Client->Server to synchronize GUI
 * actions.
 */
public class GuiDataSyncPacket extends BasePacket {
    private final int containerId;

    private final FriendlyByteBuf data;

    public GuiDataSyncPacket(int containerId, Consumer<FriendlyByteBuf> writer) {
        this.containerId = 0;
        this.data = null;

        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(getPacketID());
        data.writeVarInt(containerId);
        writer.accept(data);
        configureWrite(data);
    }

    public GuiDataSyncPacket(FriendlyByteBuf data) {
        this.containerId = data.readVarInt();
        this.data = new FriendlyByteBuf(data.copy());
    }

    public FriendlyByteBuf getData() {
        return data;
    }

    @Override
    public void clientPacketData(final INetworkInfo manager, final Player player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu && c.containerId == this.containerId) {
            ((AEBaseMenu) c).receiveServerSyncData(this);
        }
    }

    @Override
    public void serverPacketData(INetworkInfo manager, ServerPlayer player) {
        AbstractContainerMenu c = player.containerMenu;
        if (c instanceof AEBaseMenu && c.containerId == this.containerId) {
            ((AEBaseMenu) c).receiveClientAction(this);
        }
    }

}
