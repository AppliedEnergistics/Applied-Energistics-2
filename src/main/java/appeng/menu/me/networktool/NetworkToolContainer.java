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

package appeng.menu.me.networktool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.menu.AEBaseContainer;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.ContainerTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.me.networktool.NetworkToolScreen
 */
public class NetworkToolContainer extends AEBaseContainer {

    public static final MenuType<NetworkToolContainer> TYPE = ContainerTypeBuilder
            .create(NetworkToolContainer::new, INetworkTool.class)
            .build("networktool");

    private final INetworkTool toolInv;

    @GuiSync(1)
    public boolean facadeMode;

    public NetworkToolContainer(int id, final Inventory ip, final INetworkTool te) {
        super(TYPE, id, ip, null);
        this.toolInv = te;

        this.lockPlayerInventorySlot(ip.selected);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, te.getInventory(),
                    i), SlotSemantic.STORAGE);
        }

        this.createPlayerInventorySlots(ip);
    }

    public void toggleFacadeMode() {
        final CompoundTag data = this.toolInv.getItemStack().getOrCreateTag();
        data.putBoolean("hideFacades", !data.getBoolean("hideFacades"));
        this.broadcastChanges();
    }

    @Override
    public void broadcastChanges() {
        final ItemStack currentItem = this.getPlayerInventory().getSelected();

        if (currentItem != this.toolInv.getItemStack()) {
            if (!currentItem.isEmpty()) {
                if (ItemStack.isSame(this.toolInv.getItemStack(), currentItem)) {
                    this.getPlayerInventory().setItem(this.getPlayerInventory().selected,
                            this.toolInv.getItemStack());
                } else {
                    this.setValidContainer(false);
                }
            } else {
                this.setValidContainer(false);
            }
        }

        if (this.isValidContainer()) {
            final CompoundTag data = currentItem.getOrCreateTag();
            this.setFacadeMode(data.getBoolean("hideFacades"));
        }

        super.broadcastChanges();
    }

    public boolean isFacadeMode() {
        return this.facadeMode;
    }

    private void setFacadeMode(final boolean facadeMode) {
        this.facadeMode = facadeMode;
    }
}
