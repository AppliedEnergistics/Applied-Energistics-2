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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.helpers.IPriorityHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;

/**
 * @see appeng.client.gui.implementations.PriorityScreen
 */
public class PriorityMenu extends AEBaseMenu implements ISubMenu {

    private static final String ACTION_SET_PRIORITY = "setPriority";

    public static final MenuType<PriorityMenu> TYPE = MenuTypeBuilder
            .create(PriorityMenu::new, IPriorityHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .withInitialData((host, buffer) -> {
                buffer.writeVarInt(host.getPriority());
            }, (host, menu, buffer) -> {
                menu.priorityValue = buffer.readVarInt();
            })
            .build("priority");

    private final IPriorityHost host;

    private int priorityValue;

    public PriorityMenu(int id, Inventory ip, IPriorityHost host) {
        super(TYPE, id, ip, host);
        this.host = host;
        this.priorityValue = host.getPriority();

        registerClientAction(ACTION_SET_PRIORITY, Integer.class, this::setPriority);
    }

    public void setPriority(int newValue) {
        if (newValue != priorityValue) {
            if (isClientSide()) {
                // If for whatever reason the client enters the value first, do not update based
                // on incoming server data
                this.priorityValue = newValue;
                sendClientAction(ACTION_SET_PRIORITY, newValue);
            } else {
                this.host.setPriority(newValue);
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

    @Override
    public IPriorityHost getHost() {
        return this.host;
    }

}
