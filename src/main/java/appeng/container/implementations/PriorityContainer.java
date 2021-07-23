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

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.helpers.IPriorityHost;

/**
 * @see appeng.client.gui.implementations.PriorityScreen
 */
public class PriorityContainer extends AEBaseContainer {

    public static final MenuType<PriorityContainer> TYPE = ContainerTypeBuilder
            .create(PriorityContainer::new, IPriorityHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .withInitialData((host, buffer) -> {
                buffer.writeVarInt(host.getPriority());
            }, (host, container, buffer) -> {
                container.priorityValue = buffer.readVarInt();
            })
            .build("priority");

    private final IPriorityHost priHost;

    private int priorityValue;

    public PriorityContainer(int id, final Inventory ip, final IPriorityHost te) {
        super(TYPE, id, ip, te);
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
    public void broadcastChanges() {
        super.broadcastChanges();
        this.verifyPermissions(SecurityPermissions.BUILD, false);
    }

    public int getPriorityValue() {
        return priorityValue;
    }

    public IPriorityHost getPriorityHost() {
        return this.priHost;
    }

}
