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

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.menu.AEBaseContainer;
import appeng.menu.implementations.ContainerTypeBuilder;
import appeng.menu.me.common.IncrementalUpdateHelper;
import appeng.core.Api;
import appeng.core.sync.packets.CraftingStatusPacket;
import appeng.me.cluster.implementations.CraftingCPUCluster;

/**
 * @see appeng.client.gui.me.crafting.CraftingCPUScreen
 */
public class CraftingCPUContainer extends AEBaseContainer implements IMEMonitorHandlerReceiver<IAEItemStack> {

    public static final MenuType<CraftingCPUContainer> TYPE = ContainerTypeBuilder
            .create(CraftingCPUContainer::new, CraftingBlockEntity.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .withContainerTitle(craftingBlockEntity -> {
                // Use the cluster's custom name instead of the right-clicked block entities one
                CraftingCPUCluster cluster = craftingBlockEntity.getCluster();
                if (cluster != null && cluster.getName() != null) {
                    return cluster.getName();
                }
                return TextComponent.EMPTY;
            })
            .build("craftingcpu");

    private final IncrementalUpdateHelper<IAEItemStack> incrementalUpdateHelper = new IncrementalUpdateHelper<>();
    private final IGrid grid;
    private CraftingCPUCluster cpu = null;

    public CraftingCPUContainer(MenuType<?> containerType, int id, final Inventory ip, final Object te) {
        super(containerType, id, ip, te);
        final IActionHost host = (IActionHost) (te instanceof IActionHost ? te : null);

        if (host != null && host.getActionableNode() != null) {
            this.grid = host.getActionableNode().getGrid();
        } else {
            this.grid = null;
        }

        if (te instanceof CraftingBlockEntity) {
            this.setCPU(((CraftingBlockEntity) te).getCluster());
        }

        if (this.getGrid() == null && isServer()) {
            this.setValidContainer(false);
        }
    }

    protected void setCPU(final ICraftingCPU c) {
        if (c == this.cpu) {
            return;
        }

        if (this.cpu != null) {
            this.cpu.removeListener(this);
        }

        this.incrementalUpdateHelper.clear();

        if (c instanceof CraftingCPUCluster) {
            this.cpu = (CraftingCPUCluster) c;

            // Initially send all items as a full-update to the client when the CPU changes
            IItemList<IAEItemStack> allItems = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                    .createList();
            this.cpu.getListOfItem(allItems, CraftingItemList.ALL);
            for (IAEItemStack stack : allItems) {
                incrementalUpdateHelper.addChange(stack);
            }

            this.cpu.addListener(this, null);
        } else {
            this.cpu = null;
            // Clear the crafting status
            sendPacketToClient(new CraftingStatusPacket(CraftingStatus.EMPTY));
        }
    }

    public void cancelCrafting() {
        if (this.cpu != null) {
            this.cpu.cancel();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (this.cpu != null) {
            this.cpu.removeListener(this);
        }
    }

    @Override
    public void broadcastChanges() {
        if (isServer() && this.cpu != null && this.incrementalUpdateHelper.hasChanges()) {
            CraftingStatus status = CraftingStatus.create(this.incrementalUpdateHelper, this.cpu);
            this.incrementalUpdateHelper.commitChanges();

            sendPacketToClient(new CraftingStatusPacket(status));
        }
        super.broadcastChanges();
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change,
            final IActionSource actionSource) {
        for (IAEItemStack is : change) {
            this.incrementalUpdateHelper.addChange(is);
        }
    }

    @Override
    public void onListUpdate() {

    }

    IGrid getGrid() {
        return this.grid;
    }

}
