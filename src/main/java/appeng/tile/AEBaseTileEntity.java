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

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.util.ICommonTile;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.model.AEModelData;
import appeng.core.AELog;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.util.AEFluidInventory;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.hooks.ticking.TickHandler;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public class AEBaseTileEntity extends BlockEntity implements IOrientable, ICommonTile, ICustomNameObject {

    private static final ThreadLocal<WeakReference<AEBaseTileEntity>> DROP_NO_ITEMS = new ThreadLocal<>();
    private static final Map<BlockEntityType<?>, Item> REPRESENTATIVE_ITEMS = new HashMap<>();
    private int renderFragment = 0;
    @Nullable
    private String customName;
    private Direction forward = Direction.NORTH;
    private Direction up = Direction.UP;
    private boolean markDirtyQueued = false;

    public AEBaseTileEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public static void registerTileItem(BlockEntityType<?> type, final Item wat) {
        REPRESENTATIVE_ITEMS.put(type, wat);
    }

    public boolean dropItems() {
        final WeakReference<AEBaseTileEntity> what = DROP_NO_ITEMS.get();
        return what == null || what.get() != this;
    }

    public boolean notLoaded() {
        return !this.level.hasChunkAt(this.worldPosition);
    }

    @Nonnull
    public BlockEntity getTile() {
        return this;
    }

    @Nonnull
    protected ItemStack getItemFromTile() {
        final Item item = REPRESENTATIVE_ITEMS.get(getType());
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    @Override
    public void load(BlockState blockState, final CompoundTag data) {
        super.load(blockState, data);

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
    public CompoundTag save(final CompoundTag data) {
        super.save(data);

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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 64, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket pkt) {
        // / pkt.actionType
        if (pkt.getType() == 64) {
            this.handleUpdateTag(null, pkt.getTag());
        }
    }

    /**
     * Deferred tile-initialization when tiles actually start first ticking in a chunk. The tile needs to override
     * {@link #validate()} and call <code>TickHandler.instance().addInit(this);</code> to make this work.
     */
    public void onReady() {
    }

    /**
     * This builds a tag with the actual data that should be sent to the client for update syncs. If the tile entity
     * doesn't need update syncs, it returns null.
     */
    private CompoundTag writeUpdateData() {
        final CompoundTag data = new CompoundTag();

        final FriendlyByteBuf stream = new FriendlyByteBuf(Unpooled.buffer());

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

    private boolean readUpdateData(FriendlyByteBuf stream) {
        boolean output = false;

        try {
            this.renderFragment = 100;

            output = this.readFromStream(stream);

            if ((this.renderFragment & 1) == 1) {
                output = true;
            }
            this.renderFragment = 0;
        } catch (final Throwable t) {
            AELog.warn(t);
        }

        return output;
    }

    /**
     * Handles tile entites that are being sent to the client as part of a full chunk.
     */
    @Override
    public CompoundTag getUpdateTag() {
        final CompoundTag data = this.writeUpdateData();

        if (data == null) {
            return new CompoundTag();
        }

        data.putInt("x", this.worldPosition.getX());
        data.putInt("y", this.worldPosition.getY());
        data.putInt("z", this.worldPosition.getZ());
        return data;
    }

    /**
     * Handles tile entites that are being received by the client as part of a full chunk.
     */
    @Override
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        final FriendlyByteBuf stream = new FriendlyByteBuf(Unpooled.copiedBuffer(tag.getByteArray("X")));

        if (this.readUpdateData(stream)) {
            this.markForUpdate();
        }
    }

    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
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

    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        if (this.canBeRotated()) {
            final byte orientation = (byte) (this.up.ordinal() << 3 | this.forward.ordinal());
            data.writeByte(orientation);
        }
    }

    public void markForUpdate() {
        if (this.renderFragment > 0) {
            this.renderFragment |= 1;
        } else {
            // Clearing the cached model-data is always harmless regardless of status
            this.requestModelDataUpdate();

            // TODO: Optimize Network Load
            if (this.level != null && !this.isRemoved() && !notLoaded()) {

                boolean alreadyUpdated = false;
                // Let the block update it's own state with our internal state changes
                BlockState currentState = getBlockState();
                if (currentState.getBlock() instanceof AEBaseTileBlock) {
                    AEBaseTileBlock<?> tileBlock = (AEBaseTileBlock<?>) currentState.getBlock();
                    BlockState newState = tileBlock.getTileEntityBlockState(currentState, this);
                    if (currentState != newState) {
                        AELog.blockUpdate(this.worldPosition, currentState, newState, this);
                        this.level.setBlockAndUpdate(worldPosition, newState);
                        alreadyUpdated = true;
                    }
                }

                if (!alreadyUpdated) {
                    this.level.sendBlockUpdated(this.worldPosition, currentState, currentState, 1);
                }
            }
        }
    }

    /**
     * By default all blocks can have orientation, this handles saving, and loading, as well as synchronization.
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
        Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
        this.saveChanges();
    }

    public void onPlacement(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        if (stack.hasTag()) {
            this.uploadSettings(SettingsFrom.DISMANTLE_ITEM, stack.getTag());
        }
    }

    /**
     * depending on the from, different settings will be accepted, don't call this with null
     *
     * @param from     source of settings
     * @param compound compound of source
     */
    public void uploadSettings(final SettingsFrom from, final CompoundTag compound) {
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

        if (this instanceof IConfigurableFluidInventory) {
            final IFluidHandler tank = ((IConfigurableFluidInventory) this).getFluidInventoryByName("config");
            if (tank instanceof AEFluidInventory) {
                final AEFluidInventory target = (AEFluidInventory) tank;
                final AEFluidInventory tmp = new AEFluidInventory(null, target.getSlots());
                tmp.readFromNBT(compound, "config");
                for (int x = 0; x < tmp.getSlots(); x++) {
                    target.setFluidInSlot(x, tmp.getFluidInSlot(x));
                }
            }
        }
    }

    /**
     * returns the contents of the tile entity, into the world, defaults to dropping everything in the inventory.
     *
     * @param w     world
     * @param pos   block position
     * @param drops drops of tile entity
     */
    @Override
    public void getDrops(final Level w, final BlockPos pos, final List<ItemStack> drops) {

    }

    public void getNoDrops(final Level w, final BlockPos pos, final List<ItemStack> drops) {

    }

    /**
     * null means nothing to store...
     *
     * @param from source of settings
     * @return compound of source
     */
    public CompoundTag downloadSettings(final SettingsFrom from) {
        final CompoundTag output = new CompoundTag();

        if (this.hasCustomInventoryName()) {
            final CompoundTag dsp = new CompoundTag();
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
        if (this instanceof IConfigurableFluidInventory) {
            final IFluidHandler tank = ((IConfigurableFluidInventory) this).getFluidInventoryByName("config");
            if (tank instanceof AEFluidInventory) {
                ((AEFluidInventory) tank).writeToNBT(output, "config");
            }
        }
        return output.isEmpty() ? null : output;
    }

    @Override
    public Component getCustomInventoryName() {
        return new TextComponent(
                this.hasCustomInventoryName() ? this.customName : this.getClass().getSimpleName());
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void securityBreak() {
        this.level.destroyBlock(this.worldPosition, true);
        this.disableDrops();
    }

    /**
     * Checks if this tile entity is remote (we are running on the logical client side).
     */
    public boolean isRemote() {
        Level world = getLevel();
        return world == null || world.isClientSide();
    }

    public void disableDrops() {
        DROP_NO_ITEMS.set(new WeakReference<>(this));
    }

    public void saveChanges() {
        if (this.level == null) {
            return;
        }

        // Clientside is marked immediately as dirty as there is no queue processing
        // Serverside is only queued once per tick to avoid costly operations
        // TODO: Evaluate if this is still necessary
        if (this.level.isClientSide) {
            this.setChanged();
        } else {
            this.level.blockEntityChanged(this.worldPosition, this);
            if (!this.markDirtyQueued) {
                TickHandler.instance().addCallable(null, this::markDirtyAtEndOfTick);
                this.markDirtyQueued = true;
            }
        }
    }

    private Object markDirtyAtEndOfTick(final Level w) {
        this.setChanged();
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

    /**
     * AE Tile Entities will generally confine themselves to rendering within the bounding block. Forge however would
     * retrieve the collision box here, which is very expensive.
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 1, 1));
    }

}
