/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AppEng;
import appeng.core.definitions.AEParts;
import appeng.core.settings.TickRates;
import appeng.helpers.IConfigurableFluidInventory;
import appeng.items.parts.PartModels;
import appeng.me.storage.FluidHandlerAdapter;
import appeng.menu.implementations.FluidStorageBusMenu;
import appeng.parts.PartModel;
import appeng.util.fluid.AEFluidInventory;
import appeng.util.fluid.IAEFluidTank;
import appeng.util.inv.IAEFluidInventory;

public class FluidStorageBusPart extends AbstractStorageBusPart<IAEFluidStack, IFluidHandler>
        implements IAEFluidInventory, IConfigurableFluidInventory {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID,
            "part/fluid_storage_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_storage_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_storage_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_storage_bus_has_channel"));

    private final AEFluidInventory config = new AEFluidInventory(this, 63);

    public FluidStorageBusPart(ItemStack is) {
        super(TickRates.FluidStorageBus, is, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }

    @Override
    public IStorageChannel<IAEFluidStack> getStorageChannel() {
        return StorageChannels.fluids();
    }

    @Nullable
    @Override
    protected IMEInventory<IAEFluidStack> getHandlerAdapter(IFluidHandler handler, Runnable alertDevice) {
        return new FluidHandlerAdapter(handler) {
            @Override
            protected void onInjectOrExtract() {
                alertDevice.run();
            }
        };
    }

    @Override
    protected int getStackConfigSize() {
        return this.config.getSlots();
    }

    @Nullable
    @Override
    protected IAEFluidStack getStackInConfigSlot(int slot) {
        return this.config.getFluidInSlot(slot);
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
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

    public IAEFluidTank getConfig() {
        return this.config;
    }

    @Override
    public IFluidHandler getFluidInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

    @Nonnull
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
        return AEParts.FLUID_STORAGE_BUS.stack();
    }

    @Override
    public MenuType<?> getMenuType() {
        return FluidStorageBusMenu.TYPE;
    }
}
