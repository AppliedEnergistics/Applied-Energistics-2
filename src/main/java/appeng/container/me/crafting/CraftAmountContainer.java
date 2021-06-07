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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
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
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerTypeBuilder;
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

    public static final ContainerType<CraftAmountContainer> TYPE = ContainerTypeBuilder
            .create(CraftAmountContainer::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("craftamount");

    /**
     * This slot is used to synchronize a visual representation of what is to be crafted to the client.
     */
    private final Slot craftingItem;

    /**
     * This item (server-only) indicates what should actually be crafted.
     */
    private IAEItemStack itemToCreate;

    @GuiSync(1)
    private int initialAmount = -1;

    public CraftAmountContainer(int id, PlayerInventory ip, final ITerminalHost te) {
        super(TYPE, id, ip, te);

        this.craftingItem = new InaccessibleSlot(new AppEngInternalInventory(null, 1), 0);
        this.addSlot(this.craftingItem, SlotSemantic.MACHINE_OUTPUT);
    }

    /**
     * Opens the craft amount screen for the given player.
     */
    public static void open(ServerPlayerEntity player, ContainerLocator locator, IAEItemStack itemToCraft,
            int initialAmount) {
        ContainerOpener.openContainer(CraftAmountContainer.TYPE, player, locator);

        if (player.openContainer instanceof CraftAmountContainer) {
            CraftAmountContainer cca = (CraftAmountContainer) player.openContainer;
            cca.setItemToCraft(itemToCraft, initialAmount);
            cca.detectAndSendChanges();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    public IGrid getGrid() {
        final IActionHost h = (IActionHost) this.getTarget();
        return h.getActionableNode().getGrid();
    }

    public World getWorld() {
        return this.getPlayerInventory().player.world;
    }

    public IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInventory().player, (IActionHost) this.getTarget());
    }

    private void setItemToCraft(@Nonnull final IAEItemStack itemToCreate, int initialAmount) {
        // Make a copy because this stack will be modified with the requested amount
        this.itemToCreate = itemToCreate.copy();
        this.initialAmount = initialAmount;
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
                        ccc.setItemToCreate(this.itemToCreate.copy());
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

    public int getInitialAmount() {
        return initialAmount;
    }
}
