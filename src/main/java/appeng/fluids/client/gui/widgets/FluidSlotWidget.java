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

package appeng.fluids.client.gui.widgets;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Set;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FluidSlotPacket;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;

public class FluidSlotWidget extends CustomSlotWidget {
    private final IAEFluidTank fluids;
    private final int slot;

    public FluidSlotWidget(final IAEFluidTank fluids, final int slot, final int id, final int x, final int y) {
        super(id, x, y);
        this.fluids = fluids;
        this.slot = slot;
    }

    @Override
    public void drawContent(MatrixStack matrixStack, final Minecraft mc, final int mouseX, final int mouseY,
            final float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            // The tooltip area coincides with the area of the slot
            int x = getTooltipAreaX();
            int y = getTooltipAreaY();
            int width = getTooltipAreaWidth();
            int height = getTooltipAreaHeight();
            fs.getFluidStack().renderGuiRect(x, y, x + width, y + height);
        }
    }

    @Override
    public boolean canClick(final PlayerEntity player) {
        final ItemStack mouseStack = player.inventory.getItemStack();
        return mouseStack.isEmpty() || FluidAttributes.EXTRACTABLE.getFirstOrNull(mouseStack) != null;
    }

    @Override
    public void slotClicked(final ItemStack clickStack, int mouseButton) {
        if (clickStack.isEmpty() || mouseButton == 1) {
            this.setFluidStack(null);
        } else if (mouseButton == 0) {
            GroupedFluidInvView groupView = FluidAttributes.GROUPED_INV_VIEW.get(clickStack);
            Set<FluidKey> fluids = groupView.getStoredFluids();
            if (!fluids.isEmpty()) {
                FluidKey firstFluid = groupView.getStoredFluids().iterator().next();
                FluidVolume volume = firstFluid.withAmount(groupView.getAmount_F(firstFluid));
                this.setFluidStack(AEFluidStack.fromFluidVolume(volume, RoundingMode.DOWN));
            }
        }
    }

    @Override
    public ITextComponent getTooltipMessage() {
        final IAEFluidStack fluid = this.getFluidStack();
        if (fluid != null) {
            return fluid.getFluidStack().getName();
        }
        return StringTextComponent.EMPTY;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return true;
    }

    public IAEFluidStack getFluidStack() {
        return this.fluids.getFluidInSlot(this.slot);
    }

    public void setFluidStack(final IAEFluidStack stack) {
        this.fluids.setFluidInSlot(this.slot, stack);
        NetworkHandler.instance()
                .sendToServer(new FluidSlotPacket(Collections.singletonMap(this.getId(), this.getFluidStack())));
    }
}
