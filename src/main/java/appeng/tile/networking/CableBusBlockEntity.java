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

package appeng.tile.networking;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;

import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerFlags;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.helpers.AEMultiTile;
import appeng.hooks.TickHandler;
import appeng.parts.CableBusContainer;
import appeng.tile.AEBaseBlockEntity;
import appeng.util.Platform;

public class CableBusBlockEntity extends AEBaseBlockEntity implements AEMultiTile {

    private CableBusContainer cb = new CableBusContainer(this);

    private int oldLV = -1; // on re-calculate light when it changes

    public CableBusBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public void fromTag(final CompoundTag data) {
        super.fromTag(data);
        this.getCableBus().readFromNBT(data);
    }

    @Override
    public CompoundTag toTag(final CompoundTag data) {
        super.toTag(data);
        this.getCableBus().writeToNBT(data);
        return data;
    }

    @Override
    protected boolean readFromStream(final PacketByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        boolean ret = this.getCableBus().readFromStream(data);

        final int newLV = this.getCableBus().getLightValue();
        if (newLV != this.oldLV) {
            this.oldLV = newLV;
            this.world.getLightingProvider().checkBlock(this.pos);
            ret = true;
        }

        this.updateTileSetting();
        return ret || c;
    }

    @Override
    protected void writeToStream(final PacketByteBuf data) throws IOException {
        super.writeToStream(data);
        this.getCableBus().writeToStream(data);
    }

    /**
     * Changes this tile to the TESR version if any of the parts require dynamic rendering.
     */
    protected void updateTileSetting() {
        // FIXME: potentially invalidate voxel shape cache?
    }

    @Override
    public double getSquaredRenderDistance() {
        return 900.0;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        this.getCableBus().removeFromWorld();
    }

    @Override
    public void cancelRemoval() {
        super.cancelRemoval();
        TickHandler.instance().addInit(this);
    }

    @Override
    public IGridNode getGridNode(final AEPartLocation dir) {
        return this.getCableBus().getGridNode(dir);
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation side) {
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
        if (this.world == null) {
            return;
        }

        final int newLV = this.getCableBus().getLightValue();
        if (newLV != this.oldLV) {
            this.oldLV = newLV;
            this.world.getLightingProvider().checkBlock(this.pos);
        }

        super.markForUpdate();
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        // the parts and facades are handled by CableBusBlock#getDroppedStacks
        this.getCableBus().appendPartContentDrops(drops);
    }

    @Override
    public void getNoDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        this.getCableBus().appendPartContentDrops(drops);
    }

    @Override
    public void onReady() {
        super.onReady();
        if (this.getCableBus().isEmpty()) {
            if (this.world.getBlockEntity(this.pos) == this) {
                this.world.breakBlock(this.pos, true);
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
    public boolean canAddPart(final ItemStack is, final AEPartLocation side) {
        return this.getCableBus().canAddPart(is, side);
    }

    @Override
    public AEPartLocation addPart(final ItemStack is, final AEPartLocation side, final PlayerEntity player,
            final Hand hand) {
        return this.getCableBus().addPart(is, side, player, hand);
    }

    @Override
    public IPart getPart(final AEPartLocation side) {
        return this.cb.getPart(side);
    }

    @Override
    public IPart getPart(final Direction side) {
        return this.getCableBus().getPart(side);
    }

    @Override
    public void removePart(final AEPartLocation side, final boolean suppressUpdate) {
        this.getCableBus().removePart(side, suppressUpdate);
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
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
    public SelectedPart selectPart(final Vec3d pos) {
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
    public boolean hasRedstone(final AEPartLocation side) {
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
        this.getWorld().removeBlock(this.pos, false);
    }

    @Override
    public void notifyNeighbors() {
        if (this.world != null && this.world.isChunkLoaded(this.pos) && !CableBusContainer.isLoading()) {
            Platform.notifyBlocksOfNeighbors(this.world, this.pos);
        }
    }

    @Override
    public boolean isInWorld() {
        return this.getCableBus().isInWorld();
    }

    @Override
    public boolean recolourBlock(final Direction side, final AEColor colour, final PlayerEntity who) {
        return this.getCableBus().recolourBlock(side, colour, who);
    }

    public CableBusContainer getCableBus() {
        return this.cb;
    }

    private void setCableBus(final CableBusContainer cb) {
        this.cb = cb;
    }

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        super.addAllAttributes(world, pos, state, to);

        Direction direction = to.getSearchDirection();
        if (direction == null) {
            return;
        }
        AEPartLocation partLocation = AEPartLocation.fromFacing(direction.getOpposite());
        IPart part = this.cb.getPart(partLocation);
        if (part != null) {
            part.addAllAttributes(to);
        }
    }

    @Override
    public CableBusRenderState getRenderAttachmentData() {
        World world = getWorld();
        if (world == null) {
            return null;
        }

        CableBusRenderState renderState = this.cb.getRenderState();
        renderState.setWorld(world);
        renderState.setPos(pos);
        return renderState;

    }
}
