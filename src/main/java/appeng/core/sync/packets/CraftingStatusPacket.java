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

import appeng.client.gui.me.crafting.CraftingCPUScreen;
import appeng.container.me.crafting.CraftingStatus;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class CraftingStatusPacket extends BasePacket {
    private final CraftingStatus status;

    public CraftingStatusPacket(PacketBuffer buffer) {
        this.status = CraftingStatus.read(buffer);
    }

    public CraftingStatusPacket(CraftingStatus status) {
        this.status = status;

        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(getPacketID());
        status.write(data);
        configureWrite(data);
    }

    @Override
    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        Screen screen = Minecraft.getInstance().currentScreen;

        if (screen instanceof CraftingCPUScreen) {
            CraftingCPUScreen<?> cpuScreen = (CraftingCPUScreen<?>) screen;
            cpuScreen.postUpdate(this.status);
        }
    }

}
