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


import appeng.api.storage.ITerminalHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;


public class ContainerPatternTerm extends ContainerPatternEncoder {
    

    public ContainerPatternTerm(final InventoryPlayer ip, final ITerminalHost monitorable) {
        super(ip, monitorable, false);

        this.craftingSlots = new SlotFakeCraftingMatrix[9];
        this.outputSlots = new OptionalSlotFake[3];

        final IItemHandler patternInv = this.getPart().getInventoryByName("pattern");
        final IItemHandler output = this.getPart().getInventoryByName("output");

        this.crafting = this.getPart().getInventoryByName("crafting");

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(this.craftingSlots[x + y * 3] = new SlotFakeCraftingMatrix(this.crafting, x + y * 3, 18 + x * 18, -76 + y * 18));
            }
        }

        this.addSlotToContainer(this.craftSlot = new SlotPatternTerm(ip.player, this.getActionSource(), this
                .getPowerSource(), monitorable, this.crafting, patternInv, this.cOut, 110, -76 + 18, this, 2, this));
        this.craftSlot.setIIcon(-1);

        for (int y = 0; y < this.outputSlots.length; y++) {
            this.addSlotToContainer(this.outputSlots[y] = new SlotPatternOutputs(output, this, y, 110, -76 + y * 18, 0, 0, 1));
            this.outputSlots[y].setRenderDisabled(false);
            this.outputSlots[y].setIIcon(-1);
        }

        this.addSlotToContainer(
                this.patternSlotIN = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, patternInv, 0, 147, -72 - 9, this
                        .getInventoryPlayer()));
        this.addSlotToContainer(
                this.patternSlotOUT = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, patternInv, 1, 147, -72 + 34, this
                        .getInventoryPlayer()));

        this.patternSlotOUT.setStackLimit(1);

        this.bindPlayerInventory(ip, 0, 0);
        this.updateOrderOfOutputSlots();
    }


    @Override
    public boolean isSlotEnabled(final int idx) {
        if (idx == 1) {
            return Platform.isServer() ? !this.getPart().isCraftingRecipe() : !this.isCraftingMode();
        } else if (idx == 2) {
            return Platform.isServer() ? this.getPart().isCraftingRecipe() : this.isCraftingMode();
        } else {
            return false;
        }
    }

}
