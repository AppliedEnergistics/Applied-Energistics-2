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

import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import appeng.api.AEApi;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.slot.OutputSlot;
import appeng.container.slot.RestrictedInputSlot;
import appeng.items.contents.QuartzKnifeObj;
import appeng.items.materials.MaterialItem;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;

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

        this.lockPlayerInventorySlot(ip.currentItem);

        this.bindPlayerInventory(ip, 0, 184 - /* height of player inventory */82);
    }

    public void setName(final String value) {
        this.myName = value;
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack currentItem = this.getPlayerInv().getCurrentItem();

        if (currentItem != this.toolInv.getItemStack()) {
            if (!currentItem.isEmpty()) {
                if (ItemStack.areItemsEqual(this.toolInv.getItemStack(), currentItem)) {
                    this.getPlayerInv().setStack(this.getPlayerInv().currentItem,
                            this.toolInv.getItemStack());
                } else {
                    this.setValidContainer(false);
                }
            } else {
                this.setValidContainer(false);
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public void onContainerClosed(final PlayerEntity par1PlayerEntity) {
        if (this.inSlot.getInvStack(0) != null) {
            par1PlayerEntity.dropItem(this.inSlot.getInvStack(0), false);
        }
    }

    private class QuartzKniveSlot extends OutputSlot {
        QuartzKniveSlot(FixedItemInv a, int b, int c, int d, int i) {
            super(a, b, c, d, i);
        }

        @Override
        public ItemStack getStack() {
            final FixedItemInv baseInv = this.getItemHandler();
            final ItemStack input = baseInv.getInvStack(0);
            if (input == ItemStack.EMPTY) {
                return ItemStack.EMPTY;
            }

            if (RestrictedInputSlot.isMetalIngot(input)) {
                if (QuartzKnifeContainer.this.myName.length() > 0) {
                    return AEApi.instance().definitions().materials().namePress().maybeStack(1).map(namePressStack -> {
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
            if (Platform.isServer()) {
                if (!this.getItemHandler().extractItem(0, 1, false).isEmpty()) {
                    final ItemStack item = QuartzKnifeContainer.this.toolInv.getItemStack();
                    final ItemStack before = item.copy();
                    item.damage(1, QuartzKnifeContainer.this.getPlayerInv().player, p -> {
                        QuartzKnifeContainer.this.getPlayerInv().setStack(
                                QuartzKnifeContainer.this.getPlayerInv().currentItem, ItemStack.EMPTY);
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(
                                QuartzKnifeContainer.this.getPlayerInv().player, before, null));
                    });

                    QuartzKnifeContainer.this.detectAndSendChanges();
                }
            }
        }
    }
}
