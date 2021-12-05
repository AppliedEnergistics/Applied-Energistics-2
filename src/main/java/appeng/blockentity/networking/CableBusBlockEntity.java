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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.core.AppEng;
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
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.getCableBus().readFromNBT(data);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.getCableBus().writeToNBT(data);
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
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
    protected void writeToStream(FriendlyByteBuf data) {
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
    public IGridNode getGridNode(Direction dir) {
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
    public void getDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        this.getCableBus().getDrops(drops);
    }

    @Override
    public void getNoDrops(Level level, BlockPos pos, List<ItemStack> drops) {
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

    @Nullable
    @Override
    public IPart getPart(@Nullable Direction side) {
        return this.cb.getPart(side);
    }

    @Override
    public boolean canAddPart(ItemStack is, Direction side) {
        return this.getCableBus().canAddPart(is, side);
    }

    @Override
    @Nullable
    public <T extends IPart> T addPart(IPartItem<T> partItem, Direction side,
            @org.jetbrains.annotations.Nullable Player player) {
        return cb.addPart(partItem, side, player);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends IPart> T replacePart(IPartItem<T> partItem, @org.jetbrains.annotations.Nullable Direction side,
            Player owner, InteractionHand hand) {
        return cb.replacePart(partItem, side, owner, hand);
    }

    @Override
    public void removePart(@Nullable Direction side) {
        this.getCableBus().removePart(side);
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
    public boolean isBlocked(Direction side) {
        // TODO 1.10.2-R - Stuff.
        return false;
    }

    @Override
    public SelectedPart selectPartLocal(Vec3 pos) {
        return this.getCableBus().selectPartLocal(pos);
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
    public boolean hasRedstone() {
        return this.getCableBus().hasRedstone();
    }

    @Override
    public boolean isEmpty() {
        return this.getCableBus().isEmpty();
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
    public boolean recolourBlock(Direction side, AEColor colour, Player who) {
        return this.getCableBus().recolourBlock(side, colour, who);
    }

    public CableBusContainer getCableBus() {
        return this.cb;
    }

    private void setCableBus(CableBusContainer cb) {
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

    @Override
    public InteractionResult disassembleWithWrench(Player player, Level level, BlockHitResult hitResult) {

        if (!level.isClientSide) {
            final List<ItemStack> is = new ArrayList<>();
            final SelectedPart sp;

            AppEng.instance().setPartInteractionPlayer(player);
            try {
                sp = cb.selectPartWorld(hitResult.getLocation());
            } finally {
                AppEng.instance().setPartInteractionPlayer(null);
            }

            // SelectedPart contains either a facade or a part. Never both.
            if (sp.part != null) {
                sp.part.getDrops(is, true);
                cb.removePart(sp.side);
            }

            var pos = getBlockPos();

            // A facade cannot exist without a cable part, no host cleanup needed.
            if (sp.facade != null) {
                is.add(sp.facade.getItemStack());
                cb.getFacadeContainer().removeFacade(cb, sp.side);
                Platform.notifyBlocksOfNeighbors(level, pos);
            }

            if (!is.isEmpty()) {
                Platform.spawnDrops(level, pos, is);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());

    }
}
