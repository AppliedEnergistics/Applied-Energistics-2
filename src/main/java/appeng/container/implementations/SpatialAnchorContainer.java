/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.collect.Multiset;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.me.GridAccessException;
import appeng.me.cache.StatisticsCache;
import appeng.tile.spatial.SpatialAnchorTileEntity;
import appeng.util.item.AEItemStack;

public class SpatialAnchorContainer extends AEBaseContainer {

    public static ContainerType<SpatialAnchorContainer> TYPE;

    private static final ContainerHelper<SpatialAnchorContainer, SpatialAnchorTileEntity> helper = new ContainerHelper<>(
            SpatialAnchorContainer::new, SpatialAnchorTileEntity.class, SecurityPermissions.BUILD);

    private IGrid network;
    private int delay = 40;

    @GuiSync(0)
    public long powerConsumption;
    @GuiSync(1)
    public int loadedChunks;
    @GuiSync(2)
    public YesNo overlayMode = YesNo.NO;

    @GuiSync(10)
    public int allLoadedWorlds;
    @GuiSync(11)
    public int allLoadedChunks;

    @GuiSync(20)
    public int allWorlds;
    @GuiSync(21)
    public int allChunks;

    public SpatialAnchorContainer(int id, final PlayerInventory ip, final SpatialAnchorTileEntity spatialAnchor) {
        super(TYPE, id, ip, spatialAnchor, null);

        if (isServer()) {
            this.network = spatialAnchor.getGridNode(AEPartLocation.INTERNAL).getGrid();
        }
    }

    public static SpatialAnchorContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (isServer()) {
            SpatialAnchorTileEntity anchor = (SpatialAnchorTileEntity) this.getTileEntity();
            this.setOverlayMode((YesNo) anchor.getConfigManager().getSetting(Settings.OVERLAY_MODE));

            this.delay++;
            if (this.delay > 15 && this.network != null) {
                StatisticsCache statistics = this.network.getCache(StatisticsCache.class);

                this.powerConsumption = (long) anchor.getProxy().getIdlePowerUsage();
                this.loadedChunks = ((SpatialAnchorTileEntity) this.getTileEntity()).countLoadedChunks();

                try {
                    HashMap<World, Integer> stats = new HashMap<>();
                    IMachineSet anchors = anchor.getProxy().getGrid().getMachines(SpatialAnchorTileEntity.class);

                    for (IGridNode machine : anchors) {
                        SpatialAnchorTileEntity a = (SpatialAnchorTileEntity) machine.getMachine();
                        World world = machine.getGridBlock().getLocation().getWorld();
                        stats.merge(world, a.countLoadedChunks(), (left, right) -> Math.max(left, right));
                    }

                    this.allLoadedChunks = stats.values().stream().reduce((left, right) -> left + right).orElse(0);
                    this.allLoadedWorlds = stats.keySet().size();

                } catch (GridAccessException e) {
                }

                this.allWorlds = statistics.getChunks().size();
                this.allChunks = 0;
                for (Entry<IWorld, Multiset<ChunkPos>> entry : statistics.getChunks().entrySet()) {
                    this.allChunks += entry.getValue().elementSet().size();
                }

                try {
                    final MEInventoryUpdatePacket piu = new MEInventoryUpdatePacket();

                    final IItemList<IAEItemStack> list = Api.instance().storage()
                            .getStorageChannel(IItemStorageChannel.class).createList();

                    for (final ChunkPos chunk : anchor.getLoadedChunks()) {
                        final ItemStack is = anchor.getProxy().getMachineRepresentation();
                        if (!is.isEmpty()) {
                            is.getOrCreateTag().putLong("chunk", chunk.asLong());
                            final IAEItemStack ais = AEItemStack.fromItemStack(is);
                            ais.setStackSize(chunk.asLong());
                            list.add(ais);
                        }

                        for (final IAEItemStack ais : list) {
                            piu.appendItem(ais);
                        }
                    }

                    for (final Object c : this.listeners) {
                        if (c instanceof PlayerEntity) {
                            NetworkHandler.instance().sendTo(piu, (ServerPlayerEntity) c);
                        }
                    }
                } catch (final IOException e) {
                    // :P
                }

                this.delay = 0;
            }
        }

        super.detectAndSendChanges();
    }

    protected void loadSettingsFromHost(final IConfigManager cm) {
        this.setOverlayMode((YesNo) cm.getSetting(Settings.OVERLAY_MODE));
    }

    public YesNo getOverlayMode() {
        return this.overlayMode;
    }

    public void setOverlayMode(final YesNo mode) {
        this.overlayMode = mode;
    }
}
