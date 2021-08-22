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

package appeng.blockentity.networking;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.helpers.AEMultiBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.parts.CableBusContainer;
import appeng.util.Platform;

public class CableBusBlockEntity extends AEBaseBlockEntity implements AEMultiBlockEntity {

    private CableBusContainer cb = new CableBusContainer(this);

    private int oldLV = -1; // on re-calculate light when it changes

    public CableBusBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.getCableBus().readFromNBT(data);
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        this.getCableBus().writeToNBT(data);
        return data;
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        boolean ret = this.getCableBus().readFromStream(data);

        final int newLV = this.getCableBus().getLightValue();
        if (newLV != this.oldLV) {
            this.oldLV = newLV;
            this.level.getLightEngine().checkBlock(this.worldPosition);
            ret = true;
        }

        this.updateBlockEntitySettings();
        return ret || c;
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        this.getCableBus().writeToStream(data);
    }

    /**
     * Changes this block entity to the TESR version if any of the parts require dynamic rendering.
     */
    protected void updateBlockEntitySettings() {
        // FIXME: potentially invalidate voxel shape cache?
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.getCableBus().removeFromWorld();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        TickHandler.instance().addInit(this);
    }

    @Override
    public IGridNode getGridNode(final Direction dir) {
        return this.getCableBus().getGridNode(dir);
    }

    @Override
    public AECableType getCableConnectionType(Direction side) {
        return this.getCableBus().getCableConnectionType(side);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return this.getCableBus().getCableConnectionLength(cable);
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.getCableBus().removeFromWorld();
    }

    @Override
    public void markForUpdate() {
        if (this.level == null) {
            return;
        }

        final int newLV = this.getCableBus().getLightValue();
        if (newLV != this.oldLV) {
            this.oldLV = newLV;
            this.level.getLightEngine().checkBlock(this.worldPosition);
        }

        super.markForUpdate();
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        this.getCableBus().getDrops(drops);
    }

    @Override
    public void getNoDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        this.getCableBus().getNoDrops(drops);
    }

    @Override
    public void onReady() {
        super.onReady();
        if (this.getCableBus().isEmpty()) {
            if (this.level.getBlockEntity(this.worldPosition) == this) {
                this.level.destroyBlock(this.worldPosition, true);
            }
        } else {
            this.getCableBus().addToWorld();
        }
    }

    @Override
    public IFacadeContainer getFacadeContainer() {
        return this.getCableBus().getFacadeContainer();
    }

    @Override
    public boolean canAddPart(final ItemStack is, final Direction side) {
        return this.getCableBus().canAddPart(is, side);
    }

    @Override
    public boolean addPart(final ItemStack is, final Direction side, final Player player,
            final InteractionHand hand) {
        return this.getCableBus().addPart(is, side, player, hand);
    }

    @Override
    public IPart getPart(final Direction side) {
        return this.cb.getPart(side);
    }

    @Override
    public void removePart(@Nullable Direction side, final boolean suppressUpdate) {
        this.getCableBus().removePart(side, suppressUpdate);
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return new DimensionalBlockPos(this);
    }

    @Override
    public AEColor getColor() {
        return this.getCableBus().getColor();
    }

    @Override
    public void clearContainer() {
        this.setCableBus(new CableBusContainer(this));
    }

    @Override
    public boolean isBlocked(final Direction side) {
        // TODO 1.10.2-R - Stuff.
        return false;
    }

    @Override
    public SelectedPart selectPart(final Vec3 pos) {
        return this.getCableBus().selectPart(pos);
    }

    @Override
    public void markForSave() {
        this.saveChanges();
    }

    @Override
    public void partChanged() {
        this.notifyNeighbors();
    }

    @Override
    public boolean hasRedstone(final Direction side) {
        return this.getCableBus().hasRedstone(side);
    }

    @Override
    public boolean isEmpty() {
        return this.getCableBus().isEmpty();
    }

    @Override
    public Set<LayerFlags> getLayerFlags() {
        return this.getCableBus().getLayerFlags();
    }

    @Override
    public void cleanup() {
        this.getLevel().removeBlock(this.worldPosition, false);
    }

    @Override
    public void notifyNeighbors() {
        if (this.level != null && this.level.hasChunkAt(this.worldPosition) && !CableBusContainer.isLoading()) {
            Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
        }
    }

    @Override
    public boolean isInWorld() {
        return this.getCableBus().isInWorld();
    }

    @Override
    public boolean recolourBlock(final Direction side, final AEColor colour, final Player who) {
        return this.getCableBus().recolourBlock(side, colour, who);
    }

    public CableBusContainer getCableBus() {
        return this.cb;
    }

    private void setCableBus(final CableBusContainer cb) {
        this.cb = cb;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass, @Nullable Direction partLocation) {
        // Note that null will be translated to INTERNAL here

        IPart part = this.getPart(partLocation);
        LazyOptional<T> result = part == null ? LazyOptional.empty() : part.getCapability(capabilityClass);

        if (result.isPresent()) {
            return result;
        }

        return super.getCapability(capabilityClass, partLocation);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        Level level = getLevel();
        if (level == null) {
            return EmptyModelData.INSTANCE;
        }

        CableBusRenderState renderState = this.cb.getRenderState();
        renderState.setLevel(level);
        renderState.setPos(worldPosition);
        return new ModelDataMap.Builder().withInitial(CableBusRenderState.PROPERTY, renderState).build();

    }
}
