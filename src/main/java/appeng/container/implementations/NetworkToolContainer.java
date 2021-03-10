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

        this.lockPlayerInventorySlot(ip.selected);

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                this.addSlot((new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, te.getInventory(),
                        y * 3 + x, 80 - 18 + x * 18, 37 - 18 + y * 18, this.getPlayerInventory())));
            }
        }

        this.bindPlayerInventory(ip, 0, 166 - /* height of player inventory */82);
    }

    public void toggleFacadeMode() {
        final CompoundNBT data = this.toolInv.getItemStack().getOrCreateTag();
        data.putBoolean("hideFacades", !data.getBoolean("hideFacades"));
        this.broadcastChanges();
    }

    @Override
    public void broadcastChanges() {
        final ItemStack currentItem = this.getPlayerInv().getSelected();

        if (currentItem != this.toolInv.getItemStack()) {
            if (!currentItem.isEmpty()) {
                if (ItemStack.isSame(this.toolInv.getItemStack(), currentItem)) {
                    this.getPlayerInv().setItem(this.getPlayerInv().selected,
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

        super.broadcastChanges();
    }

    public boolean isFacadeMode() {
        return this.facadeMode;
    }

    private void setFacadeMode(final boolean facadeMode) {
        this.facadeMode = facadeMode;
    }
}
