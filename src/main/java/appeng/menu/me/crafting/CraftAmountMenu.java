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

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ISubMenuHost;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfirmAutoCraftPacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.InaccessibleSlot;
import appeng.util.inv.AppEngInternalInventory;

/**
 * @see appeng.client.gui.me.crafting.CraftAmountScreen
 */
public class CraftAmountMenu extends AEBaseMenu implements ISubMenu {

    public static final MenuType<CraftAmountMenu> TYPE = MenuTypeBuilder
            .create(CraftAmountMenu::new, ISubMenuHost.class)
            .build("craftamount");

    /**
     * This slot is used to synchronize a visual representation of what is to be crafted to the client.
     */
    private final AppEngSlot craftingItem;

    /**
     * This item (server-only) indicates what should actually be crafted.
     */
    private AEKey whatToCraft;

    private final ISubMenuHost host;

    public CraftAmountMenu(int id, Inventory ip, ISubMenuHost host) {
        super(TYPE, id, ip, host);
        this.host = host;
        this.craftingItem = new InaccessibleSlot(new AppEngInternalInventory(1), 0);
        this.craftingItem.setHideAmount(true);
        this.addSlot(this.craftingItem, SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public ISubMenuHost getHost() {
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

    public Level getLevel() {
        return this.getPlayerInventory().player.level();
    }

    private void setWhatToCraft(AEKey whatToCraft, int initialAmount) {
        this.whatToCraft = Objects.requireNonNull(whatToCraft, "whatToCraft");
        this.craftingItem.set(GenericStack.wrapInItemStack(whatToCraft, initialAmount));
    }

    /**
     * Confirms the craft request. If called client-side, automatically sends a packet to the server to perform the
     * action there instead.
     *
     * @param amount    The number of items to craft.
     * @param autoStart Start crafting immediately when the planning is done.
     */
    public void confirm(int amount, boolean autoStart) {
        if (!isServerSide()) {
            NetworkHandler.instance().sendToServer(new ConfirmAutoCraftPacket(amount, autoStart));
            return;
        }

        if (this.whatToCraft == null) {
            return;
        }

        var locator = getLocator();
        if (locator != null) {
            Player player = this.getPlayerInventory().player;
            MenuOpener.open(CraftConfirmMenu.TYPE, player, locator);

            if (player.containerMenu instanceof CraftConfirmMenu ccc) {
                ccc.setAutoStart(autoStart);
                ccc.planJob(
                        whatToCraft,
                        amount,
                        CalculationStrategy.REPORT_MISSING_ITEMS);
                broadcastChanges();
            }
        }
    }

    @Nullable
    public GenericStack getWhatToCraft() {
        return GenericStack.unwrapItemStack(craftingItem.getItem());
    }
}
