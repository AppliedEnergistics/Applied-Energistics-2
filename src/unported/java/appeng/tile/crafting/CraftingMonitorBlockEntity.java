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

package appeng.tile.crafting;

import java.io.IOException;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;
import net.fabricmc.api.EnvType;


import appeng.api.AEApi;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.util.item.AEItemStack;

public class CraftingMonitorBlockEntity extends CraftingBlockEntity implements IColorableTile {

    @Environment(EnvType.CLIENT)
    private Integer dspList;

    @Environment(EnvType.CLIENT)
    private boolean updateList;

    private IAEItemStack dspPlay;
    private AEColor paintedColor = AEColor.TRANSPARENT;

    public CraftingMonitorBlockEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    protected boolean readFromStream(final PacketByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];

        final boolean hasItem = data.readBoolean();

        if (hasItem) {
            this.dspPlay = AEItemStack.fromPacket(data);
        } else {
            this.dspPlay = null;
        }

        this.setUpdateList(true);
        return oldPaintedColor != this.paintedColor || c; // tesr!
    }

    @Override
    protected void writeToStream(final PacketByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeByte(this.paintedColor.ordinal());

        if (this.dspPlay == null) {
            data.writeBoolean(false);
        } else {
            data.writeBoolean(true);
            this.dspPlay.writeToPacket(data);
        }
    }

    @Override
    public void fromTag(BlockState state, final CompoundTag data) {
        super.fromTag(state, data);
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }
    }

    @Override
    public CompoundTag toTag(final CompoundTag data) {
        super.toTag(data);
        data.putByte("paintedColor", (byte) this.paintedColor.ordinal());
        return data;
    }

    @Override
    public boolean isAccelerator() {
        return false;
    }

    @Override
    public boolean isStatus() {
        return true;
    }

    public void setJob(final IAEItemStack is) {
        if ((is == null) != (this.dspPlay == null)) {
            this.dspPlay = is == null ? null : is.copy();
            this.markForUpdate();
        } else if (is != null && this.dspPlay != null) {
            if (is.getStackSize() != this.dspPlay.getStackSize()) {
                this.dspPlay = is.copy();
                this.markForUpdate();
            }
        }
    }

    public IAEItemStack getJobProgress() {
        return this.dspPlay; // AEItemStack.create( new ItemStack( Items.DIAMOND, 64 ) );
    }

    @Override
    public AEColor getColor() {
        return this.paintedColor;
    }

    @Override
    public boolean recolourBlock(final Direction side, final AEColor newPaintedColor, final PlayerEntity who) {
        if (this.paintedColor == newPaintedColor) {
            return false;
        }

        this.paintedColor = newPaintedColor;
        this.saveChanges();
        this.markForUpdate();
        return true;
    }

    public Integer getDisplayList() {
        return this.dspList;
    }

    public void setDisplayList(final Integer dspList) {
        this.dspList = dspList;
    }

    public boolean isUpdateList() {
        return this.updateList;
    }

    public void setUpdateList(final boolean updateList) {
        this.updateList = updateList;
    }

    @Override
    protected ItemStack getItemFromTile(final Object obj) {
        final Optional<ItemStack> is = AEApi.instance().definitions().blocks().craftingMonitor().maybeStack(1);

        return is.orElseGet(() -> super.getItemFromTile(obj));
    }

    @Nonnull
    @Override
    public CraftingMonitorModelData getRenderAttachmentData() {
        return new CraftingMonitorModelData(getUp(), getForward(), getConnections(), getColor());
    }

}
