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
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IUpgradeableCellContainer;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.InvOperation;
import baubles.api.BaublesApi;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import static appeng.helpers.PatternHelper.CRAFTING_GRID_DIMENSION;


public class ContainerWirelessPatternTerminal extends ContainerPatternEncoder implements IUpgradeableCellContainer {

    private final WirelessTerminalGuiObject wirelessTerminalGUIObject;
    private final int slot;
    protected AppEngInternalInventory output;
    protected AppEngInternalInventory pattern;
    protected AppEngInternalInventory upgrades;

    private double powerMultiplier = 0.5;
    private int ticks = 0;

    public ContainerWirelessPatternTerminal(final InventoryPlayer ip, final WirelessTerminalGuiObject gui) {
        super(ip, gui, gui, false);

        this.crafting = new AppEngInternalInventory(this, CRAFTING_GRID_DIMENSION * CRAFTING_GRID_DIMENSION);
        this.output = new AppEngInternalInventory(this, 3);
        this.pattern = new AppEngInternalInventory(this, 2);

        this.craftingSlots = new SlotFakeCraftingMatrix[9];
        this.outputSlots = new OptionalSlotFake[3];

        if (gui != null) {
            final int slotIndex = gui.getInventorySlot();
            if (!((IInventorySlotAware) gui).isBaubleSlot()) {
                this.lockPlayerInventorySlot(slotIndex);
            }
            this.slot = slotIndex;
        } else {
            this.slot = -1;
            this.lockPlayerInventorySlot(ip.currentItem);
        }
        this.wirelessTerminalGUIObject = gui;
        upgrades = new StackUpgradeInventory(wirelessTerminalGUIObject.getItemStack(), this, 2);

        this.loadFromNBT();

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(this.craftingSlots[x + y * 3] = new SlotFakeCraftingMatrix(this.crafting, x + y * 3, 18 + x * 18, -76 + y * 18));
            }
        }

        this.addSlotToContainer(this.craftSlot = new SlotPatternTerm(ip.player, this.getActionSource(), this
                .getPowerSource(), gui, this.crafting, pattern, this.cOut, 110, -76 + 18, this, 2, this));
        this.craftSlot.setIIcon(-1);

        for (int y = 0; y < this.outputSlots.length; y++) {
            this.addSlotToContainer(this.outputSlots[y] = new SlotPatternOutputs(output, this, y, 110, -76 + y * 18, 0, 0, 1));
            this.outputSlots[y].setRenderDisabled(false);
            this.outputSlots[y].setIIcon(-1);
        }

        this.addSlotToContainer(
                this.patternSlotIN = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, pattern, 0, 147, -72 - 9, this
                        .getInventoryPlayer()));
        this.addSlotToContainer(
                this.patternSlotOUT = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, pattern, 1, 147, -72 + 34, this
                        .getInventoryPlayer()));

        this.patternSlotOUT.setStackLimit(1);

        this.updateOrderOfOutputSlots();

        this.bindPlayerInventory(ip, 0, 0);

        this.setupUpgrades();
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {

            final ItemStack currentItem;
            if (wirelessTerminalGUIObject.isBaubleSlot()) {
                currentItem = BaublesApi.getBaublesHandler(this.getPlayerInv().player).getStackInSlot(this.slot);
            } else {
                currentItem = this.slot < 0 ? this.getPlayerInv().getCurrentItem() : this.getPlayerInv().getStackInSlot(this.slot);
            }

            if (currentItem.isEmpty()) {
                this.setValidContainer(false);
            } else if (!this.wirelessTerminalGUIObject.getItemStack().isEmpty() && currentItem != this.wirelessTerminalGUIObject.getItemStack()) {
                if (ItemStack.areItemsEqual(this.wirelessTerminalGUIObject.getItemStack(), currentItem)) {
                    this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem, this.wirelessTerminalGUIObject.getItemStack());
                } else {
                    this.setValidContainer(false);
                }
            }

            // drain 1 ae t
            this.ticks++;
            if (this.ticks > 10) {
                this.wirelessTerminalGUIObject.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
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
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        boolean crafting = false;
        if (Platform.isServer()) {
            NBTTagCompound nbtTagCompound = iGuiItemObject.getItemStack().getTagCompound();
            if (nbtTagCompound != null) {
                if (nbtTagCompound.hasKey("isCraftingMode")) {
                    crafting = nbtTagCompound.getBoolean("isCraftingMode");
                }
            }
        }
        if (idx == 1) {
            return Platform.isServer() ? !crafting : !this.isCraftingMode();
        } else if (idx == 2) {
            return Platform.isServer() ? crafting : this.isCraftingMode();
        } else {
            return false;
        }
    }

    @Override
    public void saveChanges() {
        if (Platform.isServer()) {
            NBTTagCompound tag = new NBTTagCompound();
            ((AppEngInternalInventory) crafting).writeToNBT(tag, "craftingGrid");

            this.output.writeToNBT(tag, "output");
            this.pattern.writeToNBT(tag, "patterns");
            this.upgrades.writeToNBT(tag, "upgrades");

            this.wirelessTerminalGUIObject.saveChanges(tag);
        }
    }

    private void loadFromNBT() {
        NBTTagCompound data = wirelessTerminalGUIObject.getItemStack().getTagCompound();
        if (data != null) {
            ((AppEngInternalInventory) crafting).readFromNBT(data, "craftingGrid");
            this.output.readFromNBT(data, "output");
            this.pattern.readFromNBT(data, "patterns");
            upgrades.readFromNBT(wirelessTerminalGUIObject.getItemStack().getTagCompound().getCompoundTag("upgrades"));
        }
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = this.pattern.getStackInSlot(1);
            if (!is.isEmpty() && is.getItem() instanceof ICraftingPatternItem) {
                final ICraftingPatternItem pattern = (ICraftingPatternItem) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem(is, this.getPlayerInv().player.world);
                if (details != null) {
                    this.setCraftingMode(details.isCraftable());
                    this.setSubstitute(details.canSubstitute());

                    for (int x = 0; x < this.crafting.getSlots() && x < details.getInputs().length; x++) {
                        final IAEItemStack item = details.getInputs()[x];
                        ItemHandlerUtil.setStackInSlot(this.crafting, x, item == null ? ItemStack.EMPTY : item.createItemStack());
                    }

                    for (int x = 0; x < this.output.getSlots() && x < details.getOutputs().length; x++) {
                        final IAEItemStack item = details.getOutputs()[x];
                        this.output.setStackInSlot(x, item == null ? ItemStack.EMPTY : item.createItemStack());
                    }
                }
            }
        }
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        }
        return super.getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 2;
    }

    @Override
    public void setupUpgrades() {
        if (wirelessTerminalGUIObject != null) {
            for (int upgradeSlot = 0; upgradeSlot < availableUpgrades(); upgradeSlot++) {
                this.addSlotToContainer(
                        (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, upgradeSlot, 187, upgradeSlot * 18 - 2, this.getInventoryPlayer()))
                                .setNotDraggable());
            }
        }
    }
}
