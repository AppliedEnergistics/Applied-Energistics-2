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
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.slot.InaccessibleSlot;
import appeng.me.helpers.PlayerSource;
import appeng.tile.inventory.AppEngInternalInventory;

public class CraftAmountContainer extends AEBaseContainer {

    public static ContainerType<CraftAmountContainer> TYPE;

    private static final ContainerHelper<CraftAmountContainer, ITerminalHost> helper = new ContainerHelper<>(
            CraftAmountContainer::new, ITerminalHost.class, SecurityPermissions.CRAFT);

    private final Slot craftingItem;
    private IAEItemStack itemToCreate;

    public CraftAmountContainer(int id, PlayerInventory ip, final ITerminalHost te) {
        super(TYPE, id, ip, te);

        this.craftingItem = new InaccessibleSlot(new AppEngInternalInventory(null, 1), 0, 34, 53);
        this.addSlot(this.getCraftingItem());
    }

    public static CraftAmountContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    public IGrid getGrid() {
        final IActionHost h = ((IActionHost) this.getTarget());
        return h.getActionableNode().getGrid();
    }

    public World getWorld() {
        return this.getPlayerInv().player.level;
    }

    public IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInv().player, (IActionHost) this.getTarget());
    }

    public Slot getCraftingItem() {
        return this.craftingItem;
    }

    public IAEItemStack getItemToCraft() {
        return this.itemToCreate;
    }

    public void setItemToCraft(@Nonnull final IAEItemStack itemToCreate) {
        this.itemToCreate = itemToCreate;
    }
}
