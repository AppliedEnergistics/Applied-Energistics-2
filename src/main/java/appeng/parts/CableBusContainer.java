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

package appeng.parts;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.api.config.YesNo;
import appeng.api.exceptions.FailedConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.implementations.parts.ICablePart;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IFacadeContainer;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalBlockPos;
import appeng.client.render.cablebus.CableBusRenderState;
import appeng.client.render.cablebus.CableCoreType;
import appeng.client.render.cablebus.FacadeRenderState;
import appeng.core.AELog;
import appeng.facade.FacadeContainer;
import appeng.helpers.AEMultiBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.FacadeItem;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.parts.networking.CablePart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class CableBusContainer implements AEMultiBlockEntity, ICableBusContainer {

    private static final ThreadLocal<Boolean> IS_LOADING = new ThreadLocal<>();
    private final CableBusStorage storage = new CableBusStorage();
    private YesNo hasRedstone = YesNo.UNDECIDED;
    private IPartHost tcb;
    private boolean requiresDynamicRender = false;
    private boolean inWorld = false;
    // Cached collision shape for living entities
    private VoxelShape cachedCollisionShapeLiving;
    // Cached collision shape for anything but living entities
    private VoxelShape cachedCollisionShape;
    private VoxelShape cachedShape;

    public CableBusContainer(IPartHost host) {
        this.tcb = host;
    }

    public static boolean isLoading() {
        final Boolean is = IS_LOADING.get();
        return is != null && is;
    }

    public void setHost(IPartHost host) {
        this.tcb.clearContainer();
        this.tcb = host;
    }

    @Override
    public IFacadeContainer getFacadeContainer() {
        return new FacadeContainer(this.storage, this::facadeChanged);
    }

    private void facadeChanged(Direction side) {
        invalidateShapes();
        updateNeighborShapeOnSide(side);
    }

    private ICablePart getCable() {
        return this.storage.getCenter();
    }

    @Nullable
    @Override
    public IPart getPart(@Nullable Direction partLocation) {
        if (partLocation == null) {
            return this.storage.getCenter();
        }
        return this.storage.getPart(partLocation);
    }

    @Override
    public boolean canAddPart(ItemStack is, Direction side) {
        if (FacadeItem.createFacade(is, side) != null) {
            return true;
        }

        if (is.getItem() instanceof IPartItem<?>partItem) {
            var part = partItem.createPart();
            if (part == null) {
                return false;
            }

            if (part instanceof ICablePart cablePart) {
                // Cables can be added if there's currently no cable, and existing parts work with the new cable
                return getCable() == null && arePartsCompatibleWithCable(cablePart);
            } else if (side != null) {
                // Parts can be added if the side is free, and they work with the existing cable (if any)
                return getPart(side) == null && isPartCompatibleWithCable(part, getCable());
            }
        }

        return false;
    }

    @Override
    @Nullable
    public <T extends IPart> T addPart(IPartItem<T> partItem, Direction side, @Nullable Player player) {
        // This code-path does not allow adding facades, while canAddPart allows facades.

        var part = partItem.createPart();

        if (part == null) {
            return null;
        }

        if (part instanceof ICablePart cablePart) {
            if (getCable() != null || !arePartsCompatibleWithCable(cablePart)) {
                return null;
            }

            this.storage.setCenter(cablePart);
            cablePart.setPartHostInfo(null, this, this.tcb.getBlockEntity());

            if (player != null) {
                cablePart.onPlacement(player);
            }

            if (this.inWorld) {
                // Ensure the exposed sides are set correctly before connecting it to nodes around
                updateConnections();
                cablePart.addToWorld();
            }

            // Connect the cables grid node to all existing internal grid nodes on the host
            var cableNode = cablePart.getGridNode();
            if (cableNode != null) {
                for (var partSide : Direction.values()) {
                    var existingPart = this.getPart(partSide);
                    if (existingPart != null) {
                        var existingPartNode = existingPart.getGridNode();
                        if (existingPartNode != null) {
                            try {
                                GridConnection.create(cableNode, existingPartNode, null);
                            } catch (FailedConnectionException e) {
                                if (!(e instanceof SecurityConnectionException)) {
                                    AELog.warn(e); // Security check failures are already logged in the security check
                                }

                                cablePart.removeFromWorld();
                                this.storage.setCenter(null);
                                return null;
                            }
                        }
                    }
                }
            }
        } else if (side != null) {
            var cable = getCable();
            if (!isPartCompatibleWithCable(part, cable)) {
                return null;
            }

            this.storage.setPart(side, part);
            part.setPartHostInfo(side, this, this.getBlockEntity());

            if (player != null) {
                part.onPlacement(player);
            }

            if (this.inWorld) {
                part.addToWorld();
            }

            // Connect the parts grid node to the existing cables grid node
            if (cable != null) {
                var cableNode = cable.getGridNode();
                var partNode = part.getGridNode();

                if (cableNode != null && partNode != null) {
                    try {
                        GridConnection.create(cableNode, partNode, null);
                    } catch (FailedConnectionException e) {
                        if (!(e instanceof SecurityConnectionException)) {
                            AELog.warn(e); // Security check failures are already logged in the security check
                        }

                        part.removeFromWorld();
                        this.storage.removePart(side);
                        return null;
                    }
                }
            }
        }

        updateAfterPartChange(side);

        return part;
    }

    // Check that the current attached parts are compatible with the given cable part
    private boolean arePartsCompatibleWithCable(ICablePart cable) {
        for (var d : Direction.values()) {
            var part = getPart(d);
            if (part != null && !isPartCompatibleWithCable(part, cable)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPartCompatibleWithCable(IPart part, @Nullable ICablePart cable) {
        return cable == null || part.canBePlacedOn(cable.supportsBuses());
    }

    @Override
    public <T extends IPart> T replacePart(IPartItem<T> partItem, @Nullable Direction side, Player owner,
            InteractionHand hand) {
        this.removePartWithoutUpdates(side);
        return this.addPart(partItem, side, owner);
    }

    @Override
    public void removePartFromSide(@Nullable Direction side) {
        this.removePartWithoutUpdates(side);

        updateAfterPartChange(side);

        // Cleanup the cable bus once it is no longer containing any parts.
        // Also only when the cable bus actually exists, otherwise it might perform a cleanup during initialization.
        if (this.isInWorld() && this.isEmpty()) {
            this.cleanup();
        }
    }

    private void updateAfterPartChange(Direction side) {
        this.invalidateShapes();
        this.updateDynamicRender();
        this.updateConnections();
        this.markForUpdate();
        this.markForSave();
        this.partChanged();

        updateNeighborShapeOnSide(side);
    }

    private void removePartWithoutUpdates(@Nullable Direction side) {
        if (side == null) {
            if (this.storage.getCenter() != null) {
                this.storage.getCenter().removeFromWorld();
            }
            this.storage.setCenter(null);
        } else {
            if (this.getPart(side) != null) {
                this.getPart(side).removeFromWorld();
            }
            this.storage.removePart(side);
        }
    }

    @Override
    public void markForUpdate() {
        this.tcb.markForUpdate();
    }

    @Override
    public DimensionalBlockPos getLocation() {
        return this.tcb.getLocation();
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this.tcb.getBlockEntity();
    }

    @Override
    public AEColor getColor() {
        if (this.storage.getCenter() != null) {
            final ICablePart c = this.storage.getCenter();
            return c.getCableColor();
        }
        return AEColor.TRANSPARENT;
    }

    @Override
    public void clearContainer() {
        throw new UnsupportedOperationException("Now that is silly!");
    }

    @Override
    public boolean isBlocked(Direction side) {
        return this.tcb.isBlocked(side);
    }

    @Override
    public SelectedPart selectPartLocal(Vec3 pos) {
        for (var side : Platform.DIRECTIONS_WITH_NULL) {
            var p = this.getPart(side);
            if (p != null) {
                var boxes = new ArrayList<AABB>();

                var bch = new BusCollisionHelper(boxes, side, true);
                p.getBoxes(bch);
                for (AABB bb : boxes) {
                    bb = bb.inflate(0.002, 0.002, 0.002);
                    if (bb.contains(pos)) {
                        return new SelectedPart(p, side);
                    }
                }
            }
        }

        if (PartHelper.getCableRenderMode().opaqueFacades) {
            var fc = this.getFacadeContainer();
            for (var side : Direction.values()) {
                var p = fc.getFacade(side);
                if (p != null) {
                    var boxes = new ArrayList<AABB>();

                    var bch = new BusCollisionHelper(boxes, side, true);
                    p.getBoxes(bch, true);
                    for (AABB bb : boxes) {
                        bb = bb.inflate(0.01, 0.01, 0.01);
                        if (bb.contains(pos)) {
                            return new SelectedPart(p, side);
                        }
                    }
                }
            }
        }

        return new SelectedPart();
    }

    @Override
    public void markForSave() {
        this.tcb.markForSave();
    }

    @Override
    public void partChanged() {
        // Drop all facades if no center to attach them to exists anymore
        if (this.storage.getCenter() == null) {
            var facades = new ArrayList<ItemStack>();

            var fc = this.getFacadeContainer();
            for (Direction d : Direction.values()) {
                final IFacadePart fp = fc.getFacade(d);
                if (fp != null) {
                    facades.add(fp.getItemStack());
                    fc.removeFacade(this.tcb, d);
                }
            }

            if (!facades.isEmpty()) {
                var te = this.tcb.getBlockEntity();
                Platform.spawnDrops(te.getLevel(), te.getBlockPos(), facades);
            }
        }

        // Update the exposed sides of exposed nodes
        for (var direction : Direction.values()) {
            var part = getPart(direction);
            if (part != null) {
                var node = part.getExternalFacingNode();
                if (node != null) {
                    ((GridNode) node).setExposedOnSides(EnumSet.of(direction));
                }
            }
        }

        this.tcb.partChanged();
    }

    @Override
    public boolean hasRedstone() {
        if (this.hasRedstone == YesNo.UNDECIDED) {
            this.updateRedstone();
        }

        return this.hasRedstone == YesNo.YES;
    }

    @Override
    public boolean isEmpty() {
        var fc = this.getFacadeContainer();
        for (var s : Platform.DIRECTIONS_WITH_NULL) {
            var part = this.getPart(s);
            if (part != null) {
                return false;
            }

            if (s != null) {
                var fp = fc.getFacade(s);
                if (fp != null) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void cleanup() {
        this.tcb.cleanup();
    }

    @Override
    public void notifyNeighbors() {
        this.tcb.notifyNeighbors();
    }

    @Override
    public boolean isInWorld() {
        return this.inWorld;
    }

    private void updateRedstone() {
        var te = this.getBlockEntity();
        this.hasRedstone = te.getLevel().hasNeighborSignal(te.getBlockPos()) ? YesNo.YES : YesNo.NO;
    }

    private void updateDynamicRender() {
        this.requiresDynamicRender = false;
        for (Direction s : Direction.values()) {
            final IPart p = this.getPart(s);
            if (p != null) {
                this.setRequiresDynamicRender(this.isRequiresDynamicRender() || p.requireDynamicRender());
            }
        }
    }

    public void updateConnections() {
        var center = this.storage.getCenter();
        if (center != null) {
            var sides = EnumSet.allOf(Direction.class);

            for (var s : Direction.values()) {
                if (this.getPart(s) != null || this.isBlocked(s)) {
                    sides.remove(s);
                }
            }

            center.setExposedOnSides(sides);
        }
    }

    public void addToWorld() {
        if (this.inWorld) {
            return;
        }

        this.inWorld = true;
        IS_LOADING.set(true);

        final BlockEntity te = this.getBlockEntity();

        // start with the center, then install the side parts into the grid.
        for (int x = 6; x >= 0; x--) {
            final Direction s = Platform.DIRECTIONS_WITH_NULL[x];
            final IPart part = this.getPart(s);

            if (part != null) {
                part.setPartHostInfo(s, this, te);
                part.addToWorld();

                if (s != null) {
                    final IGridNode sn = part.getGridNode();
                    if (sn != null) {
                        // this is a really stupid if statement, why was this
                        // here?
                        // if ( !sn.getConnections().iterator().hasNext() )

                        final IPart center = this.getPart(null);
                        if (center != null) {
                            final IGridNode cn = center.getGridNode();
                            if (cn != null) {
                                try {
                                    GridHelper.createGridConnection(cn, sn);
                                } catch (FailedConnectionException e) {
                                    // ekk
                                    AELog.debug(e);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.partChanged();

        IS_LOADING.set(false);
    }

    public void removeFromWorld() {
        if (!this.inWorld) {
            return;
        }

        this.inWorld = false;

        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            final IPart part = this.getPart(s);
            if (part != null) {
                part.removeFromWorld();
            }
        }

        this.invalidateShapes();
        this.partChanged();
    }

    @Override
    public IGridNode getGridNode(Direction side) {
        final IPart part = this.getPart(side);
        if (part != null) {
            final IGridNode n = part.getExternalFacingNode();
            if (n != null) {
                return n;
            }
        }

        if (this.storage.getCenter() != null) {
            return this.storage.getCenter().getGridNode();
        }

        return null;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        final IPart part = this.getPart(dir);

        if (part != null) {
            return part.getExternalCableConnectionType();
        }

        if (this.storage.getCenter() != null) {
            final ICablePart c = this.storage.getCenter();
            return c.getCableConnectionType();
        }
        return AECableType.NONE;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return this.getPart(null) instanceof ICablePart
                ? this.getPart(null).getCableConnectionLength(cable)
                : -1;
    }

    @Override
    public int isProvidingStrongPower(Direction side) {
        final IPart part = this.getPart(side);
        return part != null ? part.isProvidingStrongPower() : 0;
    }

    @Override
    public int isProvidingWeakPower(Direction side) {
        final IPart part = this.getPart(side);
        return part != null ? part.isProvidingWeakPower() : 0;
    }

    @Override
    public boolean canConnectRedstone(Direction opposite) {
        final IPart part = this.getPart(opposite);
        return part != null && part.canConnectRedstone();
    }

    @Override
    public void onEntityCollision(Entity entity) {
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            final IPart part = this.getPart(s);
            if (part != null) {
                part.onEntityCollision(entity);
            }
        }
    }

    @Override
    public boolean activate(Player player, InteractionHand hand, Vec3 pos) {
        final SelectedPart p = this.selectPartLocal(pos);
        if (p != null && p.part != null) {
            // forge sends activate even when sneaking in some cases (eg emtpy hand)
            // if sneaking try shift activate first.
            if (InteractionUtil.isInAlternateUseMode(player) && p.part.onShiftActivate(player, hand, pos)) {
                return true;
            }
            return p.part.onActivate(player, hand, pos);
        }
        return false;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        this.hasRedstone = YesNo.UNDECIDED;

        for (var s : Platform.DIRECTIONS_WITH_NULL) {
            var part = this.getPart(s);
            if (part != null) {
                part.onNeighborChanged(level, pos, neighbor);
            }
        }

        // Some parts will change their shape (connected texture style)
        invalidateShapes();
    }

    @Override
    public boolean isLadder(LivingEntity entity) {
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            final IPart p = this.getPart(side);
            if (p != null && p.isLadder(entity)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void animateTick(Level level, BlockPos pos, RandomSource r) {
        for (Direction side : Platform.DIRECTIONS_WITH_NULL) {
            final IPart p = this.getPart(side);
            if (p != null) {
                p.animateTick(level, pos, r);
            }
        }
    }

    @Override
    public int getLightValue() {
        int light = 0;

        for (Direction d : Platform.DIRECTIONS_WITH_NULL) {
            final IPart p = this.getPart(d);
            if (p != null) {
                light = Math.max(p.getLightLevel(), light);
            }
        }

        return light;
    }

    public void writeToStream(FriendlyByteBuf data) {
        int sides = 0;
        for (int x = 0; x < Platform.DIRECTIONS_WITH_NULL.length; x++) {
            var p = this.getPart(Platform.DIRECTIONS_WITH_NULL[x]);
            if (p != null) {
                sides |= 1 << x;
            }
        }

        data.writeByte((byte) sides);

        for (int x = 0; x < Platform.DIRECTIONS_WITH_NULL.length; x++) {
            var p = this.getPart(Platform.DIRECTIONS_WITH_NULL[x]);
            if (p != null) {
                data.writeVarInt(IPartItem.getNetworkId(p.getPartItem()));

                p.writeToStream(data);
            }
        }

        this.getFacadeContainer().writeToStream(data);
    }

    public boolean readFromStream(FriendlyByteBuf data) {
        final byte sides = data.readByte();

        boolean updateBlock = false;

        for (int x = 0; x < Platform.DIRECTIONS_WITH_NULL.length; x++) {
            Direction side = Platform.DIRECTIONS_WITH_NULL[x];
            if ((sides & 1 << x) == 1 << x) {
                IPart p = this.getPart(side);

                var itemId = data.readVarInt();
                var partItem = IPartItem.byNetworkId(itemId);

                if (p != null && p.getPartItem() == partItem) {
                    if (p.readFromStream(data)) {
                        updateBlock = true;
                    }
                } else if (partItem != null) {
                    this.removePartFromSide(side);
                    p = this.addPart(partItem, side, null);
                    if (p != null) {
                        p.readFromStream(data);
                    } else {
                        throw new IllegalStateException("Invalid Stream For CableBus Container.");
                    }
                } else {
                    throw new IllegalStateException("Invalid item from server for part: " + itemId);
                }
            } else if (this.getPart(side) != null) {
                this.removePartFromSide(side);
            }
        }

        updateBlock |= this.getFacadeContainer().readFromStream(data);

        // Updating block entities may change the collision shape
        this.invalidateShapes();

        return updateBlock;
    }

    public void writeToNBT(CompoundTag data) {
        data.putInt("hasRedstone", this.hasRedstone.ordinal());

        final IFacadeContainer fc = this.getFacadeContainer();
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            fc.writeToNBT(data);

            var part = this.getPart(s);
            if (part != null) {
                var itemId = IPartItem.getId(part.getPartItem());

                var side = this.getSide(part);
                var id = side == null ? "center" : side.name();

                var partData = new CompoundTag();
                part.writeToNBT(partData);

                data.putString("item:" + id, itemId.toString());
                if (!partData.isEmpty()) {
                    data.put("extra:" + id, partData);
                }
            }
        }
    }

    private Direction getSide(IPart part) {
        if (this.storage.getCenter() == part) {
            return null;
        } else {
            for (Direction side : Direction.values()) {
                if (this.getPart(side) == part) {
                    return side;
                }
            }
        }

        throw new IllegalStateException("Uhh Bad Part (" + part + ") on Side.");
    }

    public void readFromNBT(CompoundTag data) {
        invalidateShapes();

        if (data.contains("hasRedstone")) {
            this.hasRedstone = YesNo.values()[data.getInt("hasRedstone")];
        }

        for (var side : Platform.DIRECTIONS_WITH_NULL) {
            var id = side == null ? "center" : side.name();

            // TODO 1.18: Remove in Beta
            // Migrates from old format to new format
            String defKey = "def:" + id;
            if (data.contains(defKey, Tag.TAG_COMPOUND)) {
                final CompoundTag def = data.getCompound(defKey);
                final ItemStack iss = ItemStack.of(def);
                if (iss.isEmpty()) {
                    continue;
                }
                data.putString("item:" + id, Registry.ITEM.getKey(iss.getItem()).toString());
            }

            String itemKey = "item:" + id;
            String extraKey = "extra:" + id;
            if (data.contains(itemKey, Tag.TAG_STRING)) {
                var itemId = new ResourceLocation(data.getString(itemKey));
                var partItem = IPartItem.byId(itemId);
                if (partItem == null) {
                    AELog.warn("Ignoring persisted part with non-part-item %s", itemId);
                    continue;
                }

                var partData = data.getCompound(extraKey);

                var p = this.getPart(side);
                if (p != null && p.getPartItem() == partItem) {
                    p.readFromNBT(partData);
                } else {
                    p = this.replacePart(partItem, side, null, null);
                    if (p != null) {
                        p.readFromNBT(partData);
                    } else {
                        AELog.warn("Invalid NBT For CableBus Container: " + itemId
                                + " is not a valid part; it was ignored.");
                    }
                }
            } else {
                this.removePartFromSide(side);
            }
        }

        this.getFacadeContainer().readFromNBT(data);
    }

    public List<ItemStack> addPartDrops(List<ItemStack> drops) {
        for (var side : Platform.DIRECTIONS_WITH_NULL) {
            var part = this.getPart(side);
            if (part != null) {
                part.addPartDrop(drops, false);
            }

            if (side != null) {
                final IFacadePart fp = this.getFacadeContainer().getFacade(side);
                if (fp != null) {
                    drops.add(fp.getItemStack());
                }
            }
        }

        return drops;
    }

    public List<ItemStack> addAdditionalDrops(List<ItemStack> drops) {
        for (var side : Platform.DIRECTIONS_WITH_NULL) {
            var part = this.getPart(side);
            if (part != null) {
                part.addAdditionalDrops(drops, false);
            }
        }

        return drops;
    }

    @Override
    public boolean recolourBlock(Direction side, AEColor colour, Player who) {
        final IPart cable = this.getPart(null);
        if (cable != null) {
            final ICablePart pc = (ICablePart) cable;
            return pc.changeColor(colour, who);
        }
        return false;
    }

    public boolean isRequiresDynamicRender() {
        return this.requiresDynamicRender;
    }

    private void setRequiresDynamicRender(boolean requiresDynamicRender) {
        this.requiresDynamicRender = requiresDynamicRender;
    }

    @Override
    public CableBusRenderState getRenderState() {
        final CablePart cable = (CablePart) this.storage.getCenter();

        final CableBusRenderState renderState = new CableBusRenderState();

        if (cable != null) {
            renderState.setCableColor(cable.getCableColor());
            renderState.setCableType(cable.getCableConnectionType());
            renderState.setCoreType(CableCoreType.fromCableType(cable.getCableConnectionType()));

            // Check each outgoing connection for the desired characteristics
            for (var side : Direction.values()) {
                // Is there a connection?
                if (!cable.isConnected(side)) {
                    continue;
                }

                // If there is one, check out which type it has, but default to this cable's
                // type
                AECableType connectionType = cable.getCableConnectionType();

                // Only use the incoming cable-type of the adjacent block, if it's not a cable bus itself
                // Dense cables however also respect the adjacent cable-type since their outgoing connection
                // point would look too big for other cable types
                final BlockPos adjacentPos = this.getBlockEntity().getBlockPos().relative(side);
                var adjacentHost = GridHelper.getNodeHost(getBlockEntity().getLevel(), adjacentPos);

                if (adjacentHost != null) {
                    var adjacentType = adjacentHost.getCableConnectionType(side.getOpposite());
                    connectionType = AECableType.min(connectionType, adjacentType);
                }

                // Check if the adjacent TE is a cable bus or not
                if (adjacentHost instanceof CableBusContainer) {
                    renderState.getCableBusAdjacent().add(side);
                }

                renderState.getConnectionTypes().put(side, connectionType);
            }

            // Collect the number of channels used per side
            // We have to do this even for non-smart cables since a glass cable can display
            // a connection as smart if the
            // adjacent block entity requires it
            for (var side : Direction.values()) {
                int channels = cable.getCableConnectionType().isSmart() ? cable.getChannelsOnSide(side) : 0;
                renderState.getChannelsOnSide().put(side, channels);
            }
        }

        // Determine attachments and facades
        for (var side : Direction.values()) {
            final FacadeRenderState facadeState = this.getFacadeRenderState(side);

            if (facadeState != null) {
                renderState.getFacades().put(side, facadeState);
            }

            final IPart part = this.getPart(side);

            if (part == null) {
                continue;
            }

            renderState.getPartModelData().put(side, part.getRenderAttachmentData());

            // This will add the part's bounding boxes to the render state, which is
            // required for facades
            final IPartCollisionHelper bch = new BusCollisionHelper(renderState.getBoundingBoxes(), side, true);
            part.getBoxes(bch);

            // Some attachments want a thicker cable than glass, account for that
            var desiredType = part.getDesiredConnectionType();
            if (renderState.getCoreType() == CableCoreType.GLASS
                    && (desiredType == AECableType.SMART || desiredType == AECableType.COVERED)) {
                renderState.setCoreType(CableCoreType.COVERED);
            }

            int length = (int) part.getCableConnectionLength(null);
            if (length > 0 && length <= 8) {
                renderState.getAttachmentConnections().put(side, length);
            }

            renderState.getAttachments().put(side, part.getStaticModels());
        }

        return renderState;
    }

    private FacadeRenderState getFacadeRenderState(Direction side) {
        // Store the "masqueraded" itemstack for the given side, if there is a facade
        final IFacadePart facade = this.storage.getFacade(side);

        if (facade != null) {
            final ItemStack textureItem = facade.getTextureItem();
            final BlockState blockState = facade.getBlockState();

            Level level = getBlockEntity().getLevel();
            if (blockState != null && textureItem != null && level != null) {
                return new FacadeRenderState(blockState,
                        !facade.getBlockState().isSolidRender(level, getBlockEntity().getBlockPos()));
            }
        }

        return null;
    }

    /**
     * See {@link Block#getShape}
     */
    public VoxelShape getShape() {
        if (cachedShape == null) {
            cachedShape = createShape(false, false);
        }

        return cachedShape;
    }

    /**
     * See {@link Block#getCollisionShape}
     */
    public VoxelShape getCollisionShape(CollisionContext context) {
        // This is a hack for annihilation planes
        var itemEntity = context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof ItemEntity;

        if (itemEntity) {
            if (cachedCollisionShapeLiving == null) {
                cachedCollisionShapeLiving = createShape(true, true);
            }
            return cachedCollisionShapeLiving;
        } else {
            if (cachedCollisionShape == null) {
                cachedCollisionShape = createShape(true, false);
            }
            return cachedCollisionShape;
        }
    }

    private VoxelShape createShape(boolean forCollision, boolean forItemEntity) {
        final List<AABB> boxes = new ArrayList<>();

        final IFacadeContainer fc = this.getFacadeContainer();
        for (Direction s : Platform.DIRECTIONS_WITH_NULL) {
            final IPartCollisionHelper bch = new BusCollisionHelper(boxes, s, !forCollision);

            final IPart part = this.getPart(s);
            if (part != null) {
                part.getBoxes(bch);
            }

            if ((PartHelper.getCableRenderMode().opaqueFacades || forCollision)
                    && s != null) {
                final IFacadePart fp = fc.getFacade(s);
                if (fp != null) {
                    fp.getBoxes(bch, forItemEntity);
                }
            }
        }

        return VoxelShapeCache.get(boxes);
    }

    private void invalidateShapes() {
        cachedShape = null;
        cachedCollisionShape = null;
        cachedCollisionShapeLiving = null;
    }

    private void updateNeighborShapeOnSide(Direction side) {
        // Update the shape of the neighbor asynchronously (i.e. for walls)
        var be = getBlockEntity();
        if (be != null && be.getLevel() != null && !be.getLevel().isClientSide()) {
            TickHandler.instance().addCallable(be.getLevel(), level -> {
                if (!be.isRemoved()) {
                    var ourPos = be.getBlockPos();
                    var neighborPos = ourPos.relative(side);
                    var neighborState = level.getBlockState(neighborPos);
                    var ourState = be.getBlockState();
                    BlockState newNeighborState = neighborState.updateShape(side.getOpposite(), ourState, level,
                            neighborPos, ourPos);
                    Block.updateOrDestroy(neighborState, newNeighborState, level, neighborPos, Block.UPDATE_ALL,
                            Block.UPDATE_LIMIT);
                }
            });
        }
    }

}
