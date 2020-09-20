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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.Api;
import appeng.items.contents.QuartzKnifeObj;
import appeng.items.materials.MaterialItem;
import appeng.tile.inventory.AppEngInternalInventory;

public class QuartzKnifeContainer extends AEBaseContainer {

    public static ScreenHandlerType<QuartzKnifeContainer> TYPE;

    private static final ContainerHelper<QuartzKnifeContainer, QuartzKnifeObj> helper = new ContainerHelper<>(
            QuartzKnifeContainer::new, QuartzKnifeObj.class);

    public static QuartzKnifeContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final QuartzKnifeObj toolInv;

    private final FixedItemInv inSlot = new AppEngInternalInventory(null, 1, 1);
    private String myName = "";

    public QuartzKnifeContainer(int id, final PlayerInventory ip, final QuartzKnifeObj te) {
        super(TYPE, id, ip, null, null);
        this.toolInv = te;

        this.addSlot(
                new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.METAL_INGOTS, this.inSlot, 0, 94, 44, ip));
        this.addSlot(new QuartzKniveSlot(this.inSlot, 0, 134, 44, -1));

        this.lockPlayerInventorySlot(ip.selectedSlot);

        this.bindPlayerInventory(ip, 0, 184 - /* height of player inventory */82);
    }

    public void setName(final String value) {
        this.myName = value;
    }

    @Override
    public void sendContentUpdates() {
        final ItemStack currentItem = this.getPlayerInv().getMainHandStack();

        if (currentItem != this.toolInv.getItemStack()) {
            if (!currentItem.isEmpty()) {
                if (ItemStack.areItemsEqual(this.toolInv.getItemStack(), currentItem)) {
                    this.getPlayerInv().setStack(this.getPlayerInv().selectedSlot, this.toolInv.getItemStack());
                } else {
                    this.setValidContainer(false);
                }
            } else {
                this.setValidContainer(false);
            }
        }

        super.sendContentUpdates();
    }

    @Override
    public void close(final PlayerEntity par1PlayerEntity) {
        if (this.inSlot.getInvStack(0) != null) {
            par1PlayerEntity.dropItem(this.inSlot.getInvStack(0), false);
        }
    }

    private class QuartzKniveSlot extends OutputSlot {
        QuartzKniveSlot(FixedItemInv inv, int invSlot, int x, int y, int iconIdx) {
            super(inv, invSlot, x, y, iconIdx);
        }

        @Override
        public ItemStack getStack() {
            final ItemStack input = super.getStack();

            if (RestrictedInputSlot.isMetalIngot(input)) {
                if (QuartzKnifeContainer.this.myName.length() > 0) {
                    return Api.instance().definitions().materials().namePress().maybeStack(1).map(namePressStack -> {
                        final CompoundTag compound = namePressStack.getOrCreateTag();
                        compound.putString(MaterialItem.TAG_INSCRIBE_NAME, QuartzKnifeContainer.this.myName);

                        return namePressStack;
                    }).orElse(ItemStack.EMPTY);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack takeStack(int amount) {
            ItemStack ret = this.getStack();
            if (!ret.isEmpty()) {
                this.makePlate();
            }
            return ret;
        }

        @Override
        public void setStack(ItemStack stack) {
            if (stack.isEmpty()) {
                this.makePlate();
            }
        }

        private void makePlate() {
            if (isServer()) {
                if (!inSlot.getSlot(0).extract(1).isEmpty()) {
                    final ItemStack item = QuartzKnifeContainer.this.toolInv.getItemStack();
                    final ItemStack before = item.copy();
                    // FIXME FABRIC: Quartz Knife currently is not damageable due to recipe
                    // remainder concerns
                    item.damage(1, QuartzKnifeContainer.this.getPlayerInv().player, p -> {
                        QuartzKnifeContainer.this.getPlayerInv()
                                .setStack(QuartzKnifeContainer.this.getPlayerInv().selectedSlot, ItemStack.EMPTY);
                        // FIXME FABRIC equivalent??
                        // FIXME FABRIC MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(
                        // FIXME FABRIC QuartzKnifeContainer.this.getPlayerInv().player, before, null));
                    });

                    QuartzKnifeContainer.this.sendContentUpdates();
                }
            }
        }
    }
}
