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

package appeng.tile;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.items.IItemHandler;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.util.ICommonTile;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.model.AEModelData;
import appeng.core.AELog;
import appeng.core.features.IStackSrc;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.hooks.TickHandler;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public class AEBaseTileEntity extends TileEntity implements IOrientable, ICommonTile, ICustomNameObject {

    private static final ThreadLocal<WeakReference<AEBaseTileEntity>> DROP_NO_ITEMS = new ThreadLocal<>();
    private static final Map<Class<? extends TileEntity>, IStackSrc> ITEM_STACKS = new HashMap<>();
    private int renderFragment = 0;
    @Nullable
    private String customName;
    private Direction forward = Direction.NORTH;
    private Direction up = Direction.UP;
    private boolean markDirtyQueued = false;

    public AEBaseTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public static void registerTileItem(final Class<? extends TileEntity> c, final IStackSrc wat) {
        ITEM_STACKS.put(c, wat);
    }

    public boolean dropItems() {
        final WeakReference<AEBaseTileEntity> what = DROP_NO_ITEMS.get();
        return what == null || what.get() != this;
    }

    public boolean notLoaded() {
        return !this.world.isBlockLoaded(this.pos);
    }

    @Nonnull
    public TileEntity getTile() {
        return this;
    }

    @Nullable
    protected ItemStack getItemFromTile(final Object obj) {
        final IStackSrc src = ITEM_STACKS.get(obj.getClass());
        if (src == null) {
            return ItemStack.EMPTY;
        }
        return src.stack(1);
    }

    @Override
    public void read(BlockState blockState, final CompoundNBT data) {
        super.read(blockState, data);

        if (data.contains("customName")) {
            this.customName = data.getString("customName");
        } else {
            this.customName = null;
        }

        try {
            if (this.canBeRotated()) {
                this.forward = Direction.valueOf(data.getString("forward"));
                this.up = Direction.valueOf(data.getString("up"));
            }
        } catch (final IllegalArgumentException ignored) {
        }
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);

        if (this.canBeRotated()) {
            data.putString("forward", this.getForward().name());
            data.putString("up", this.getUp().name());
        }

        if (this.customName != null) {
            data.putString("customName", this.customName);
        }

        return data;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 64, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt) {
        // / pkt.actionType
        if (pkt.getTileEntityType() == 64) {
            this.handleUpdateTag(null, pkt.getNbtCompound());
        }
    }

    public void onReady() {
    }

    /**
     * This builds a tag with the actual data that should be sent to the client for
     * update syncs. If the tile entity doesn't need update syncs, it returns null.
     */
    private CompoundNBT writeUpdateData() {
        final CompoundNBT data = new CompoundNBT();

        final PacketBuffer stream = new PacketBuffer(Unpooled.buffer());

        try {
            this.writeToStream(stream);
            if (stream.readableBytes() == 0) {
                return null;
            }
        } catch (final Throwable t) {
            AELog.debug(t);
        }

        stream.capacity(stream.readableBytes());
        data.putByteArray("X", stream.array());
        return data;
    }

    private boolean readUpdateData(PacketBuffer stream) {
        boolean output = false;

        try {
            this.renderFragment = 100;

            output = this.readFromStream(stream);

            if ((this.renderFragment & 1) == 1) {
                output = true;
            }
            this.renderFragment = 0;
        } catch (final Throwable t) {
            AELog.debug(t);
        }

        return output;
    }

    /**
     * Handles tile entites that are being sent to the client as part of a full
     * chunk.
     */
    @Override
    public CompoundNBT getUpdateTag() {
        final CompoundNBT data = this.writeUpdateData();

        if (data == null) {
            return new CompoundNBT();
        }

        data.putInt("x", this.pos.getX());
        data.putInt("y", this.pos.getY());
        data.putInt("z", this.pos.getZ());
        return data;
    }

    /**
     * Handles tile entites that are being received by the client as part of a full
     * chunk.
     */
    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        final PacketBuffer stream = new PacketBuffer(Unpooled.copiedBuffer(tag.getByteArray("X")));

        if (this.readUpdateData(stream)) {
            this.markForUpdate();
        }
    }

    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        if (this.canBeRotated()) {
            final Direction old_Forward = this.forward;
            final Direction old_Up = this.up;

            final byte orientation = data.readByte();
            this.forward = Direction.values()[orientation & 0x7];
            this.up = Direction.values()[orientation >> 3];

            return this.forward != old_Forward || this.up != old_Up;
        }
        return false;
    }

    protected void writeToStream(final PacketBuffer data) throws IOException {
        if (this.canBeRotated()) {
            final byte orientation = (byte) ((this.up.ordinal() << 3) | this.forward.ordinal());
            data.writeByte(orientation);
        }
    }

    public void markForUpdate() {
        if (this.renderFragment > 0) {
            this.renderFragment |= 1;
        } else {
            // TODO: Optimize Network Load
            if (this.world != null) {
                this.requestModelDataUpdate();

                boolean alreadyUpdated = false;
                // Let the block update it's own state with our internal state changes
                BlockState currentState = getBlockState();
                if (currentState.getBlock() instanceof AEBaseTileBlock) {
                    AEBaseTileBlock<?> tileBlock = (AEBaseTileBlock<?>) currentState.getBlock();
                    BlockState newState = tileBlock.getTileEntityBlockState(currentState, this);
                    if (currentState != newState) {
                        AELog.blockUpdate(this.pos, currentState, newState, this);
                        this.world.setBlockState(pos, newState);
                        alreadyUpdated = true;
                    }
                }

                if (!alreadyUpdated) {
                    this.world.notifyBlockUpdate(this.pos, currentState, currentState, 1);
                }
            }
        }
    }

    /**
     * By default all blocks can have orientation, this handles saving, and loading,
     * as well as synchronization.
     *
     * @return true if tile can be rotated
     */
    @Override
    public boolean canBeRotated() {
        return true;
    }

    @Override
    public Direction getForward() {
        return this.forward;
    }

    @Override
    public Direction getUp() {
        return this.up;
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        this.forward = inForward;
        this.up = inUp;
        this.markForUpdate();
        Platform.notifyBlocksOfNeighbors(this.world, this.pos);
        this.saveChanges();
    }

    public void onPlacement(BlockItemUseContext context) {
        ItemStack stack = context.getItem();
        if (stack.hasTag()) {
            this.uploadSettings(SettingsFrom.DISMANTLE_ITEM, stack.getTag());
        }
    }

    /**
     * depending on the from, different settings will be accepted, don't call this
     * with null
     *
     * @param from     source of settings
     * @param compound compound of source
     */
    public void uploadSettings(final SettingsFrom from, final CompoundNBT compound) {
        if (this instanceof IConfigurableObject) {
            final IConfigManager cm = ((IConfigurableObject) this).getConfigManager();
            if (cm != null) {
                cm.readFromNBT(compound);
            }
        }

        if (this instanceof IPriorityHost) {
            final IPriorityHost pHost = (IPriorityHost) this;
            pHost.setPriority(compound.getInt("priority"));
        }

        if (this instanceof ISegmentedInventory) {
            final IItemHandler inv = ((ISegmentedInventory) this).getInventoryByName("config");
            if (inv instanceof AppEngInternalAEInventory) {
                final AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
                final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory(null, target.getSlots());
                tmp.readFromNBT(compound, "config");
                for (int x = 0; x < tmp.getSlots(); x++) {
                    target.setStackInSlot(x, tmp.getStackInSlot(x));
                }
            }
        }
    }

    /**
     * returns the contents of the tile entity, into the world, defaults to dropping
     * everything in the inventory.
     *
     * @param w     world
     * @param pos   block position
     * @param drops drops of tile entity
     */
    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {

    }

    public void getNoDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {

    }

    /**
     * null means nothing to store...
     *
     * @param from source of settings
     *
     * @return compound of source
     */
    public CompoundNBT downloadSettings(final SettingsFrom from) {
        final CompoundNBT output = new CompoundNBT();

        if (this.hasCustomInventoryName()) {
            final CompoundNBT dsp = new CompoundNBT();
            dsp.putString("Name", this.customName);
            output.put("display", dsp);
        }

        if (this instanceof IConfigurableObject) {
            final IConfigManager cm = ((IConfigurableObject) this).getConfigManager();
            if (cm != null) {
                cm.writeToNBT(output);
            }
        }

        if (this instanceof IPriorityHost) {
            final IPriorityHost pHost = (IPriorityHost) this;
            output.putInt("priority", pHost.getPriority());
        }

        if (this instanceof ISegmentedInventory) {
            final IItemHandler inv = ((ISegmentedInventory) this).getInventoryByName("config");
            if (inv instanceof AppEngInternalAEInventory) {
                ((AppEngInternalAEInventory) inv).writeToNBT(output, "config");
            }
        }

        return output.isEmpty() ? null : output;
    }

    @Override
    public ITextComponent getCustomInventoryName() {
        return new StringTextComponent(
                this.hasCustomInventoryName() ? this.customName : this.getClass().getSimpleName());
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void securityBreak() {
        this.world.destroyBlock(this.pos, true);
        this.disableDrops();
    }

    /**
     * Checks if this tile entity is remote (we are running on the logical client
     * side).
     */
    public boolean isRemote() {
        World world = getWorld();
        return world == null || world.isRemote();
    }

    public void disableDrops() {
        DROP_NO_ITEMS.set(new WeakReference<>(this));
    }

    public void saveChanges() {
        if (this.world != null) {
            this.world.markChunkDirty(this.pos, this);
            if (!this.markDirtyQueued) {
                TickHandler.instance().addCallable(null, this::markDirtyAtEndOfTick);
                this.markDirtyQueued = true;
            }
        }
    }

    private Object markDirtyAtEndOfTick(final World w) {
        this.markDirty();
        this.markDirtyQueued = false;
        return null;
    }

    public void setName(final String name) {
        this.customName = name;
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new AEModelData(up, forward);
    }

}
