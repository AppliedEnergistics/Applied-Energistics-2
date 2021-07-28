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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.items.IItemHandler;

import appeng.client.gui.Icon;
import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.definitions.AEItems;
import appeng.items.contents.QuartzKnifeObj;
import appeng.items.materials.NamePressItem;
import appeng.tile.inventory.AppEngInternalInventory;

/**
 * @see appeng.client.gui.implementations.QuartzKnifeScreen
 */
public class QuartzKnifeContainer extends AEBaseContainer {

    public static final MenuType<QuartzKnifeContainer> TYPE = ContainerTypeBuilder
            .create(QuartzKnifeContainer::new, QuartzKnifeObj.class)
            .build("quartzknife");

    private final QuartzKnifeObj toolInv;

    private final IItemHandler inSlot = new AppEngInternalInventory(null, 1, 1);

    private String currentName = "";

    public QuartzKnifeContainer(int id, final Inventory ip, final QuartzKnifeObj te) {
        super(TYPE, id, ip, te);
        this.toolInv = te;

        this.addSlot(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.METAL_INGOTS, this.inSlot, 0),
                SlotSemantic.MACHINE_INPUT);
        this.addSlot(new QuartzKniveSlot(this.inSlot, 0, null), SlotSemantic.MACHINE_OUTPUT);

        this.lockPlayerInventorySlot(ip.selected);

        this.createPlayerInventorySlots(ip);
    }

    public void setName(final String value) {
        this.currentName = value;
    }

    @Override
    public void broadcastChanges() {
        if (!ensureGuiItemIsInSlot(this.toolInv, this.getPlayerInventory().selected)) {
            this.setValidContainer(false);
            return;
        }

        super.broadcastChanges();
    }

    @Override
    public void removed(final Player player) {
        ItemStack item = this.inSlot.extractItem(0, Integer.MAX_VALUE, false);
        if (!item.isEmpty()) {
            player.drop(item, false);
        }
    }

    private class QuartzKniveSlot extends OutputSlot {
        QuartzKniveSlot(IItemHandler inv, int invSlot, Icon icon) {
            super(inv, invSlot, icon);
        }

        @Override
        public ItemStack getItem() {
            final IItemHandler baseInv = this.getItemHandler();
            final ItemStack input = baseInv.getStackInSlot(0);
            if (input == ItemStack.EMPTY) {
                return ItemStack.EMPTY;
            }

            if (RestrictedInputSlot.isMetalIngot(input) && QuartzKnifeContainer.this.currentName.length() > 0) {
                ItemStack namePressStack = AEItems.NAME_PRESS.stack();
                final CompoundTag compound = namePressStack.getOrCreateTag();
                compound.putString(NamePressItem.TAG_INSCRIBE_NAME, QuartzKnifeContainer.this.currentName);

                return namePressStack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack remove(int amount) {
            ItemStack ret = this.getItem();
            if (!ret.isEmpty()) {
                this.makePlate();
            }
            return ret;
        }

        @Override
        public void set(final ItemStack stack) {
            if (stack.isEmpty()) {
                this.makePlate();
            }
        }

        private void makePlate() {
            if (isServer() && !this.getItemHandler().extractItem(0, 1, false).isEmpty()) {
                final ItemStack item = QuartzKnifeContainer.this.toolInv.getItemStack();
                final ItemStack before = item.copy();
                Inventory playerInv = QuartzKnifeContainer.this.getPlayerInventory();
                item.hurtAndBreak(1, playerInv.player, p -> {
                    playerInv.setItem(playerInv.selected, ItemStack.EMPTY);
                    MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(playerInv.player, before, null));
                });

                QuartzKnifeContainer.this.broadcastChanges();
            }
        }
    }
}
