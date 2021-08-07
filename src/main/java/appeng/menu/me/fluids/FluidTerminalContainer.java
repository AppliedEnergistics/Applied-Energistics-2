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

package appeng.menu.me.fluids;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.menu.implementations.ContainerTypeBuilder;
import appeng.menu.me.common.MEMonitorableContainer;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.fluid.AEFluidStack;
import appeng.util.fluid.FluidSoundHelper;

/**
 * @see appeng.client.gui.me.fluids.FluidTerminalScreen
 * @since rv6 12/05/2018
 */
public class FluidTerminalContainer extends MEMonitorableContainer<IAEFluidStack> {

    public static final MenuType<FluidTerminalContainer> TYPE = ContainerTypeBuilder
            .create(FluidTerminalContainer::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_terminal");

    public FluidTerminalContainer(int id, Inventory ip, ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public FluidTerminalContainer(MenuType<?> containerType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory) {
        super(containerType, id, ip, host, bindInventory,
                Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));
    }

    @Override
    protected boolean hideViewCells() {
        return true;
    }

    @Override
    protected void handleNetworkInteraction(ServerPlayer player, @Nullable IAEFluidStack stack,
            InventoryAction action) {

        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            return;
        }

        var carried = getCarried();
        if (carried.getCount() != 1) {
            // only support stacksize 1 for now, since filled items are _usually_ not stackable
            return;
        }

        final LazyOptional<IFluidHandlerItem> fhOpt = FluidUtil.getFluidHandler(carried);
        if (!fhOpt.isPresent()) {
            // only fluid handlers items
            return;
        }
        IFluidHandlerItem fh = fhOpt.orElse(null);

        if (action == InventoryAction.FILL_ITEM && stack != null) {
            // Check how much we can store in the item
            stack.setStackSize(Integer.MAX_VALUE);
            int amountAllowed = fh.fill(stack.getFluidStack(), FluidAction.SIMULATE);
            stack.setStackSize(amountAllowed);

            // Check if we can pull out of the system
            final IAEFluidStack canPull = Platform.poweredExtraction(this.powerSource, this.monitor, stack,
                    this.getActionSource(), Actionable.SIMULATE);
            if (canPull == null || canPull.getStackSize() < 1) {
                return;
            }

            // How much could fit into the menu
            final int canFill = fh.fill(canPull.getFluidStack(), FluidAction.SIMULATE);
            if (canFill == 0) {
                return;
            }

            // Now actually pull out of the system
            stack.setStackSize(canFill);
            final IAEFluidStack pulled = Platform.poweredExtraction(this.powerSource, this.monitor, stack,
                    this.getActionSource());
            if (pulled == null || pulled.getStackSize() < 1) {
                // Something went wrong
                AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
                return;
            }

            // Actually fill
            final int used = fh.fill(pulled.getFluidStack(), FluidAction.EXECUTE);

            if (used != canFill) {
                AELog.error("Fluid item [%s] reported a different possible amount than it actually accepted.",
                        carried.getHoverName());
            }

            setCarried(fh.getContainer());
            FluidSoundHelper.playFillSound(player, pulled.getFluidStack());
        } else if (action == InventoryAction.EMPTY_ITEM) {
            // See how much we can drain from the item
            final FluidStack extract = fh.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            if (extract.isEmpty() || extract.getAmount() < 1) {
                return;
            }

            // Check if we can push into the system
            final IAEFluidStack notStorable = Platform.poweredInsert(this.powerSource, this.monitor,
                    AEFluidStack.fromFluidStack(extract), this.getActionSource(), Actionable.SIMULATE);

            if (notStorable != null && notStorable.getStackSize() > 0) {
                final int toStore = (int) (extract.getAmount() - notStorable.getStackSize());
                final FluidStack storable = fh.drain(toStore, FluidAction.SIMULATE);

                if (storable.isEmpty() || storable.getAmount() == 0) {
                    return;
                } else {
                    extract.setAmount(storable.getAmount());
                }
            }

            // Actually drain
            final FluidStack drained = fh.drain(extract, FluidAction.EXECUTE);
            extract.setAmount(drained.getAmount());

            final IAEFluidStack notInserted = Platform.poweredInsert(this.powerSource, this.monitor,
                    AEFluidStack.fromFluidStack(extract), this.getActionSource());

            if (notInserted != null && notInserted.getStackSize() > 0) {
                AELog.error("Fluid item [%s] reported a different possible amount to drain than it actually provided.",
                        carried.getHoverName());
            }

            setCarried(fh.getContainer());
            FluidSoundHelper.playEmptySound(player, extract);
        }
    }

}
