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

package appeng.container.me.fluids;

import java.math.RoundingMode;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.ExactFluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.misc.Ref;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.me.common.MEMonitorableContainer;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.FluidSoundHelper;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;

/**
 * @see appeng.client.gui.me.fluids.FluidTerminalScreen
 * @since rv6 12/05/2018
 */
public class FluidTerminalContainer extends MEMonitorableContainer<IAEFluidStack> {

    public static final ContainerType<FluidTerminalContainer> TYPE = ContainerTypeBuilder
            .create(FluidTerminalContainer::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_terminal");

    public FluidTerminalContainer(int id, PlayerInventory ip, ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public FluidTerminalContainer(ContainerType<?> containerType, int id, PlayerInventory ip, ITerminalHost host,
            boolean bindInventory) {
        super(containerType, id, ip, host, bindInventory,
                Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));
    }

    @Override
    protected boolean hideViewCells() {
        return true;
    }

    @Override
    protected void handleNetworkInteraction(ServerPlayerEntity player, @Nullable IAEFluidStack stack,
            InventoryAction action) {

        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            return;
        }

        final ItemStack held = player.inventory.getItemStack();
        if (held.getCount() != 1) {
            // only support stacksize 1 for now, since filled items are _usually_ not stackable
            return;
        }

        if (action == InventoryAction.FILL_ITEM && stack != null) {
            Ref<ItemStack> container = new Ref<>(held);
            FluidInsertable insertable = FluidAttributes.INSERTABLE.getFirstOrNull(container);
            if (insertable == null) {
                return; // Not a fillable item.
            }

            // Check how much we can store in the item
            FluidVolume volumeOverflow = insertable.attemptInsertion(
                    stack.getFluidStack().withAmount(FluidAmount.ofWhole(Long.MAX_VALUE)),
                    Simulation.SIMULATE);
            FluidAmount amountAllowed = FluidAmount.ofWhole(Long.MAX_VALUE).saturatedSub(volumeOverflow.amount());

            AEFluidStack actualStack = AEFluidStack.fromFluidVolume(
                    stack.getFluidStack().withAmount(amountAllowed), RoundingMode.DOWN);
            if (actualStack == null) {
                return; // Might be nothing allowed...
            }

            // Check if we can pull out of the system
            final IAEFluidStack canPull = Platform.poweredExtraction(this.powerSource, this.monitor, actualStack,
                    this.getActionSource(), Actionable.SIMULATE);
            if (canPull == null || canPull.getStackSize() < 1) {
                return;
            }

            // How much could fit into the container
            volumeOverflow = insertable.attemptInsertion(canPull.getFluidStack(), Simulation.SIMULATE);
            if (!volumeOverflow.isEmpty()) {
                return;
            }

            // Now actually pull out of the system
            actualStack.setStackSize(canPull.getStackSize());
            final IAEFluidStack pulled = Platform.poweredExtraction(this.powerSource, this.monitor, actualStack,
                    this.getActionSource());
            if (pulled == null || pulled.getStackSize() < 1) {
                // Something went wrong
                AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
                return;
            }

            // Actually fill
            volumeOverflow = insertable.attemptInsertion(pulled.getFluidStack(), Simulation.ACTION);

            if (!volumeOverflow.isEmpty()) {
                AELog.error(
                        "Fluid item [%s] reported a different possible amount than it actually accepted. Overflow: %s.",
                        held, volumeOverflow);
            }

            player.inventory.setItemStack(container.get());
            this.updateHeld(player);
            FluidSoundHelper.playFillSound(player, pulled.getFluidStack());
        } else if (action == InventoryAction.EMPTY_ITEM) {
            Ref<ItemStack> container = new Ref<>(held);
            FluidExtractable extractable = FluidAttributes.EXTRACTABLE.getFirstOrNull(container);
            if (extractable == null) {
                return; // Not a drainable item.
            }

            // See how much we can drain from the item
            FluidVolume extract = extractable.attemptAnyExtraction(FluidAmount.MAX_BUCKETS, Simulation.SIMULATE);
            AEFluidStack aeStack = AEFluidStack.fromFluidVolume(extract, RoundingMode.DOWN);
            if (aeStack == null || aeStack.getStackSize() == 0) {
                return; // Not enough liquid in the container
            }

            // Check if we can push into the system
            final IAEFluidStack notStorable = Platform.poweredInsert(this.powerSource, this.monitor, aeStack,
                    this.getActionSource(), Actionable.SIMULATE);

            // Adjust the amount if needed
            if (notStorable != null && notStorable.getStackSize() > 0) {
                final FluidAmount toStore = aeStack.getAmount().sub(notStorable.getAmount());
                extract = extractable.attemptExtraction(new ExactFluidFilter(aeStack.getFluid()), toStore,
                        Simulation.SIMULATE);

                if (extract.isEmpty()) {
                    return;
                }
            }

            // Actually drain
            FluidVolume drained = extractable.extract(extract.getFluidKey(), extract.amount());
            aeStack = AEFluidStack.fromFluidVolume(drained, RoundingMode.DOWN);
            if (aeStack == null) {
                // TODO WARN
                return; // I guess the extractable decided to return a different amount upon execution
            }

            final IAEFluidStack notInserted = Platform.poweredInsert(this.powerSource, this.monitor, aeStack,
                    this.getActionSource());

            if (notInserted != null && notInserted.getStackSize() > 0) {
                AELog.error("Fluid item [%s] reported a different possible amount to drain than it actually provided.",
                        held.getDisplayName());
            }

            player.inventory.setItemStack(container.get());
            this.updateHeld(player);
        }
    }

}
