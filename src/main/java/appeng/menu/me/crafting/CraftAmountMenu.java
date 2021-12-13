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

package appeng.menu.me.crafting;

import java.util.Objects;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.ITerminalHost;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfirmAutoCraftPacket;
import appeng.me.helpers.PlayerSource;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;

/**
 * @see appeng.client.gui.me.crafting.CraftAmountScreen
 */
public class CraftAmountMenu extends AEBaseMenu implements ISubMenu {

    public static final MenuType<CraftAmountMenu> TYPE = MenuTypeBuilder
            .create(CraftAmountMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("craftamount");

    /**
     * This slot is used to synchronize a visual representation of what is to be crafted to the client.
     */
    private final Slot craftingItem;

    /**
     * This item (server-only) indicates what should actually be crafted.
     */
    private AEKey whatToCraft;

    @GuiSync(1)
    private int initialAmount = -1;

    private final ITerminalHost host;

    public CraftAmountMenu(int id, Inventory ip, ITerminalHost host) {
        super(TYPE, id, ip, host);
        this.host = host;
        this.craftingItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        this.addSlot(this.craftingItem, SlotSemantic.MACHINE_OUTPUT);
    }

    @Override
    public ITerminalHost getHost() {
        return host;
    }

    /**
     * Opens the craft amount screen for the given player.
     */
    public static void open(ServerPlayer player, MenuLocator locator, AEKey whatToCraft, int initialAmount) {
        MenuOpener.open(CraftAmountMenu.TYPE, player, locator);

        if (player.containerMenu instanceof CraftAmountMenu cca) {
            cca.setWhatToCraft(whatToCraft, initialAmount);
            cca.broadcastChanges();
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    public Level getLevel() {
        return this.getPlayerInventory().player.level;
    }

    public IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInventory().player, (IActionHost) this.getTarget());
    }

    private void setWhatToCraft(@Nonnull AEKey whatToCraft, int initialAmount) {
        this.whatToCraft = Objects.requireNonNull(whatToCraft, "whatToCraft");
        this.initialAmount = initialAmount;
        this.craftingItem.set(whatToCraft.wrapForDisplayOrFilter());
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
        if (target instanceof IActionHost ah) {
            final IGridNode gn = ah.getActionableNode();
            if (gn == null) {
                return;
            }

            final IGrid g = gn.getGrid();
            if (this.whatToCraft == null) {
                return;
            }

            Future<ICraftingPlan> futureJob = null;
            try {
                var cg = g.getCraftingService();
                var actionSource = getActionSrc();
                futureJob = cg.beginCraftingCalculation(getLevel(), () -> actionSource, whatToCraft, amount);

                var locator = getLocator();
                if (locator != null) {
                    Player player = this.getPlayerInventory().player;
                    MenuOpener.open(CraftConfirmMenu.TYPE, player, locator);

                    if (player.containerMenu instanceof CraftConfirmMenu ccc) {
                        ccc.setAutoStart(autoStart);
                        ccc.setWhatToCraft(this.whatToCraft, amount);
                        ccc.setJob(futureJob);
                        broadcastChanges();
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
