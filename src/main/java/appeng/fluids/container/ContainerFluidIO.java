/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;

import appeng.api.config.SecurityPermissions;
import appeng.container.ContainerLocator;
import appeng.container.implementations.ContainerHelper;
import appeng.fluids.parts.PartSharedFluidBus;
import appeng.fluids.util.IAEFluidTank;

/**
 * @author BrockWS
 * @version rv5 - 1/05/2018
 * @since rv5 1/05/2018
 */
public class ContainerFluidIO extends ContainerFluidConfigurable {

    public static ContainerType<ContainerFluidIO> TYPE;

    private static final ContainerHelper<ContainerFluidIO, PartSharedFluidBus> helper = new ContainerHelper<>(
            ContainerFluidIO::new, PartSharedFluidBus.class, SecurityPermissions.BUILD);

    public static ContainerFluidIO fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final PartSharedFluidBus bus;

    public ContainerFluidIO(int id, PlayerInventory ip, PartSharedFluidBus te) {
        super(TYPE, id, ip, te);
        this.bus = te;
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return this.bus.getConfig();
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }
}
