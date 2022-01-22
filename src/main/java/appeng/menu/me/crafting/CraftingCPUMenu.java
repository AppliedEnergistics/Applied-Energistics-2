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

import java.util.function.Consumer;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.CpuSelectionMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.IActionHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.sync.packets.CraftingStatusPacket;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.IncrementalUpdateHelper;

/**
 * @see appeng.client.gui.me.crafting.CraftingCPUScreen
 */
public class CraftingCPUMenu extends AEBaseMenu {

    private static final String ACTION_CANCEL_CRAFTING = "cancelCrafting";

    public static final MenuType<CraftingCPUMenu> TYPE = MenuTypeBuilder
            .create(CraftingCPUMenu::new, CraftingBlockEntity.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .withMenuTitle(craftingBlockEntity -> {
                // Use the cluster's custom name instead of the right-clicked block entities one
                CraftingCPUCluster cluster = craftingBlockEntity.getCluster();
                if (cluster != null && cluster.getName() != null) {
                    return cluster.getName();
                }
                return TextComponent.EMPTY;
            })
            .build("craftingcpu");

    private final IncrementalUpdateHelper incrementalUpdateHelper = new IncrementalUpdateHelper();
    private final IGrid grid;
    private CraftingCPUCluster cpu = null;
    private final Consumer<AEKey> cpuChangeListener = incrementalUpdateHelper::addChange;

    @GuiSync(0)
    public CpuSelectionMode schedulingMode = CpuSelectionMode.ANY;

    public CraftingCPUMenu(MenuType<?> menuType, int id, Inventory ip, Object te) {
        super(menuType, id, ip, te);
        final IActionHost host = (IActionHost) (te instanceof IActionHost ? te : null);

        if (host != null && host.getActionableNode() != null) {
            this.grid = host.getActionableNode().getGrid();
        } else {
            this.grid = null;
        }

        if (te instanceof CraftingBlockEntity) {
            this.setCPU(((CraftingBlockEntity) te).getCluster());
        }

        if (this.getGrid() == null && isServerSide()) {
            this.setValidMenu(false);
        }

        registerClientAction(ACTION_CANCEL_CRAFTING, this::cancelCrafting);
    }

    protected void setCPU(ICraftingCPU c) {
        if (c == this.cpu) {
            return;
        }

        if (this.cpu != null) {
            this.cpu.craftingLogic.removeListener(cpuChangeListener);
        }

        this.incrementalUpdateHelper.reset();

        if (c instanceof CraftingCPUCluster) {
            this.cpu = (CraftingCPUCluster) c;

            // Initially send all items as a full-update to the client when the CPU changes
            var allItems = new KeyCounter();
            cpu.craftingLogic.getAllItems(allItems);
            for (var entry : allItems) {
                incrementalUpdateHelper.addChange(entry.getKey());
            }

            this.cpu.craftingLogic.addListener(cpuChangeListener);
        } else {
            this.cpu = null;
            // Clear the crafting status
            sendPacketToClient(new CraftingStatusPacket(CraftingStatus.EMPTY));
        }
    }

    public void cancelCrafting() {
        if (isClientSide()) {
            sendClientAction(ACTION_CANCEL_CRAFTING);
        } else {
            if (this.cpu != null) {
                this.cpu.cancel();
            }
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.cpu != null) {
            this.cpu.craftingLogic.removeListener(cpuChangeListener);
        }
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide() && this.cpu != null) {
            this.schedulingMode = this.cpu.getSelectionMode();

            if (this.incrementalUpdateHelper.hasChanges()) {
                CraftingStatus status = CraftingStatus.create(this.incrementalUpdateHelper, this.cpu.craftingLogic);
                this.incrementalUpdateHelper.commitChanges();

                sendPacketToClient(new CraftingStatusPacket(status));
            }
        }

        super.broadcastChanges();
    }

    public CpuSelectionMode getSchedulingMode() {
        return schedulingMode;
    }

    public boolean allowConfiguration() {
        return true;
    }

    IGrid getGrid() {
        return this.grid;
    }

}
