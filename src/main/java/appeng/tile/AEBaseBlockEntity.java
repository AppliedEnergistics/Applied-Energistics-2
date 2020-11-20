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

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import alexiil.mc.lib.attributes.item.FixedItemInv;

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

public class AEBaseBlockEntity extends BlockEntity implements IOrientable, ICommonTile, ICustomNameObject,
        BlockEntityClientSerializable, RenderAttachmentBlockEntity, AttributeProvider {

    static {
        DeferredTileEntityUnloader.register();
    }

    protected void onChunkUnloaded() {
    }

    private static final ThreadLocal<WeakReference<AEBaseBlockEntity>> DROP_NO_ITEMS = new ThreadLocal<>();
    private static final Map<Class<? extends BlockEntity>, IStackSrc> ITEM_STACKS = new HashMap<>();
    private int renderFragment = 0;
    @Nullable
    private String customName;
    private Direction forward = Direction.NORTH;
    private Direction up = Direction.UP;
    private boolean markDirtyQueued = false;

    public AEBaseBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public static void registerTileItem(final Class<? extends BlockEntity> c, final IStackSrc wat) {
        ITEM_STACKS.put(c, wat);
    }

    public boolean dropItems() {
        final WeakReference<AEBaseBlockEntity> what = DROP_NO_ITEMS.get();
        return what == null || what.get() != this;
    }

    public boolean notLoaded() {
        return !this.world.isChunkLoaded(this.pos);
    }

    @Nonnull
    public BlockEntity getTile() {
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
    public void fromTag(BlockState state, final CompoundTag data) {
        super.fromTag(state, data);

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
    public CompoundTag toTag(final CompoundTag data) {
        super.toTag(data);

        if (this.canBeRotated()) {
            data.putString("forward", this.getForward().name());
            data.putString("up", this.getUp().name());
        }

        if (this.customName != null) {
            data.putString("customName", this.customName);
        }

        return data;
    }

    public void onReady() {
    }

    private boolean readUpdateData(PacketByteBuf stream) {
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

    @Override
    public CompoundTag toClientTag(CompoundTag data) {
        boolean finished = false;

        final PacketByteBuf stream = new PacketByteBuf(Unpooled.buffer());

        try {
            this.writeToStream(stream);
            if (stream.readableBytes() == 0) {
                finished = true;
            }
        } catch (final Throwable t) {
            AELog.debug(t);
        }
        if (!finished) {
            stream.capacity(stream.readableBytes());
            data.putByteArray("X", stream.array());
        }

        return data;
    }

    /**
     * Handles tile entites that are being received by the client as part of a full chunk.
     */
    @Override
    public void fromClientTag(CompoundTag tag) {
        final PacketByteBuf stream = new PacketByteBuf(Unpooled.copiedBuffer(tag.getByteArray("X")));

        if (this.readUpdateData(stream)) {
            this.markForUpdate();
        }
    }

    protected boolean readFromStream(final PacketByteBuf data) throws IOException {
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

    protected void writeToStream(final PacketByteBuf data) throws IOException {
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
            if (this.world != null && !this.isRemoved() && !notLoaded()) {
                boolean alreadyUpdated = false;
                // Let the block update it's own state with our internal state changes
                BlockState currentState = getCachedState();
                if (currentState.getBlock() instanceof AEBaseTileBlock) {
                    AEBaseTileBlock<?> tileBlock = (AEBaseTileBlock<?>) currentState.getBlock();
                    BlockState newState = tileBlock.getBlockEntityBlockState(currentState, this);
                    if (currentState != newState) {
                        AELog.blockUpdate(this.pos, currentState, newState, this);
                        this.world.setBlockState(pos, newState);
                        alreadyUpdated = true;
                    }
                }

                if (!alreadyUpdated) {
                    this.world.updateListeners(this.pos, currentState, currentState, 1);
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
        Platform.notifyBlocksOfNeighbors(this.world, this.pos);
        this.saveChanges();
    }

    public void onPlacement(ItemPlacementContext context) {
        ItemStack stack = context.getStack();
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
            final FixedItemInv inv = ((ISegmentedInventory) this).getInventoryByName("config");
            if (inv instanceof AppEngInternalAEInventory) {
                final AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
                final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory(null, target.getSlotCount());
                tmp.readFromNBT(compound, "config");
                for (int x = 0; x < tmp.getSlotCount(); x++) {
                    target.forceSetInvStack(x, tmp.getInvStack(x));
                }
            }
        }
    }

    /**
     * returns the contents of the block entity, into the world, defaults to dropping everything in the inventory.
     *
     * @param w     world
     * @param pos   block position
     * @param drops drops of block entity
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
            final FixedItemInv inv = ((ISegmentedInventory) this).getInventoryByName("config");
            if (inv instanceof AppEngInternalAEInventory) {
                ((AppEngInternalAEInventory) inv).writeToNBT(output, "config");
            }
        }

        return output.isEmpty() ? null : output;
    }

    @Override
    public Text getCustomInventoryName() {
        return new LiteralText(this.hasCustomInventoryName() ? this.customName : this.getClass().getSimpleName());
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void securityBreak() {
        this.world.breakBlock(this.pos, true);
        this.disableDrops();
    }

    /**
     * Checks if this block entity is remote (we are running on the logical client side).
     */
    public boolean isClient() {
        World world = getWorld();
        return world == null || world.isClient();
    }

    public void disableDrops() {
        DROP_NO_ITEMS.set(new WeakReference<>(this));
    }

    public void saveChanges() {
        if (this.world != null) {
            this.world.markDirty(this.pos, this);
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

    @Override
    public Object getRenderAttachmentData() {
        return new AEModelData(up, forward);
    }

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
    }

}
