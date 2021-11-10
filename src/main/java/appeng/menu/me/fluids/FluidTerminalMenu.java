/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.menu.me.fluids;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEFluidKey;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.util.fluid.FluidSoundHelper;

/**
 * @see appeng.client.gui.me.fluids.FluidTerminalScreen
 * @since rv6 12/05/2018
 */
public class FluidTerminalMenu extends MEMonitorableMenu<AEFluidKey> {

    public static final MenuType<FluidTerminalMenu> TYPE = MenuTypeBuilder
            .create(FluidTerminalMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_terminal");

    public FluidTerminalMenu(int id, Inventory ip, ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public FluidTerminalMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory,
                StorageChannels.fluids());
    }

    @Override
    protected boolean hideViewCells() {
        return true;
    }

    @Override
    protected void handleNetworkInteraction(ServerPlayer player, @Nullable AEFluidKey clickedKey,
            InventoryAction action) {

        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            return;
        }

        var fh = ContainerItemContext.ofPlayerCursor(player, this).find(FluidStorage.ITEM);
        if (fh == null) {
            return;
        }

        if (action == InventoryAction.FILL_ITEM && clickedKey != null) {
            // Check how much we can store in the item
            long amountAllowed;
            try (var tx = Transaction.openOuter()) {
                amountAllowed = fh.insert(clickedKey.toVariant(), Long.MAX_VALUE, tx);
                if (amountAllowed == 0) {
                    return; // Nothing.
                }
            }

            // Check if we can pull out of the system
            var canPull = StorageHelper.poweredExtraction(this.powerSource, this.monitor, clickedKey, amountAllowed,
                    this.getActionSource(), Actionable.SIMULATE);
            if (canPull <= 0) {
                return;
            }

            // How much could fit into the carried container
            try (var tx = Transaction.openOuter()) {
                long canFill = fh.insert(clickedKey.toVariant(), canPull, tx);
                if (canFill == 0) {
                    return;
                }

                // Now actually pull out of the system
                var inserted = StorageHelper.poweredExtraction(this.powerSource, this.monitor, clickedKey, canFill,
                        this.getActionSource());
                if (inserted <= 0) {
                    // Something went wrong
                    AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
                    return;
                }

                tx.commit();
            }

            FluidSoundHelper.playFillSound(player, clickedKey.toVariant());
        } else if (action == InventoryAction.EMPTY_ITEM) {
            // See how much we can drain from the item
            var content = StorageUtil.findExtractableContent(fh, null);
            if (content == null) {
                return;
            }

            var what = AEFluidKey.of(content.resource());
            var amount = content.amount();

            // Check if we can push into the system
            var canInsert = StorageHelper.poweredInsert(this.powerSource, this.monitor, what, amount,
                    this.getActionSource(), Actionable.SIMULATE);

            // Actually drain
            try (var tx = Transaction.openOuter()) {
                var extracted = fh.extract(what.toVariant(), canInsert, tx);
                if (extracted != canInsert) {
                    AELog.error(
                            "Fluid item [%s] reported a different possible amount to drain than it actually provided.",
                            getCarried());
                    return;
                }

                if (StorageHelper.poweredInsert(this.powerSource, this.monitor, what,
                        extracted, this.getActionSource()) != extracted) {
                    AELog.error("Failed to insert previously simulated %s into ME system", what);
                    return;
                }

                tx.commit();
            }

            FluidSoundHelper.playEmptySound(player, what.toVariant());
        }
    }

}
