/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.automation;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.capability.IFluidHandler;

import appeng.api.parts.IPartModel;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AppEng;
import appeng.helpers.IConfigurableFluidInventory;
import appeng.items.parts.PartModels;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.FluidLevelEmitterMenu;
import appeng.parts.PartModel;
import appeng.util.fluid.AEFluidInventory;
import appeng.util.fluid.IAEFluidTank;
import appeng.util.inv.IAEFluidInventory;

public class FluidLevelEmitterPart extends AbstractStorageLevelEmitterPart<IAEFluidStack> implements
        IAEFluidInventory, IConfigurableFluidInventory {
    @PartModels
    public static final ResourceLocation MODEL_BASE_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_base_off");
    @PartModels
    public static final ResourceLocation MODEL_BASE_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_base_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_has_channel");

    public static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
    public static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
    public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    public static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
    public static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
    public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);

    private final AEFluidInventory config = new AEFluidInventory(this, 1);

    public FluidLevelEmitterPart(ItemStack is) {
        super(is, false);
    }

    @Nullable
    @Override
    protected IAEFluidStack getConfiguredStack() {
        return config.getFluidInSlot(0);
    }

    @Override
    protected IStorageChannel<IAEFluidStack> getChannel() {
        return StorageChannels.fluids();
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        this.configureWatchers();
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(FluidLevelEmitterMenu.TYPE, player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_ON : MODEL_OFF_ON;
        } else {
            return this.isLevelEmitterOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
        }
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

}
