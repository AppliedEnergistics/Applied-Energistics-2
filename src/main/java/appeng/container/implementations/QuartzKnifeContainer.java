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

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.items.IItemHandler;

import appeng.client.gui.Icon;
import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.api.definitions.ApiMaterials;
import appeng.items.contents.QuartzKnifeObj;
import appeng.items.materials.MaterialItem;
import appeng.tile.inventory.AppEngInternalInventory;

/**
 * @see appeng.client.gui.implementations.QuartzKnifeScreen
 */
public class QuartzKnifeContainer extends AEBaseContainer {

    public static final ContainerType<QuartzKnifeContainer> TYPE = ContainerTypeBuilder
            .create(QuartzKnifeContainer::new, QuartzKnifeObj.class)
            .build("quartzknife");

    private final QuartzKnifeObj toolInv;

    private final IItemHandler inSlot = new AppEngInternalInventory(null, 1, 1);

    private String currentName = "";

    public QuartzKnifeContainer(int id, final PlayerInventory ip, final QuartzKnifeObj te) {
        super(TYPE, id, ip, te);
        this.toolInv = te;

        this.addSlot(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.METAL_INGOTS, this.inSlot, 0),
                SlotSemantic.MACHINE_INPUT);
        this.addSlot(new QuartzKniveSlot(this.inSlot, 0, null), SlotSemantic.MACHINE_OUTPUT);

        this.lockPlayerInventorySlot(ip.currentItem);

        this.createPlayerInventorySlots(ip);
    }

    public void setName(final String value) {
        this.currentName = value;
    }

    @Override
    public void detectAndSendChanges() {
        if (!ensureGuiItemIsInSlot(this.toolInv, this.getPlayerInventory().currentItem)) {
            this.setValidContainer(false);
            return;
        }

        super.detectAndSendChanges();
    }

    @Override
    public void onContainerClosed(final PlayerEntity player) {
        ItemStack item = this.inSlot.extractItem(0, Integer.MAX_VALUE, false);
        if (!item.isEmpty()) {
            player.dropItem(item, false);
        }
    }

    private class QuartzKniveSlot extends OutputSlot {
        QuartzKniveSlot(IItemHandler inv, int invSlot, Icon icon) {
            super(inv, invSlot, icon);
        }

        @Override
        public ItemStack getStack() {
            final IItemHandler baseInv = this.getItemHandler();
            final ItemStack input = baseInv.getStackInSlot(0);
            if (input == ItemStack.EMPTY) {
                return ItemStack.EMPTY;
            }

            if (RestrictedInputSlot.isMetalIngot(input) && QuartzKnifeContainer.this.currentName.length() > 0) {
                return ApiMaterials.namePress().maybeStack(1).map(namePressStack -> {
                    final CompoundNBT compound = namePressStack.getOrCreateTag();
                    compound.putString(MaterialItem.TAG_INSCRIBE_NAME, QuartzKnifeContainer.this.currentName);

                    return namePressStack;
                }).orElse(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack decrStackSize(int amount) {
            ItemStack ret = this.getStack();
            if (!ret.isEmpty()) {
                this.makePlate();
            }
            return ret;
        }

        @Override
        public void putStack(final ItemStack stack) {
            if (stack.isEmpty()) {
                this.makePlate();
            }
        }

        private void makePlate() {
            if (isServer() && !this.getItemHandler().extractItem(0, 1, false).isEmpty()) {
                final ItemStack item = QuartzKnifeContainer.this.toolInv.getItemStack();
                final ItemStack before = item.copy();
                PlayerInventory playerInv = QuartzKnifeContainer.this.getPlayerInventory();
                item.damageItem(1, playerInv.player, p -> {
                    playerInv.setInventorySlotContents(playerInv.currentItem, ItemStack.EMPTY);
                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(playerInv.player, before, null));
                });

                QuartzKnifeContainer.this.detectAndSendChanges();
            }
        }
    }
}
