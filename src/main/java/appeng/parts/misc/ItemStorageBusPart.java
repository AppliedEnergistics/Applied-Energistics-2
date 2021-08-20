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

package appeng.parts.misc;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.blockentity.inventory.AppEngInternalAEInventory;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.ItemStorageBusMenu;
import appeng.parts.PartModel;
import appeng.util.inv.InvOperation;

public class ItemStorageBusPart extends AbstractStorageBusPart<IAEItemStack> {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/item_storage_bus_base");

    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_storage_bus_off"));

    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_storage_bus_on"));

    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_storage_bus_has_channel"));

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 63);

    public ItemStorageBusPart(final ItemStack is) {
        super(TickRates.ItemStorageBus, is);
    }

    @Override
    protected IStorageChannel<IAEItemStack> getStorageChannel() {
        return StorageChannels.items();
    }

    @Nullable
    @Override
    protected IMEInventory<IAEItemStack> getHandlerAdapter(BlockEntity target, Direction targetSide,
            Runnable alertDevice) {
        var itemHandlerOpt = target
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetSide);
        if (itemHandlerOpt.isPresent()) {
            return new ItemHandlerAdapter(itemHandlerOpt.orElse(null), alertDevice);
        }

        return null;
    }

    @Override
    protected int getHandlerHash(BlockEntity target, Direction targetSide) {
        var itemHandlerOpt = target
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetSide);

        if (itemHandlerOpt.isPresent()) {
            var itemHandler = itemHandlerOpt.orElse(null);
            return Objects.hash(target, itemHandler, itemHandler.getSlots());
        } else {
            return 0;
        }
    }

    @Override
    protected int getStackConfigSize() {
        return this.config.getSlots();
    }

    @Nullable
    @Override
    protected IAEItemStack getStackInConfigSlot(int slot) {
        return this.config.getAEStackInSlot(slot);
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);

        if (inv == this.config) {
            this.scheduleCacheReset(true);
        }
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.config.writeToNBT(data, "config");
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }

        return super.getInventoryByName(name);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.ITEM_STORAGE_BUS.stack();
    }

    @Override
    public MenuType<?> getMenuType() {
        return ItemStorageBusMenu.TYPE;
    }
}
