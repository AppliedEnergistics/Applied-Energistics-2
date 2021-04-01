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

package appeng.container.implementations;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.helpers.IPriorityHost;

public class PriorityContainer extends AEBaseContainer {

    public static ContainerType<PriorityContainer> TYPE;

    private static final ContainerHelper<PriorityContainer, IPriorityHost> helper = new ContainerHelper<>(
            PriorityContainer::new, IPriorityHost.class, SecurityPermissions.BUILD);

    public static PriorityContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf, (host, container, buffer) -> {
            container.priorityValue = buffer.readVarInt();
        });
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator, (host, buffer) -> buffer.writeVarInt(host.getPriority()));
    }

    private final IPriorityHost priHost;

    private int priorityValue;

    public PriorityContainer(int id, final PlayerInventory ip, final IPriorityHost te) {
        super(TYPE, id, ip, (TileEntity) (te instanceof TileEntity ? te : null),
                (IPart) (te instanceof IPart ? te : null));
        this.priHost = te;
        this.priorityValue = te.getPriority();
    }

    public void setPriority(final int newValue) {
        if (newValue != priorityValue) {
            if (isClient()) {
                // If for whatever reason the client enters the value first, do not update based
                // on incoming server data
                this.priorityValue = newValue;
                NetworkHandler.instance()
                        .sendToServer(new ConfigValuePacket("PriorityHost.Priority", String.valueOf(newValue)));
            } else {
                this.priHost.setPriority(newValue);
                this.priorityValue = newValue;
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.verifyPermissions(SecurityPermissions.BUILD, false);
    }

    public int getPriorityValue() {
        return priorityValue;
    }

    public IPriorityHost getPriorityHost() {
        return this.priHost;
    }

}
