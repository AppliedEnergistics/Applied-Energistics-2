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

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.implementations.blockentities.IColorableBlockEntity;
import appeng.api.stacks.GenericStack;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;

public class CraftingMonitorBlockEntity extends CraftingBlockEntity implements IColorableBlockEntity {

    private GenericStack display;
    private AEColor paintedColor = AEColor.TRANSPARENT;

    public CraftingMonitorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final AEColor oldPaintedColor = this.paintedColor;
        this.paintedColor = AEColor.values()[data.readByte()];

        this.display = GenericStack.readBuffer(data);

        return oldPaintedColor != this.paintedColor || c; // Block Entity Renderer takes care of display
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeByte(this.paintedColor.ordinal());

        GenericStack.writeBuffer(display, data);
    }

    @Override
    public void loadTag(CompoundTag data) {
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
    public boolean recolourBlock(Direction side, AEColor newPaintedColor, Player who) {
        if (this.paintedColor == newPaintedColor) {
            return false;
        }

        this.paintedColor = newPaintedColor;
        this.saveChanges();
        this.markForUpdate();
        return true;
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return AEBlocks.CRAFTING_MONITOR.asItem();
    }

    @Override
    public Object getRenderAttachmentData() {
        return new CraftingMonitorModelData(getUp(), getForward(), getConnections(), getColor());
    }

    @Override
    public boolean canBeRotated() {
        return true;
    }
}
