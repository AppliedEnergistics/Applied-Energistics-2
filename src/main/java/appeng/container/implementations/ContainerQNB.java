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
import appeng.container.slot.IOptionalSlotHost;
import appeng.container.slot.OptionalSlotRestrictedInput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.qnb.TileQuantumBridge;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;


public class ContainerQNB extends AEBaseContainer implements IOptionalSlotHost {

    OptionalSlotRestrictedInput SINGULARITY;
    OptionalSlotRestrictedInput QUANTUM_CARD;

    public ContainerQNB(final InventoryPlayer ip, final TileQuantumBridge quantumBridge) {
        super(ip, quantumBridge, null);

        SINGULARITY = new OptionalSlotRestrictedInput(SlotRestrictedInput.PlacableItemType.QE_SINGULARITY, quantumBridge
                .getInternalInventory(), this, 0, 80, 37, 0, this.getInventoryPlayer());
        this.addSlotToContainer(SINGULARITY.setStackLimit(1));

        QUANTUM_CARD = new OptionalSlotRestrictedInput(SlotRestrictedInput.PlacableItemType.CARD_QUANTUM, quantumBridge
                .getInternalInventory(), this, 1, 80, 55, 1, this.getInventoryPlayer());
        this.addSlotToContainer((QUANTUM_CARD).setStackLimit(1));

        this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);
    }


    @Override
    public boolean isSlotEnabled(int idx) {
        if (QUANTUM_CARD.getItemHandler().getStackInSlot(QUANTUM_CARD.slotNumber).isEmpty() &&
                SINGULARITY.getItemHandler().getStackInSlot(SINGULARITY.slotNumber).isEmpty()) {
            return true;
        } else if (idx == 0) {
            return QUANTUM_CARD.getItemHandler().getStackInSlot(QUANTUM_CARD.slotNumber).isEmpty();
        } else if (idx == 1) {
            return SINGULARITY.getItemHandler().getStackInSlot(SINGULARITY.slotNumber).isEmpty();
        }
        return false;
    }
}
