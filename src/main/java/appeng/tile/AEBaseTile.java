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


import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.util.ICommonTile;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IOrientable;
import appeng.core.AELog;
import appeng.core.features.IStackSrc;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.util.AEFluidInventory;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.hooks.TickHandler;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AEBaseTile extends TileEntity implements IOrientable, ICommonTile, ICustomNameObject {

    private static final ThreadLocal<WeakReference<AEBaseTile>> DROP_NO_ITEMS = new ThreadLocal<>();
    private static final Map<Class<? extends TileEntity>, IStackSrc> ITEM_STACKS = new HashMap<>();
    private int renderFragment = 0;
    @Nullable
    private String customName;
    private EnumFacing forward = null;
    private EnumFacing up = null;
    private IBlockState state;
    private boolean markDirtyQueued = false;

    @Override
    public boolean shouldRefresh(final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newSate) {
        return newSate.getBlock() != oldState.getBlock(); // state doesn't change tile entities in AE2.
    }

    public static void registerTileItem(final Class<? extends TileEntity> c, final IStackSrc wat) {
        ITEM_STACKS.put(c, wat);
    }

    public boolean dropItems() {
        final WeakReference<AEBaseTile> what = DROP_NO_ITEMS.get();
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

    @Nonnull
    public IBlockState getBlockState() {
        if (this.state == null) {
            this.state = this.world.getBlockState(this.getPos());
        }
        return this.state;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);

        if (data.hasKey("customName")) {
            this.customName = data.getString("customName");
        } else {
            this.customName = null;
        }

        try {
            if (this.canBeRotated()) {
                this.forward = EnumFacing.valueOf(data.getString("forward"));
                this.up = EnumFacing.valueOf(data.getString("up"));
            }
        } catch (final IllegalArgumentException ignored) {
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);

        if (this.canBeRotated()) {
            data.setString("forward", this.getForward().name());
            data.setString("up", this.getUp().name());
        }

        if (this.customName != null) {
            data.setString("customName", this.customName);
        }

        return data;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 64, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity pkt) {
        // / pkt.actionType
        if (pkt.getTileEntityType() == 64) {
            this.handleUpdateTag(pkt.getNbtCompound());
        }
    }

    public void onReady() {
    }

    /**
     * This builds a tag with the actual data that should be sent to the client for update syncs.
     * If the tile entity doesn't need update syncs, it returns null.
     */
    private NBTTagCompound writeUpdateData() {
        final NBTTagCompound data = new NBTTagCompound();

        final ByteBuf stream = Unpooled.buffer();

        try {
            this.writeToStream(stream);
            if (stream.readableBytes() == 0) {
                return null;
            }
        } catch (final Throwable t) {
            AELog.debug(t);
        }

        stream.capacity(stream.readableBytes());
        data.setByteArray("X", stream.array());
        return data;
    }

    private boolean readUpdateData(ByteBuf stream) {
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
     * Handles tile entites that are being sent to the client as part of a full chunk.
     */
    @Override
    public NBTTagCompound getUpdateTag() {
        final NBTTagCompound data = this.writeUpdateData();

        if (data == null) {
            return new NBTTagCompound();
        }

        data.setInteger("x", this.pos.getX());
        data.setInteger("y", this.pos.getY());
        data.setInteger("z", this.pos.getZ());
        return data;
    }

    /**
     * Handles tile entites that are being received by the client as part of a full chunk.
     */
    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        final ByteBuf stream = Unpooled.copiedBuffer(tag.getByteArray("X"));

        if (this.readUpdateData(stream)) {
            this.markForUpdate();
        }
    }

    protected boolean readFromStream(final ByteBuf data) throws IOException {
        if (this.canBeRotated()) {
            final EnumFacing old_Forward = this.forward;
            final EnumFacing old_Up = this.up;

            final byte orientation = data.readByte();
            this.forward = EnumFacing.VALUES[orientation & 0x7];
            this.up = EnumFacing.VALUES[orientation >> 3];

            return this.forward != old_Forward || this.up != old_Up;
        }
        return false;
    }

    protected void writeToStream(final ByteBuf data) throws IOException {
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
                AELog.blockUpdate(this.pos, this);
                this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);
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
    public EnumFacing getForward() {
        if (this.forward == null) {
            return EnumFacing.NORTH;
        }
        return this.forward;
    }

    @Override
    public EnumFacing getUp() {
        if (this.up == null) {
            return EnumFacing.UP;
        }
        return this.up;
    }

    @Override
    public void setOrientation(final EnumFacing inForward, final EnumFacing inUp) {
        this.forward = inForward;
        this.up = inUp;
        this.markForUpdate();
        Platform.notifyBlocksOfNeighbors(this.world, this.pos);
    }

    public void onPlacement(final ItemStack stack, final EntityPlayer player, final EnumFacing side) {
        if (stack.hasTagCompound()) {
            this.uploadSettings(SettingsFrom.DISMANTLE_ITEM, stack.getTagCompound());
        }
    }

    /**
     * depending on the from, different settings will be accepted, don't call this with null
     *
     * @param from     source of settings
     * @param compound compound of source
     */
    public void uploadSettings(final SettingsFrom from, final NBTTagCompound compound) {
        if (compound != null && this instanceof IConfigurableObject) {
            final IConfigManager cm = ((IConfigurableObject) this).getConfigManager();
            if (cm != null) {
                cm.readFromNBT(compound);
            }
        }

        if (this instanceof IPriorityHost) {
            final IPriorityHost pHost = (IPriorityHost) this;
            pHost.setPriority(compound.getInteger("priority"));
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
     * @return compound of source
     */
    public NBTTagCompound downloadSettings(final SettingsFrom from) {
        final NBTTagCompound output = new NBTTagCompound();

        if (this.hasCustomInventoryName()) {
            final NBTTagCompound dsp = new NBTTagCompound();
            dsp.setString("Name", this.getCustomInventoryName());
            output.setTag("display", dsp);
        }

        if (this instanceof IConfigurableObject) {
            final IConfigManager cm = ((IConfigurableObject) this).getConfigManager();
            if (cm != null) {
                cm.writeToNBT(output);
            }
        }

        if (this instanceof IPriorityHost) {
            final IPriorityHost pHost = (IPriorityHost) this;
            output.setInteger("priority", pHost.getPriority());
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

        return output.hasNoTags() ? null : output;
    }

    @Override
    public String getCustomInventoryName() {
        return this.hasCustomInventoryName() ? this.customName : this.getClass().getSimpleName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.customName != null && this.customName.length() > 0;
    }

    @Override
    public void setCustomName(@Nullable String customName) {
        setName(customName);
    }

    public void securityBreak() {
        this.world.destroyBlock(this.pos, true);
        this.disableDrops();
    }

    public void disableDrops() {
        DROP_NO_ITEMS.set(new WeakReference<>(this));
    }

    public void saveChanges() {
        if (this.world != null) {
            this.world.markChunkDirty(this.pos, this);
            if (!this.markDirtyQueued) {
                TickHandler.INSTANCE.addCallable(null, this::markDirtyAtEndOfTick);
                this.markDirtyQueued = true;
            }
        }
    }

    private Object markDirtyAtEndOfTick(final World w) {
        this.markDirty();
        this.markDirtyQueued = false;
        return null;
    }

    public boolean requiresTESR() {
        return false;
    }

    public void setName(final String name) {
        this.customName = name;
    }
}
