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


import appeng.container.ContainerNull;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotCraftingTerm;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperInvItemHandler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;


public class ContainerWirelessCraftingTerminal extends ContainerMEPortableCell implements IAEAppEngInventory, IContainerCraftingPacket {

    private final WirelessTerminalGuiObject wirelessTerminalGUIObject;

    private final AppEngInternalInventory output = new AppEngInternalInventory(this, 1);
    private final SlotCraftingMatrix[] craftingSlots = new SlotCraftingMatrix[9];
    private final SlotCraftingTerm outputSlot;

    private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);

    private IRecipe currentRecipe;

    public ContainerWirelessCraftingTerminal(final InventoryPlayer ip, final WirelessTerminalGuiObject gui) {
        super(ip, gui, gui, false);
        this.wirelessTerminalGUIObject = gui;

        final IItemHandler crafting = this.craftingGrid;

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlotToContainer(this.craftingSlots[x + y * 3] = new SlotCraftingMatrix(this, crafting, x + y * 3, 37 + x * 18, -72 + y * 18));
            }
        }

        this.addSlotToContainer(this.outputSlot = new SlotCraftingTerm(this.getPlayerInv().player, this.getActionSource(), this
                .getPowerSource(), this.civ, crafting, crafting, this.output, 131, -72 + 18, this));

        this.bindPlayerInventory(ip, 0, 0);
        this.loadFromNBT();

        this.onCraftMatrixChanged(new WrapperInvItemHandler(crafting));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (!this.wirelessTerminalGUIObject.rangeCheck()) {
            if (Platform.isServer() && this.isValidContainer()) {
                this.getPlayerInv().player.sendMessage(PlayerMessages.OutOfRange.get());
            }

            this.setValidContainer(false);
        } else {
            this.setPowerMultiplier(AEConfig.instance().wireless_getDrainRate(this.wirelessTerminalGUIObject.getRange()));
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        final ContainerNull cn = new ContainerNull();
        final InventoryCrafting ic = new InventoryCrafting(cn, 3, 3);

        for (int x = 0; x < 9; x++) {
            ic.setInventorySlotContents(x, this.craftingSlots[x].getStack());
        }

        if (this.currentRecipe == null || !this.currentRecipe.matches(ic, this.getPlayerInv().player.world)) {
            this.currentRecipe = CraftingManager.findMatchingRecipe(ic, this.getPlayerInv().player.world);
        }

        if (this.currentRecipe == null) {
            this.outputSlot.putStack(ItemStack.EMPTY);
        } else {
            final ItemStack craftingResult = this.currentRecipe.getCraftingResult(ic);

            this.outputSlot.putStack(craftingResult);
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
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("player")) {
            return new PlayerInvWrapper(this.getInventoryPlayer());
        } else if (name.equals("crafting")) {
            return this.craftingGrid;
        }
        return null;
    }

    @Override
    public boolean useRealItems() {
        return true;
    }
}
