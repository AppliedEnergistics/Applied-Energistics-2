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


import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.qnb.TileQuantumBridge;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;


public class ContainerQNB extends AEBaseContainer {

    SlotRestrictedInput SINGULARITY;
    SlotRestrictedInput QUANTUM_CARD;

    public ContainerQNB(final InventoryPlayer ip, final TileQuantumBridge quantumBridge) {
        super(ip, quantumBridge, null);

        SINGULARITY = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.QE_SINGULARITY, quantumBridge
                .getInternalInventory(), 0, 80, 37, this.getInventoryPlayer());
        this.addSlotToContainer(SINGULARITY.setStackLimit(1));

        QUANTUM_CARD = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.CARD_QUANTUM, quantumBridge
                .getInternalInventory(), 1, 80, 55, this.getInventoryPlayer());
        this.addSlotToContainer((QUANTUM_CARD).setStackLimit(1));

        this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);
    }


}
