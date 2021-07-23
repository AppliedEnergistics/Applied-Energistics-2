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

import javax.annotation.Nonnull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.util.item.AEItemStack;

public class CraftingMonitorTileEntity extends CraftingTileEntity implements IColorableTile {

    @OnlyIn(Dist.CLIENT)
    private Integer dspList;

    @OnlyIn(Dist.CLIENT)
    private boolean updateList;

    private IAEItemStack dspPlay;
    private AEColor paintedColor = AEColor.TRANSPARENT;

    public CraftingMonitorTileEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
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
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
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
    public void load(BlockState blockState, final CompoundTag data) {
        super.load(blockState, data);
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
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
        if (is == null != (this.dspPlay == null)) {
            this.dspPlay = is == null ? null : is.copy();
            this.markForUpdate();
        } else if (is != null && this.dspPlay != null && is.getStackSize() != this.dspPlay.getStackSize()) {
            this.dspPlay = is.copy();
            this.markForUpdate();
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
    public boolean recolourBlock(final Direction side, final AEColor newPaintedColor, final Player who) {
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
    protected ItemStack getItemFromTile() {
        return AEBlocks.CRAFTING_MONITOR.stack();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new CraftingMonitorModelData(getUp(), getForward(), getConnections(), getColor());
    }

}
