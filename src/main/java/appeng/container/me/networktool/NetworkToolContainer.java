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

package appeng.container.me.networktool;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerHelper;
import appeng.container.slot.RestrictedInputSlot;

public class NetworkToolContainer extends AEBaseContainer {

    public static ContainerType<NetworkToolContainer> TYPE;

    private static final ContainerHelper<NetworkToolContainer, INetworkTool> helper = new ContainerHelper<>(
            NetworkToolContainer::new, INetworkTool.class);

    public static NetworkToolContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final INetworkTool toolInv;

    @GuiSync(1)
    public boolean facadeMode;

    public NetworkToolContainer(int id, final PlayerInventory ip, final INetworkTool te) {
        super(TYPE, id, ip, null, null);
        this.toolInv = te;

        this.lockPlayerInventorySlot(ip.currentItem);

        for (int y = 0; y < 3; y++) {
            int slotY = 19 + y * 18;
            for (int x = 0; x < 3; x++) {
                int slotX = 62 + x * 18;
                int invSlot = y * 3 + x;
                this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, te.getInventory(),
                        invSlot, slotX, slotY, this.getPlayerInventory())));
            }
        }

        this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);
    }

    public void toggleFacadeMode() {
        final CompoundNBT data = this.toolInv.getItemStack().getOrCreateTag();
        data.putBoolean("hideFacades", !data.getBoolean("hideFacades"));
        this.detectAndSendChanges();
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack currentItem = this.getPlayerInv().getCurrentItem();

        if (currentItem != this.toolInv.getItemStack()) {
            if (!currentItem.isEmpty()) {
                if (ItemStack.areItemsEqual(this.toolInv.getItemStack(), currentItem)) {
                    this.getPlayerInv().setInventorySlotContents(this.getPlayerInv().currentItem,
                            this.toolInv.getItemStack());
                } else {
                    this.setValidContainer(false);
                }
            } else {
                this.setValidContainer(false);
            }
        }

        if (this.isValidContainer()) {
            final CompoundNBT data = currentItem.getOrCreateTag();
            this.setFacadeMode(data.getBoolean("hideFacades"));
        }

        super.detectAndSendChanges();
    }

    public boolean isFacadeMode() {
        return this.facadeMode;
    }

    private void setFacadeMode(final boolean facadeMode) {
        this.facadeMode = facadeMode;
    }
}
