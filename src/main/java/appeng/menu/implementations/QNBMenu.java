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
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.implementations.QNBScreen
 */
public class QNBMenu extends AEBaseMenu {

    public static final MenuType<QNBMenu> TYPE = ContainerTypeBuilder
            .create(QNBMenu::new, QuantumBridgeBlockEntity.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("qnb");

    public QNBMenu(int id, final Inventory ip, final QuantumBridgeBlockEntity quantumBridge) {
        super(TYPE, id, ip, quantumBridge);

        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.QE_SINGULARITY,
                quantumBridge.getInternalInventory(), 0).setStackLimit(1), SlotSemantic.STORAGE);

        this.createPlayerInventorySlots(ip);
    }

}
