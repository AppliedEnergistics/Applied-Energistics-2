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

package appeng.fluids.util;


import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;
import appeng.fluids.items.FluidDummyItem;
import appeng.util.Platform;
import appeng.util.item.AEStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;


public final class AEFluidStack extends AEStack<IAEFluidStack> implements IAEFluidStack, Comparable<AEFluidStack> {

    private final Fluid fluid;
    private NBTTagCompound tagCompound;

    private AEFluidStack(final AEFluidStack fluidStack) {
        this.fluid = fluidStack.fluid;
        this.setStackSize(fluidStack.getStackSize());

        // priority = is.priority;
        this.setCraftable(fluidStack.isCraftable());
        this.setCountRequestable(fluidStack.getCountRequestable());

        if (fluidStack.hasTagCompound()) {
            this.tagCompound = fluidStack.tagCompound.copy();
        }
    }

    private AEFluidStack(@Nonnull final FluidStack fluidStack) {
        this.fluid = fluidStack.getFluid();

        if (this.fluid == null) {
            throw new IllegalArgumentException("Fluid is null.");
        }

        this.setStackSize(fluidStack.amount);
        this.setCraftable(false);
        this.setCountRequestable(0);

        if (fluidStack.tag != null) {
            this.tagCompound = fluidStack.tag.copy();
        }
    }

    public static AEFluidStack fromFluidStack(final FluidStack input) {
        if (input == null) {
            return null;
        }

        return new AEFluidStack(input);
    }

    public static IAEFluidStack fromNBT(final NBTTagCompound data) {
        final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(data);

        if (fluidStack == null) {
            return null;
        }

        final AEFluidStack fluid = AEFluidStack.fromFluidStack(fluidStack);
        fluid.setStackSize(data.getLong("Cnt"));
        fluid.setCountRequestable(data.getLong("Req"));
        fluid.setCraftable(data.getBoolean("Craft"));

        if (fluid.hasTagCompound()) {
            fluid.tagCompound = fluid.tagCompound.copy();
        }

        return fluid;
    }

    public static IAEFluidStack fromPacket(final ByteBuf buffer) throws IOException {
        final byte mask = buffer.readByte();
        final byte stackType = (byte) ((mask & 0x0C) >> 2);
        final byte countReqType = (byte) ((mask & 0x30) >> 4);
        final boolean isCraftable = (mask & 0x40) > 0;
        final boolean hasTagCompound = (mask & 0x80) > 0;

        // don't send this...
        final NBTTagCompound d = new NBTTagCompound();

        final byte len2 = buffer.readByte();
        final byte[] name = new byte[len2];
        buffer.readBytes(name, 0, len2);

        d.setString("FluidName", new String(name, StandardCharsets.UTF_8));
        d.setByte("Count", (byte) 0);

        if (hasTagCompound) {
            final int len = buffer.readInt();

            final byte[] bd = new byte[len];
            buffer.readBytes(bd);

            final DataInputStream di = new DataInputStream(new ByteArrayInputStream(bd));
            d.setTag("Tag", CompressedStreamTools.read(di));
        }

        final long stackSize = getPacketValue(stackType, buffer);
        final long countRequestable = getPacketValue(countReqType, buffer);

        final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(d);

        if (fluidStack == null) {
            return null;
        }

        final AEFluidStack fluid = AEFluidStack.fromFluidStack(fluidStack);
        // fluid.priority = (int) priority;
        fluid.setStackSize(stackSize);
        fluid.setCountRequestable(countRequestable);
        fluid.setCraftable(isCraftable);
        return fluid;
    }

    @Override
    public void add(final IAEFluidStack option) {
        if (option == null) {
            return;
        }
        this.incStackSize(option.getStackSize());
        this.setCountRequestable(this.getCountRequestable() + option.getCountRequestable());
        this.setCraftable(this.isCraftable() || option.isCraftable());
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        data.setString("FluidName", this.fluid.getName());
        data.setByte("Count", (byte) 0);
        data.setLong("Cnt", this.getStackSize());
        data.setLong("Req", this.getCountRequestable());
        data.setBoolean("Craft", this.isCraftable());

        if (this.hasTagCompound()) {
            data.setTag("Tag", this.tagCompound);
        } else {
            data.removeTag("Tag");
        }
    }

    @Override
    public boolean fuzzyComparison(final IAEFluidStack other, final FuzzyMode mode) {
        return this.fluid == other.getFluid();
    }

    @Override
    public IAEFluidStack copy() {
        return new AEFluidStack(this);
    }

    @Override
    public IAEFluidStack empty() {
        final IAEFluidStack dup = this.copy();
        dup.reset();
        return dup;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return true;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public int compareTo(final AEFluidStack other) {
        if (this.fluid != other.fluid) {
            return this.fluid.getName().compareTo(other.fluid.getName());
        }

        if (Platform.itemComparisons().isNbtTagEqual(this.tagCompound, other.tagCompound)) {
            return 0;
        }

        return this.tagCompound.hashCode() - other.tagCompound.hashCode();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.fluid == null) ? 0 : this.fluid.hashCode());
        result = prime * result + ((this.tagCompound == null) ? 0 : this.tagCompound.hashCode());

        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AEFluidStack) {
            final AEFluidStack is = (AEFluidStack) other;
            return is.fluid == this.fluid && Platform.itemComparisons().isNbtTagEqual(this.tagCompound, is.tagCompound);
        } else if (other instanceof FluidStack) {
            final FluidStack is = (FluidStack) other;
            return is.getFluid() == this.fluid && Platform.itemComparisons().isNbtTagEqual(this.tagCompound, is.tag);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getStackSize() + "x" + this.getFluidStack().getFluid().getName() + " " + this.tagCompound;
    }

    @Override
    public boolean hasTagCompound() {
        return this.tagCompound != null;
    }

    @Override
    public FluidStack getFluidStack() {
        final int amount = (int) Math.min(Integer.MAX_VALUE, this.getStackSize());
        final FluidStack is = new FluidStack(this.fluid, amount, this.tagCompound);

        return is;
    }

    @Override
    public Fluid getFluid() {
        return this.fluid;
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        ItemStack is = Api.INSTANCE.definitions().items().dummyFluidItem().maybeStack(1).orElse(ItemStack.EMPTY);
        if (!is.isEmpty()) {
            FluidDummyItem item = (FluidDummyItem) is.getItem();
            item.setFluidStack(is, this.getFluidStack());
            return is;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void writeToPacket(final ByteBuf buffer) throws IOException {
        final byte mask = (byte) ((this.getType(this.getStackSize()) << 2) | (this
                .getType(this.getCountRequestable()) << 4) | ((byte) (this.isCraftable() ? 1 : 0) << 6) | (this.hasTagCompound() ? 1 : 0) << 7);

        buffer.writeByte(mask);

        this.writeToStream(buffer);

        this.putPacketValue(buffer, this.getStackSize());
        this.putPacketValue(buffer, this.getCountRequestable());
    }

    private void writeToStream(final ByteBuf buffer) throws IOException {
        final byte[] name = this.fluid.getName().getBytes(StandardCharsets.UTF_8);
        buffer.writeByte((byte) name.length);
        buffer.writeBytes(name);
        if (this.hasTagCompound()) {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final DataOutputStream data = new DataOutputStream(bytes);

            CompressedStreamTools.write(this.tagCompound, data);

            final byte[] tagBytes = bytes.toByteArray();
            final int size = tagBytes.length;

            buffer.writeInt(size);
            buffer.writeBytes(tagBytes);
        }
    }
}
