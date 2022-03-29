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

package appeng.blockentity.crafting;

import java.util.*;

import com.google.common.collect.Iterators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.block.crafting.AbstractCraftingUnitBlock.CraftingUnitType;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.CraftingCPUCalculator;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.NullConfigManager;
import appeng.util.Platform;
import appeng.util.iterators.ChainedIterator;

public class CraftingBlockEntity extends AENetworkBlockEntity
        implements IAEMultiBlock<CraftingCPUCluster>, IPowerChannelState, IConfigurableObject {

    private final CraftingCPUCalculator calc = new CraftingCPUCalculator(this);
    private CompoundTag previousState = null;
    private boolean isCoreBlock = false;
    private CraftingCPUCluster cluster;

    public CraftingBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setFlags(GridFlags.MULTIBLOCK, GridFlags.REQUIRE_CHANNEL)
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .addService(IGridMultiblock.class, this::getMultiblockNodes);
    }

    @Override
    protected Item getItemFromBlockEntity() {
        if (isAccelerator()) {
            return AEBlocks.CRAFTING_ACCELERATOR.asItem();
        } else {
            return AEBlocks.CRAFTING_UNIT.asItem();
        }
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        if (this.cluster != null) {
            this.cluster.updateName();
        }
    }

    public boolean isAccelerator() {
        if (this.level == null) {
            return false;
        }

        final AbstractCraftingUnitBlock<?> unit = (AbstractCraftingUnitBlock<?>) this.level
                .getBlockState(this.worldPosition)
                .getBlock();
        return unit.type == CraftingUnitType.ACCELERATOR;
    }

    @Override
    public void onReady() {
        super.onReady();
        this.getMainNode().setVisualRepresentation(this.getItemFromBlockEntity());
        if (level instanceof ServerLevel serverLevel) {
            this.calc.calculateMultiblock(serverLevel, worldPosition);
        }
    }

    public void updateMultiBlock(BlockPos changedPos) {
        if (level instanceof ServerLevel serverLevel) {
            this.calc.updateMultiblockAfterNeighborUpdate(serverLevel, worldPosition, changedPos);
        }
    }

    public void updateStatus(CraftingCPUCluster c) {
        if (this.cluster != null && this.cluster != c) {
            this.cluster.breakCluster();
        }

        this.cluster = c;
        this.updateSubType(true);
    }

    public void updateSubType(boolean updateFormed) {
        if (this.level == null || this.notLoaded() || this.isRemoved()) {
            return;
        }

        final boolean formed = this.isFormed();
        boolean power = false;

        if (this.getMainNode().isReady()) {
            power = this.getMainNode().isActive();
        }

        final BlockState current = this.level.getBlockState(this.worldPosition);

        // The block entity might try to update while being destroyed
        if (current.getBlock() instanceof AbstractCraftingUnitBlock) {
            final BlockState newState = current.setValue(AbstractCraftingUnitBlock.POWERED, power)
                    .setValue(AbstractCraftingUnitBlock.FORMED, formed);

            if (current != newState) {
                // Not using flag 2 here (only send to clients, prevent block update) will cause
                // infinite loops
                // In case there is an inconsistency in the crafting clusters.
                this.level.setBlock(this.worldPosition, newState, 2);
            }
        }

        if (updateFormed) {
            if (formed) {
                this.getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
            } else {
                this.getMainNode().setExposedOnSides(EnumSet.noneOf(Direction.class));
            }
        }
    }

    public boolean isFormed() {
        if (isClientSide()) {
            return this.level.getBlockState(this.worldPosition).getValue(AbstractCraftingUnitBlock.FORMED);
        }
        return this.cluster != null;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putBoolean("core", this.isCoreBlock());
        if (this.isCoreBlock() && this.cluster != null) {
            this.cluster.writeToNBT(data);
        }
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.setCoreBlock(data.getBoolean("core"));
        if (this.isCoreBlock()) {
            if (this.cluster != null) {
                this.cluster.readFromNBT(data);
            } else {
                this.setPreviousState(data.copy());
            }
        }
    }

    @Override
    public void disconnect(boolean update) {
        if (this.cluster != null) {
            this.cluster.destroy();
            if (update) {
                this.updateSubType(true);
            }
        }
    }

    @Override
    public CraftingCPUCluster getCluster() {
        return this.cluster;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.updateSubType(false);
    }

    public boolean isStatus() {
        return false;
    }

    public boolean isStorage() {
        return false;
    }

    public int getStorageBytes() {
        return 0;
    }

    public void breakCluster() {
        if (this.cluster != null) {
            this.cluster.cancel();
            var inv = this.cluster.craftingLogic.getInventory();

            // Drop stacks
            var stacks = new ArrayList<ItemStack>();

            for (var entry : inv.list) {
                entry.getKey().addDrops(entry.getLongValue(), stacks, this.level, this.worldPosition);
            }

            Platform.spawnDrops(this.level, worldPosition, stacks);

            this.cluster.destroy();
        }
    }

    @Override
    public boolean isPowered() {
        if (isClientSide()) {
            return this.level.getBlockState(this.worldPosition).getValue(AbstractCraftingUnitBlock.POWERED);
        }
        return this.getMainNode().isActive();
    }

    @Override
    public boolean isActive() {
        if (!isClientSide()) {
            return this.getMainNode().isActive();
        }
        return this.isPowered() && this.isFormed();
    }

    public boolean isCoreBlock() {
        return this.isCoreBlock;
    }

    public void setCoreBlock(boolean isCoreBlock) {
        this.isCoreBlock = isCoreBlock;
    }

    public CompoundTag getPreviousState() {
        return this.previousState;
    }

    public void setPreviousState(CompoundTag previousState) {
        this.previousState = previousState;
    }

    @Override
    public Object getRenderAttachmentData() {
        return new CraftingCubeModelData(getUp(), getForward(), getConnections());
    }

    protected EnumSet<Direction> getConnections() {
        if (level == null) {
            return EnumSet.noneOf(Direction.class);
        }

        EnumSet<Direction> connections = EnumSet.noneOf(Direction.class);

        for (Direction facing : Direction.values()) {
            if (this.isConnected(level, worldPosition, facing)) {
                connections.add(facing);
            }
        }

        return connections;
    }

    private boolean isConnected(BlockGetter level, BlockPos pos, Direction side) {
        BlockPos adjacentPos = pos.relative(side);
        return level.getBlockState(adjacentPos).getBlock() instanceof AbstractCraftingUnitBlock;
    }

    private Iterator<IGridNode> getMultiblockNodes() {
        if (this.getCluster() == null) {
            return new ChainedIterator<>();
        }
        return Iterators.transform(this.getCluster().getBlockEntities(), CraftingBlockEntity::getGridNode);
    }

    @Override
    public IConfigManager getConfigManager() {
        var cluster = this.getCluster();

        if (cluster != null) {
            return this.getCluster().getConfigManager();
        } else {
            return NullConfigManager.INSTANCE;
        }
    }
}
