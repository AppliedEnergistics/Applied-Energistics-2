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


import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.SlotOutput;
import appeng.container.slot.SlotRestrictedInput;
import appeng.tile.misc.TileCondenser;
import appeng.util.Platform;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;


public class ContainerCondenser extends AEBaseContainer implements IProgressProvider {

    private final TileCondenser condenser;
    @GuiSync(0)
    public long requiredEnergy = 0;
    @GuiSync(1)
    public long storedPower = 0;
    @GuiSync(2)
    public CondenserOutput output = CondenserOutput.TRASH;
    private final ItemStack prevStack = ItemStack.EMPTY;

    public ContainerCondenser(final InventoryPlayer ip, final TileCondenser condenser) {
        super(ip, condenser, null);
        this.condenser = condenser;

        IItemHandler inv = condenser.getInternalInventory();

        this.addSlotToContainer(new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.TRASH, inv, 0, 51, 52, ip));
        this.addSlotToContainer(new SlotOutput(inv, 1, 105, 52, -1));
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.STORAGE_COMPONENT, inv, 2, 101, 26, ip)).setStackLimit(1));

        this.bindPlayerInventory(ip, 0, 197 - /* height of player inventory */82);
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack is = this.condenser.getInternalInventory().getStackInSlot(1);
        if (Platform.isServer()) {
            final double maxStorage = this.condenser.getStorage();
            final double requiredEnergy = this.condenser.getRequiredPower();

            this.requiredEnergy = requiredEnergy == 0 ? (int) maxStorage : (int) Math.min(requiredEnergy, maxStorage);
            this.storedPower = (int) this.condenser.getStoredPower();
            this.setOutput((CondenserOutput) this.condenser.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT));

            for (final IContainerListener listener : this.listeners) {
                if (!ItemStack.areItemsEqual(is, prevStack)) {
                    listener.sendSlotContents(this, 1, is);
                }
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public int getCurrentProgress() {
        return (int) this.storedPower;
    }

    @Override
    public int getMaxProgress() {
        return (int) this.requiredEnergy;
    }

    public CondenserOutput getOutput() {
        return this.output;
    }

    private void setOutput(final CondenserOutput output) {
        this.output = output;
    }
}
