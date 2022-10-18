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


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotPatternTerm;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.sync.packets.PacketPatternSlot;
import appeng.items.storage.ItemViewCell;
import appeng.me.helpers.MachineSource;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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

        for (int y = 0; y < 3; y++) {
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

    public void craftOrGetItem(final PacketPatternSlot packetPatternSlot) {
        if (packetPatternSlot.slotItem != null && this.getCellInventory() != null) {
            final IAEItemStack out = packetPatternSlot.slotItem.copy();
            InventoryAdaptor inv = new AdaptorItemHandler(new WrapperCursorItemHandler(this.getPlayerInv().player.inventory));
            final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(this.getPlayerInv().player);

            if (packetPatternSlot.shift) {
                inv = playerInv;
            }

            if (!inv.simulateAdd(out.createItemStack()).isEmpty()) {
                return;
            }

            final IAEItemStack extracted = Platform.poweredExtraction(this.getPowerSource(), this.getCellInventory(), out, this.getActionSource());
            final EntityPlayer p = this.getPlayerInv().player;

            if (extracted != null) {
                inv.addItems(extracted.createItemStack());
                if (p instanceof EntityPlayerMP) {
                    this.updateHeld((EntityPlayerMP) p);
                }
                this.detectAndSendChanges();
                return;
            }

            final InventoryCrafting ic = new InventoryCrafting(new ContainerNull(), 3, 3);
            final InventoryCrafting real = new InventoryCrafting(new ContainerNull(), 3, 3);

            for (int x = 0; x < 9; x++) {
                ic.setInventorySlotContents(x, packetPatternSlot.pattern[x] == null ? ItemStack.EMPTY : packetPatternSlot.pattern[x].createItemStack());
            }

            final IRecipe r = CraftingManager.findMatchingRecipe(ic, p.world);

            if (r == null) {
                return;
            }

            final IMEMonitor<IAEItemStack> storage = this.getPart()
                    .getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IItemList<IAEItemStack> all = storage.getStorageList();

            final ItemStack is = r.getCraftingResult(ic);

            for (int x = 0; x < ic.getSizeInventory(); x++) {
                if (!ic.getStackInSlot(x).isEmpty()) {
                    final ItemStack pulled = Platform.extractItemsByRecipe(this.getPowerSource(), this.getActionSource(), storage, p.world, r, is, ic,
                            ic.getStackInSlot(x), x, all, Actionable.MODULATE, ItemViewCell.createFilter(this.getViewCells()));
                    real.setInventorySlotContents(x, pulled);
                }
            }

            final IRecipe rr = CraftingManager.findMatchingRecipe(real, p.world);

            if (rr == r && Platform.itemComparisons().isSameItem(rr.getCraftingResult(real), is)) {
                final InventoryCraftResult craftingResult = new InventoryCraftResult();
                craftingResult.setRecipeUsed(rr);

                final SlotCrafting sc = new SlotCrafting(p, real, craftingResult, 0, 0, 0);
                sc.onTake(p, is);

                for (int x = 0; x < real.getSizeInventory(); x++) {
                    final ItemStack failed = playerInv.addItems(real.getStackInSlot(x));

                    if (!failed.isEmpty()) {
                        p.dropItem(failed, false);
                    }
                }

                inv.addItems(is);
                if (p instanceof EntityPlayerMP) {
                    this.updateHeld((EntityPlayerMP) p);
                }
                this.detectAndSendChanges();
            } else {
                for (int x = 0; x < real.getSizeInventory(); x++) {
                    final ItemStack failed = real.getStackInSlot(x);
                    if (!failed.isEmpty()) {
                        this.getCellInventory()
                                .injectItems(AEItemStack.fromItemStack(failed), Actionable.MODULATE,
                                        new MachineSource(this.getPart()));
                    }
                }
            }
        }
    }
}
