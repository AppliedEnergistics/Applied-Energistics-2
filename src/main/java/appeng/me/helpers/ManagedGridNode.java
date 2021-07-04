/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.me.helpers;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IConfigurableGridNode;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeHost;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.util.AEColor;
import appeng.api.util.IOrientable;
import appeng.core.Api;
import appeng.core.worlddata.WorldData;
import appeng.hooks.ticking.TickHandler;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.me.cache.StatisticsCache;
import appeng.tile.AEBaseTileEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Manages the lifecycle of a {@link IGridNode}.
 */
@SuppressWarnings("UnusedReturnValue")
public class ManagedGridNode {

    private final IGridNodeHost host;
    private final String nbtName; // name
    private CompoundNBT data = null; // input
    private boolean isReady = false;
    @Nullable
    private IConfigurableGridNode node = null;

    // The following values are used until the node is constructed, and then are applied to the node
    private AEColor gridColor = AEColor.TRANSPARENT;
    private Set<Direction> exposedOnSides = EnumSet.allOf(Direction.class);
    private ItemStack visualRepresentation = ItemStack.EMPTY;
    private EnumSet<GridFlags> flags = EnumSet.noneOf(GridFlags.class);
    private double idlePowerUsage = 1.0;
    private int owner = -1; // ME player id of owner

    public ManagedGridNode(IGridNodeHost host, final String nbtName) {
        this.host = Objects.requireNonNull(host);
        this.nbtName = Objects.requireNonNull(nbtName);
    }

    public void writeToNBT(final CompoundNBT tag) {
        if (this.node != null) {
            this.node.saveToNBT(this.nbtName, tag);
        }
    }

    public void validate() {
        if (this.host instanceof AEBaseTileEntity) {
            TickHandler.instance().addInit((AEBaseTileEntity) this.host);
        }
    }

    public void onChunkUnloaded() {
        this.isReady = false;
        this.remove();
    }

    public void remove() {
        this.isReady = false;
        if (this.node != null) {
            this.node.destroy();
            this.node = null;
        }
    }

    public void onReady() {
        this.isReady = true;

        // send orientation based directionality to the node.
        if (this.host instanceof IOrientable ori) {
            if (ori.canBeRotated()) {
                ori.setOrientation(ori.getForward(), ori.getUp());
            }
        }

        if (this.node == null && !host.getWorld().isRemote()) {
            this.node = Api.instance().grid().createGridNode(host, flags);
            this.node.setGridColor(gridColor);
            this.node.setExposedOnSides(exposedOnSides);
            this.node.setOwner(owner);
            this.node.setIdlePowerUsage(idlePowerUsage);
            this.node.setVisualRepresentation(visualRepresentation);
            this.readFromNBT(this.data);
            this.node.markReady();
            this.node.updateState();
        }
    }

    public IGridNode getNode() {
        return this.node;
    }

    public void readFromNBT(final CompoundNBT tag) {
        this.data = tag;
        if (this.node != null && this.data != null) {
            this.node.loadFromNBT(this.nbtName, this.data);
            this.data = null;
        }
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean isActive() {
        if (this.node == null) {
            return false;
        }

        return this.node.isActive();
    }

    public boolean isPowered() {
        try {
            return this.getEnergy().isNetworkPowered();
        } catch (final GridAccessException e) {
            return false;
        }
    }

    public void setOwner(@Nonnull PlayerEntity player) {
        final GameProfile profile = player.getGameProfile();
        this.owner = WorldData.instance().playerData().getMePlayerId(profile);
        if (node != null) {
            node.setOwner(owner);
        }
    }

    /**
     * short cut!
     *
     * @return grid of node
     * @throws GridAccessException of node or grid is null
     */
    @Nonnull
    public IGrid getGridOrThrow() throws GridAccessException {
        if (this.node == null) {
            throw new GridAccessException();
        }
        final IGrid grid = this.node.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }
        return grid;
    }

    @Nonnull
    public IPathingGrid getPath() throws GridAccessException {
        return this.getGridCache(IPathingGrid.class);
    }

    @Nonnull
    public ITickManager getTick() throws GridAccessException {
        return this.getGridCache(ITickManager.class);
    }

    @Nonnull
    public IStorageGrid getStorage() throws GridAccessException {
        return this.getGridCache(IStorageGrid.class);
    }

    @Nonnull
    public P2PCache getP2P() throws GridAccessException {
        return this.getGridCache(P2PCache.class);
    }

    @Nonnull
    public ISecurityGrid getSecurity() throws GridAccessException {
        return this.getGridCache(ISecurityGrid.class);
    }

    @Nonnull
    public ICraftingGrid getCrafting() throws GridAccessException {
        return this.getGridCache(ICraftingGrid.class);
    }

    @Nonnull
    public StatisticsCache getStatistics() throws GridAccessException {
        return this.getGridCache(StatisticsCache.class);
    }

    @Nonnull
    public IEnergyGrid getEnergy() throws GridAccessException {
        return this.getGridCache(IEnergyGrid.class);
    }

    @Nonnull
    private <T extends IGridCache> T getGridCache(Class<T> clazz) throws GridAccessException {
        return this.getGridOrThrow().getCache(clazz);
    }

    public ManagedGridNode setFlags(final GridFlags... requireChannel) {
        Preconditions.checkState(node == null, "Flags cannot be changed when the node was already created.");
        final EnumSet<GridFlags> flags = EnumSet.noneOf(GridFlags.class);
        Collections.addAll(flags, requireChannel);
        this.flags = flags;
        return this;
    }

    public ManagedGridNode setExposedOnSides(@NotNull Set<Direction> directions) {
        this.exposedOnSides = ImmutableSet.copyOf(directions);
        if (node != null) {
            node.setExposedOnSides(this.exposedOnSides);
        }
        return this;
    }

    public ManagedGridNode setIdlePowerUsage(double usagePerTick) {
        this.idlePowerUsage = usagePerTick;
        if (node != null) {
            node.setIdlePowerUsage(usagePerTick);
        }
        return this;
    }

    public ManagedGridNode setVisualRepresentation(@NotNull ItemStack visualRepresentation) {
        this.visualRepresentation = Objects.requireNonNull(visualRepresentation);
        if (node != null) {
            node.setVisualRepresentation(visualRepresentation);
        }
        return this;
    }

    public ManagedGridNode setGridColor(@NotNull AEColor gridColor) {
        this.gridColor = gridColor;
        if (node != null) {
            node.setGridColor(gridColor);
        }
        return this;
    }

    @Nonnegative
    public double getIdlePowerUsage() {
        return node != null ? node.getIdlePowerUsage() : idlePowerUsage;
    }

    @Nonnull
    public ItemStack getVisualRepresentation() {
        return node != null ? node.getVisualRepresentation() : visualRepresentation;
    }

    @Nonnull
    public AEColor getGridColor() {
        return node != null ? node.getGridColor() : gridColor;
    }
}
