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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.networking.events.GridCellArrayUpdate;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.core.definitions.AEParts;
import appeng.helpers.IConfigurableFluidInventory;
import appeng.items.parts.PartModels;
import appeng.me.storage.MEInventoryHandler;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.FluidFormationPlaneMenu;
import appeng.util.fluid.AEFluidInventory;
import appeng.util.fluid.IAEFluidTank;
import appeng.util.inv.IAEFluidInventory;
import appeng.util.prioritylist.PrecisePriorityList;

public class FluidFormationPlanePart extends AbstractFormationPlanePart<IAEFluidStack>
        implements IAEFluidInventory, IConfigurableFluidInventory {
    private static final PlaneModels MODELS = new PlaneModels("part/fluid_formation_plane",
            "part/fluid_formation_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final MEInventoryHandler<IAEFluidStack> myHandler = new MEInventoryHandler<>(this,
            StorageChannels.fluids());
    private final AEFluidInventory config = new AEFluidInventory(this, 63);

    public FluidFormationPlanePart(final ItemStack is) {
        super(is);
        this.updateHandler();
    }

    @Override
    protected void updateHandler() {
        this.myHandler.setBaseAccess(AccessRestriction.WRITE);
        this.myHandler.setWhitelist(
                this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        this.myHandler.setPriority(this.getPriority());

        final IItemList<IAEFluidStack> priorityList = StorageChannels.fluids().createList();

        final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
        for (int x = 0; x < this.config.getSlots() && x < slotsToUse; x++) {
            final IAEFluidStack is = this.config.getFluidInSlot(x);
            if (is != null) {
                priorityList.add(is);
            }
        }
        this.myHandler.setPartitionList(new PrecisePriorityList<>(priorityList));

        getMainNode().ifPresent(g -> g.postEvent(new GridCellArrayUpdate()));
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, IActionSource src) {
        if (this.blocked || input == null || input.getStackSize() < FluidAttributes.BUCKET_VOLUME) {
            // need a full bucket
            return input;
        }

        final BlockEntity te = this.getHost().getBlockEntity();
        final Level level = te.getLevel();
        final AEPartLocation side = this.getSide();
        final BlockPos pos = te.getBlockPos().relative(side.getDirection());
        final BlockState state = level.getBlockState(pos);

        if (this.canReplace(level, state, pos)) {
            if (type == Actionable.MODULATE) {
                final FluidStack fs = input.getFluidStack();
                fs.setAmount(FluidAttributes.BUCKET_VOLUME);

                final FluidTank tank = new FluidTank(FluidAttributes.BUCKET_VOLUME);
                tank.fill(fs, IFluidHandler.FluidAction.EXECUTE);

                FakePlayer fakePlayer = FakePlayerFactory.getMinecraft((ServerLevel) level);
                if (!FluidUtil.tryPlaceFluid(fakePlayer, level, InteractionHand.MAIN_HAND, pos, tank, fs)) {
                    return input;
                }
            }
            final IAEFluidStack ret = input.copy();
            ret.setStackSize(input.getStackSize() - FluidAttributes.BUCKET_VOLUME);
            return ret.getStackSize() == 0 ? null : ret;
        }
        this.blocked = true;
        return input;
    }

    private boolean canReplace(Level level, BlockState state, BlockPos pos) {
        return state.getMaterial().isReplaceable() && level.getFluidState(pos).isEmpty()
                && !state.getMaterial().isLiquid();
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        if (inv == this.config) {
            this.updateHandler();
        }
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.config.readFromNBT(data, "config");
        this.updateHandler();
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.config.writeToNBT(data, "config");
    }

    @Override
    public IFluidHandler getFluidInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(FluidFormationPlaneMenu.TYPE, player, MenuLocator.forPart(this));
        }

        return true;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return StorageChannels.fluids();
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (this.getMainNode().isActive()
                && channel == StorageChannels.fluids()) {
            final List<IMEInventoryHandler> handler = new ArrayList<>(1);
            handler.add(this.myHandler);
            return handler;
        }
        return Collections.emptyList();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new PlaneModelData(getConnections());
    }

    public IAEFluidTank getConfig() {
        return this.config;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEParts.FLUID_FORMATION_PLANE.stack();
    }

    @Override
    public MenuType<?> getMenuType() {
        return FluidFormationPlaneMenu.TYPE;
    }
}
