/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.p2p;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;

public class ItemP2PTunnelPart extends P2PTunnelPart<ItemP2PTunnelPart> {
    private static final float POWER_DRAIN = 2.0f;
    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_items");
    private static final IItemHandler NULL_ITEM_HANDLER = new NullItemHandler();

    private final IItemHandler inputHandler = new InputItemHandler();
    private final IItemHandler outputHandler = new OutputItemHandler();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public ItemP2PTunnelPart(final ItemStack is) {
        super(is);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return POWER_DRAIN;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass) {
        if (capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (this.isOutput()) {
                return (LazyOptional<T>) LazyOptional.of(() -> this.outputHandler);
            }
            return (LazyOptional<T>) LazyOptional.of(() -> this.inputHandler);
        }

        return super.getCapability(capabilityClass);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private IItemHandler getAttachedItemHandler() {
        LazyOptional<IItemHandler> itemHandler = LazyOptional.empty();
        if (this.isActive()) {
            final TileEntity self = this.getTile();
            final TileEntity te = self.getWorld().getTileEntity(self.getPos().offset(this.getSide().getDirection()));

            if (te != null) {
                itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                        this.getSide().getOpposite().getDirection());
            }
        }
        return itemHandler.orElse(NULL_ITEM_HANDLER);
    }

    private class InputItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            final ItemStack remainder = stack.copy();

            final int outputTunnels = ItemP2PTunnelPart.this.getOutputs().size();
            final int amount = stack.getCount();

            if (outputTunnels == 0 || amount == 0) {
                return stack;
            }

            final int amountPerOutput = Math.max(1, amount / outputTunnels);
            int overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (ItemP2PTunnelPart target : ItemP2PTunnelPart.this.getOutputs()) {
                final IItemHandler output = target.getAttachedItemHandler();
                final int toSend = amountPerOutput + overflow;
                final ItemStack fillWithItemStack = stack.copy();
                fillWithItemStack.setCount(toSend);

                ItemStack received = ItemStack.EMPTY;

                for (int i = 0; i < output.getSlots(); i++) {
                    received = output.insertItem(i, fillWithItemStack, simulate);

                    if (received.isEmpty()) {
                        break;
                    }
                }

                overflow = received.getCount();
                remainder.setCount(remainder.getCount() - toSend - received.getCount());

                if (remainder.isEmpty()) {
                    break;
                }
            }

            if (!simulate) {
                ItemP2PTunnelPart.this.queueTunnelDrain(PowerUnits.RF, stack.getCount() - remainder.getCount());
            }

            return remainder;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }

    }

    private class OutputItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return ItemP2PTunnelPart.this.getInput().getAttachedItemHandler().getSlots();
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return ItemP2PTunnelPart.this.getInput().getAttachedItemHandler().getStackInSlot(slot);
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemP2PTunnelPart.this.getInput().getAttachedItemHandler().extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return ItemP2PTunnelPart.this.getInput().getAttachedItemHandler().getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return ItemP2PTunnelPart.this.getInput().getAttachedItemHandler().isItemValid(slot, stack);
        }
    }

    private static class NullItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            return 0;
        }

        @Override
        @Nonnull
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        @Nonnull
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        @Nonnull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 0;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return false;
        }
    }
}
