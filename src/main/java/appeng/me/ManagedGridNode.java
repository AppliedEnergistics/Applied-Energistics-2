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

package appeng.me;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MutableClassToInstanceMap;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IGridNodeService;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AEColor;
import appeng.core.worlddata.WorldData;

/**
 * Manages the lifecycle of a {@link IGridNode}.
 */
@SuppressWarnings("UnusedReturnValue")
public class ManagedGridNode implements IManagedGridNode {
    private static class InitData<T> {
        private final T logicalHost;
        private final IGridNodeListener<T> listener;
        public ClassToInstanceMap<IGridNodeService> services;
        private CompoundNBT data = null;

        // The following values are used until the node is constructed, and then are applied to the node
        private AEColor gridColor = AEColor.TRANSPARENT;
        private Set<Direction> exposedOnSides = EnumSet.allOf(Direction.class);
        private ItemStack visualRepresentation = ItemStack.EMPTY;
        private EnumSet<GridFlags> flags = EnumSet.noneOf(GridFlags.class);
        private double idlePowerUsage = 1.0;
        private int owner = -1; // ME player id of owner
        private World world;
        private BlockPos pos;
        private boolean inWorldNode;

        public InitData(T logicalHost, IGridNodeListener<T> listener) {
            this.logicalHost = Objects.requireNonNull(logicalHost);
            this.listener = Objects.requireNonNull(listener);
        }

        public GridNode createNode() {
            GridNode node;
            if (inWorldNode) {
                Preconditions.checkState(pos != null, "No position was set for an in-world node");
                node = new InWorldGridNode((ServerWorld) world, pos, logicalHost, listener, flags);
                node.setExposedOnSides(exposedOnSides);
            } else {
                node = new GridNode((ServerWorld) world, logicalHost, listener, flags);
            }
            node.setGridColor(gridColor);
            node.setOwningPlayerId(owner);
            node.setIdlePowerUsage(idlePowerUsage);
            node.setVisualRepresentation(visualRepresentation);
            if (services != null) {
                for (var serviceClass : services.keySet()) {
                    addService(node, serviceClass);
                }
            }
            return node;
        }

        private <SC extends IGridNodeService> void addService(GridNode node, Class<SC> serviceClass) {
            node.addService(serviceClass, services.getInstance(serviceClass));
        }
    }

    /**
     * This will be set in the constructor and initialization data will be aggregated in this field until the node is
     * finally constructed. At that point, this field will be set to null to clear the memory.
     */
    @Nullable
    private InitData<?> initData;

    /**
     * The name of the NBT sub-tag used to store the managed node's data in the host.
     */
    private String tagName = "gn";

    @Nullable
    private GridNode node = null;

    public <T> ManagedGridNode(T nodeOwner, IGridNodeListener<? super T> listener) {
        this.initData = new InitData<>(nodeOwner, listener);
    }

    @Override
    public ManagedGridNode setInWorldNode(boolean accessible) {
        getInitData().inWorldNode = accessible;
        return this;
    }

    /**
     * Changes the name of the NBT subtag in the host's NBT data that this node's data will be stored as.
     */
    @Override
    public ManagedGridNode setTagName(String tagName) {
        if (getInitData().data != null) {
            throw new IllegalStateException("Cannot change tag name after NBT has already been read.");
        }
        this.tagName = Objects.requireNonNull(tagName);
        return this;
    }

    @Override
    public void onChunkUnloaded() {
        this.destroy();
    }

    @Override
    public void destroy() {
        if (this.node != null) {
            this.node.destroy();
            this.node = null;
        }
    }

    @Override
    public void create(World world, @Nullable BlockPos blockPos) {
        // We can only ready up if the init-data still exists
        var initData = getInitData();
        initData.world = world;
        initData.pos = blockPos;
        this.initData = null;

        if (this.node == null && !initData.world.isRemote()) {
            createNode(initData);
        }
    }

    private void createNode(InitData<?> initData) {
        Preconditions.checkState(node == null);

        var node = initData.createNode();
        if (initData.data != null) {
            node.loadFromNBT(this.tagName, initData.data);
        }
        // The field has to be set before markReady is called! Otherwise the grid will reach back into a null-pointer
        // during it's creation.
        this.node = node;
        this.node.markReady();
    }

    public IGridNode getNode() {
        return this.node;
    }

    public void loadFromNBT(CompoundNBT tag) {
        if (node == null) {
            getInitData().data = tag;
        } else {
            this.node.loadFromNBT(this.tagName, tag);
        }
    }

    @Override
    public void saveToNBT(CompoundNBT tag) {
        if (this.node != null) {
            this.node.saveToNBT(this.tagName, tag);
        }
    }

    @Override
    public boolean isReady() {
        return initData == null && node != null;
    }

    /**
     * @see IGridNode#isActive()
     */
    @Override
    public boolean isActive() {
        if (this.node == null) {
            return false;
        }

        return this.node.isActive();
    }

    @Override
    public boolean isPowered() {
        var grid = getGrid();
        return grid != null && grid.getEnergyService().isNetworkPowered();
    }

    @Override
    public void setOwningPlayerId(int ownerPlayerId) {
        if (this.initData != null) {
            getInitData().owner = ownerPlayerId;
        } else {
            if (node != null) {
                node.setOwningPlayerId(ownerPlayerId);
            }
        }
    }

    @Override
    public void setOwningPlayer(@Nonnull PlayerEntity player) {
        var playerId = WorldData.instance().playerData().getMePlayerId(player.getGameProfile());
        setOwningPlayerId(playerId);
    }

    @Override
    public ManagedGridNode setFlags(final GridFlags... requireChannel) {
        var flags = EnumSet.noneOf(GridFlags.class);
        Collections.addAll(flags, requireChannel);
        getInitData().flags = flags;
        return this;
    }

    public ManagedGridNode setExposedOnSides(@NotNull Set<Direction> directions) {
        if (node == null) {
            getInitData().exposedOnSides = ImmutableSet.copyOf(directions);
        } else {
            node.setExposedOnSides(directions);
        }
        return this;
    }

    public ManagedGridNode setIdlePowerUsage(double usagePerTick) {
        if (node == null) {
            getInitData().idlePowerUsage = usagePerTick;
        } else {
            node.setIdlePowerUsage(usagePerTick);
        }
        return this;
    }

    public ManagedGridNode setVisualRepresentation(@NotNull ItemStack visualRepresentation) {
        if (node == null) {
            getInitData().visualRepresentation = Objects.requireNonNull(visualRepresentation);
        } else {
            node.setVisualRepresentation(visualRepresentation);
        }
        return this;
    }

    public ManagedGridNode setGridColor(@NotNull AEColor gridColor) {
        if (this.node == null) {
            getInitData().gridColor = gridColor;
        } else {
            node.setGridColor(gridColor);
        }
        return this;
    }

    @Nonnegative
    public double getIdlePowerUsage() {
        return node != null ? node.getIdlePowerUsage() : getInitData().idlePowerUsage;
    }

    @Nonnull
    public ItemStack getVisualRepresentation() {
        return node != null ? node.getVisualRepresentation() : getInitData().visualRepresentation;
    }

    @Nonnull
    private InitData<?> getInitData() {
        if (initData == null) {
            throw new IllegalStateException(
                    "The node has already been initialized. Initialization data cannot be changed anymore.");
        }
        return initData;
    }

    @Override
    public <T extends IGridNodeService> ManagedGridNode addService(Class<T> serviceClass, T service) {
        var initData = getInitData();
        if (initData.services == null) {
            initData.services = MutableClassToInstanceMap.create();
        }
        initData.services.putInstance(serviceClass, service);
        return this;
    }

    public AEColor getGridColor() {
        if (node == null) {
            return getInitData().gridColor;
        } else {
            return node.getGridColor();
        }
    }

}
