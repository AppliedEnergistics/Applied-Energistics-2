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

package appeng.blockentity.crafting;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.stacks.GenericStack;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;

public class CraftingMonitorBlockEntity extends CraftingBlockEntity implements IColorableBlockEntity {

    @OnlyIn(Dist.CLIENT)
    private Integer dspList;

    @OnlyIn(Dist.CLIENT)
    private boolean updateList;

    private GenericStack display;
    private AEColor paintedColor = AEColor.TRANSPARENT;

    public CraftingMonitorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];

        this.display = GenericStack.readBuffer(data);

        this.setUpdateList(true);
        return oldPaintedColor != this.paintedColor || c; // tesr!
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeByte(this.paintedColor.ordinal());

        GenericStack.writeBuffer(display, data);
    }

    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        if (data.contains("paintedColor")) {
            this.paintedColor = AEColor.values()[data.getByte("paintedColor")];
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putByte("paintedColor", (byte) this.paintedColor.ordinal());
    }

    @Override
    public boolean isAccelerator() {
        return false;
    }

    @Override
    public boolean isStatus() {
        return true;
    }

    public void setJob(@Nullable GenericStack stack) {
        if (!Objects.equals(this.display, stack)) {
            this.display = stack;
            this.markForUpdate();
        }
    }

    @Nullable
    public GenericStack getJobProgress() {
        return this.display;
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
    protected ItemStack getItemFromBlockEntity() {
        return AEBlocks.CRAFTING_MONITOR.stack();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new CraftingMonitorModelData(getUp(), getForward(), getConnections(), getColor());
    }

}
