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

package appeng.menu.implementations;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.inventories.InternalInventory;
import appeng.client.gui.Icon;
import appeng.core.definitions.AEItems;
import appeng.items.materials.NamePressItem;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.inv.AppEngInternalInventory;

/**
 * @see appeng.client.gui.implementations.QuartzKnifeScreen
 */
public class QuartzKnifeMenu extends AEBaseMenu {

    private static final String ACTION_SET_NAME = "setName";

    public static final MenuType<QuartzKnifeMenu> TYPE = MenuTypeBuilder
            .create(QuartzKnifeMenu::new, ItemMenuHost.class)
            .build("quartzknife");

    private final InternalInventory inSlot = new AppEngInternalInventory(null, 1, 1);

    private String currentName = "";

    public QuartzKnifeMenu(int id, Inventory ip, ItemMenuHost host) {
        super(TYPE, id, ip, host);

        this.addSlot(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.METAL_INGOTS, this.inSlot, 0),
                SlotSemantics.MACHINE_INPUT);
        this.addSlot(new QuartzKniveSlot(this.inSlot, 0, null), SlotSemantics.MACHINE_OUTPUT);

        this.createPlayerInventorySlots(ip);

        registerClientAction(ACTION_SET_NAME, String.class, this::setName);
    }

    public void setName(String value) {
        this.currentName = value;
        if (isClient()) {
            sendClientAction(ACTION_SET_NAME, value);
        }
    }

    @Override
    public void removed(Player player) {
        ItemStack item = this.inSlot.extractItem(0, Integer.MAX_VALUE, false);
        if (!item.isEmpty()) {
            player.drop(item, false);
        }
    }

    private class QuartzKniveSlot extends OutputSlot {
        QuartzKniveSlot(InternalInventory inv, int invSlot, Icon icon) {
            super(inv, invSlot, icon);
        }

        @Override
        public ItemStack getItem() {
            var baseInv = this.getInventory();
            final ItemStack input = baseInv.getStackInSlot(0);
            if (input == ItemStack.EMPTY) {
                return ItemStack.EMPTY;
            }

            if (RestrictedInputSlot.isMetalIngot(input) && QuartzKnifeMenu.this.currentName.length() > 0) {
                ItemStack namePressStack = AEItems.NAME_PRESS.stack();
                final CompoundTag compound = namePressStack.getOrCreateTag();
                compound.putString(NamePressItem.TAG_INSCRIBE_NAME, QuartzKnifeMenu.this.currentName);

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
            if (isServer() && !this.getInventory().extractItem(0, 1, false).isEmpty()) {
                final ItemStack item = itemMenuHost.getItemStack();
                final ItemStack before = item.copy();
                Inventory playerInv = QuartzKnifeMenu.this.getPlayerInventory();
                item.hurtAndBreak(1, playerInv.player, p -> {
                    playerInv.setItem(playerInv.selected, ItemStack.EMPTY);
                    // FIXME FABRIC no such event
                    // MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(playerInv.player, before, null));
                });

                QuartzKnifeMenu.this.broadcastChanges();
            }
        }
    }
}
