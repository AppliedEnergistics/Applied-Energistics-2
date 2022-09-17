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


import appeng.api.AEApi;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.Optional;


public class TileCraftingMonitorTile extends TileCraftingTile implements IColorableTile {

    @SideOnly(Side.CLIENT)
    private Integer dspList;

    @SideOnly(Side.CLIENT)
    private boolean updateList;

    private IAEItemStack dspPlay;
    private AEColor paintedColor = AEColor.TRANSPARENT;

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
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
    protected void writeToStream(final ByteBuf data) throws IOException {
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
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setByte("paintedColor", (byte) this.paintedColor.ordinal());
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
    public boolean requiresTESR() {
        return this.dspPlay != null;
    }

    @Override
    public AEColor getColor() {
        return this.paintedColor;
    }

    @Override
    public boolean recolourBlock(final EnumFacing side, final AEColor newPaintedColor, final EntityPlayer who) {
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
}
