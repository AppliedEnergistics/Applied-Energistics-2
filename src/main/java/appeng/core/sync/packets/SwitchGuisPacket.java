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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class SwitchGuisPacket extends BasePacket {

    private final ContainerType<?> newGui;

    public SwitchGuisPacket(final PacketBuffer stream) {
        this.newGui = Registry.MENU.getOrDefault(stream.readResourceLocation());
    }

    // api
    public SwitchGuisPacket(final ContainerType<?> newGui) {
        this.newGui = newGui;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        ResourceLocation newGuiId = Registry.MENU.getKey(newGui);
        if (newGuiId == null) {
            throw new IllegalArgumentException("Unregistered screen handler: " + newGui);
        }
        data.writeResourceLocation(newGuiId);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final Container c = player.openContainer;
        if (c instanceof AEBaseContainer) {
            final AEBaseContainer bc = (AEBaseContainer) c;
            final ContainerLocator locator = bc.getLocator();
            if (locator != null) {
                ContainerOpener.openContainer(newGui, player, locator);
            }
        }
    }
}
