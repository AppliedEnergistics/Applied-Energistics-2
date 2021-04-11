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

package appeng.container.me.crafting;

import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerHelper;
import appeng.container.slot.InaccessibleSlot;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfirmAutoCraftPacket;
import appeng.me.helpers.PlayerSource;
import appeng.tile.inventory.AppEngInternalInventory;

/**
 * @see appeng.client.gui.me.crafting.CraftAmountScreen
 */
public class CraftAmountContainer extends AEBaseContainer {

    public static ContainerType<CraftAmountContainer> TYPE;

    private static final ContainerHelper<CraftAmountContainer, ITerminalHost> helper = new ContainerHelper<>(
            CraftAmountContainer::new, ITerminalHost.class, SecurityPermissions.CRAFT);

    /**
     * This slot is used to synchronize a visual representation of what is to be crafted to the client.
     */
    private final Slot craftingItem;

    /**
     * This item (server-only) indicates what should actually be crafted.
     */
    private IAEItemStack itemToCreate;

    public CraftAmountContainer(int id, PlayerInventory ip, final ITerminalHost te) {
        super(TYPE, id, ip, te);

        this.craftingItem = new InaccessibleSlot(new AppEngInternalInventory(null, 1), 0, 34, 53);
        this.addSlot(this.craftingItem);
    }

    public static CraftAmountContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    public IGrid getGrid() {
        final IActionHost h = ((IActionHost) this.getTarget());
        return h.getActionableNode().getGrid();
    }

    public World getWorld() {
        return this.getPlayerInventory().player.world;
    }

    public IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInventory().player, (IActionHost) this.getTarget());
    }

    public void setItemToCraft(@Nonnull final IAEItemStack itemToCreate) {
        // Make a copy because this stack will be modified with the requested amount
        this.itemToCreate = itemToCreate.copy();
        this.craftingItem.putStack(itemToCreate.asItemStackRepresentation());
    }

    /**
     * Confirms the craft request. If called client-side, automatically sends a packet to the server to perform the
     * action there instead.
     *
     * @param amount    The number of items to craft.
     * @param autoStart Start crafting immediately when the planning is done.
     */
    public void confirm(int amount, boolean autoStart) {
        if (!isServer()) {
            NetworkHandler.instance().sendToServer(new ConfirmAutoCraftPacket(amount, autoStart));
            return;
        }

        final Object target = getTarget();
        if (target instanceof IActionHost) {
            final IActionHost ah = (IActionHost) target;
            final IGridNode gn = ah.getActionableNode();
            if (gn == null) {
                return;
            }

            final IGrid g = gn.getGrid();
            if (g == null || this.itemToCreate == null) {
                return;
            }

            this.itemToCreate.setStackSize(amount);

            Future<ICraftingJob> futureJob = null;
            try {
                final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
                futureJob = cg.beginCraftingJob(getWorld(), getGrid(), getActionSrc(),
                        this.itemToCreate, null);

                final ContainerLocator locator = getLocator();
                if (locator != null) {
                    PlayerEntity player = this.getPlayerInventory().player;
                    ContainerOpener.openContainer(CraftConfirmContainer.TYPE, player, locator);

                    if (player.openContainer instanceof CraftConfirmContainer) {
                        final CraftConfirmContainer ccc = (CraftConfirmContainer) player.openContainer;
                        ccc.setAutoStart(autoStart);
                        ccc.setJob(futureJob);
                        detectAndSendChanges();
                    }
                }
            } catch (final Throwable e) {
                if (futureJob != null) {
                    futureJob.cancel(true);
                }
                AELog.info(e);
            }
        }

    }
}
