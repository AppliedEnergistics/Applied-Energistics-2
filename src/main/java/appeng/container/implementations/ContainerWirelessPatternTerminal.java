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


import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;


public class ContainerWirelessPatternTerminal extends ContainerPatternEncoder {

    private final WirelessTerminalGuiObject wirelessTerminalGUIObject;

    private double powerMultiplier = 0.5;
    protected final IPortableCell civ;
    private int ticks = 0;
    private final int slot;
    private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);

    protected AppEngInternalInventory output;
    protected AppEngInternalInventory pattern;

    public ContainerWirelessPatternTerminal(final InventoryPlayer ip, final WirelessTerminalGuiObject gui) {
        super(ip, gui, gui, false);

        this.craftingSlots = new SlotFakeCraftingMatrix[9];
        this.outputSlots = new OptionalSlotFake[3];

        if (gui != null) {
            final int slotIndex = gui.getInventorySlot();
            this.lockPlayerInventorySlot(slotIndex);
            this.slot = slotIndex;
        } else {
            this.slot = -1;
            this.lockPlayerInventorySlot(ip.currentItem);
        }
        this.civ = gui;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(this.craftingSlots[x + y * 3] = new SlotFakeCraftingMatrix(this.crafting, x + y * 3, 18 + x * 18, -76 + y * 18));
            }
        }

        this.addSlotToContainer(this.craftSlot = new SlotPatternTerm(ip.player, this.getActionSource(), this
                .getPowerSource(), gui, this.crafting, patternInv, this.cOut, 110, -76 + 18, this, 2, this));
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

        this.bindPlayerInventory(ip, 0, 0);
        this.wirelessTerminalGUIObject = gui;
        this.loadFromNBT();
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack currentItem = this.slot < 0 ? this.getPlayerInv().getCurrentItem() : this.getPlayerInv().getStackInSlot(this.slot);

        if (this.civ == null || currentItem.isEmpty()) {
            this.setValidContainer(false);
        } else if (this.civ != null && !this.civ.getItemStack().isEmpty() && currentItem != this.civ.getItemStack()) {
            if (ItemStack.areItemsEqual(this.civ.getItemStack(), currentItem)) {
                this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, this.civ.getItemStack());
            } else {
                this.setValidContainer(false);
            }
        }

        // drain 1 ae t
        this.ticks++;
        if (this.ticks > 10) {
            this.civ.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
            this.ticks = 0;
        }

        if (!this.wirelessTerminalGUIObject.rangeCheck()) {
            if (Platform.isServer() && this.isValidContainer()) {
                this.getPlayerInv().player.sendMessage(PlayerMessages.OutOfRange.get());
            }

            this.setValidContainer(false);
        } else {
            this.setPowerMultiplier(AEConfig.instance().wireless_getDrainRate(this.wirelessTerminalGUIObject.getRange()));
        }

        super.detectAndSendChanges();
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
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

    @Override
    public void saveChanges() {
        if (Platform.isServer()) {
            NBTTagCompound tag = new NBTTagCompound();
            this.craftingGrid.writeToNBT(tag, "craftingGrid");
            this.wirelessTerminalGUIObject.saveChanges(tag);
        }
    }

    private void loadFromNBT() {
        NBTTagCompound data = wirelessTerminalGUIObject.getItemStack().getTagCompound();
        if (data != null) {
            this.craftingGrid.readFromNBT(data, "craftingGrid");
        }
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        if (inv == craftingGrid) {
            saveChanges();
        }
    }

    @Override
    public boolean useRealItems() {
        return false;
    }
}
