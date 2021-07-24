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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.IIngredientSupplier;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.FluidSlotPacket;
import appeng.fluids.client.gui.FluidBlitter;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;

public class FluidSlotWidget extends CustomSlotWidget implements IIngredientSupplier {
    private final IAEFluidTank fluids;

    public FluidSlotWidget(IAEFluidTank fluids, int slot) {
        super(slot);
        this.fluids = fluids;
    }

    @Override
    public void drawContent(PoseStack matrixStack, final Minecraft mc, final int mouseX, final int mouseY,
                            final float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            FluidBlitter.create(fs.getFluidStack())
                    .dest(getTooltipAreaX(), getTooltipAreaY(), getTooltipAreaWidth(), getTooltipAreaHeight())
                    .blit(matrixStack, getBlitOffset());
        }
    }

    @Override
    public boolean canClick(final Player player) {
        if (player.containerMenu == null) {
            return false;
        }
        final ItemStack mouseStack = player.containerMenu.getCarried();
        return mouseStack.isEmpty()
                || mouseStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
    }

    @Override
    public void slotClicked(final ItemStack clickStack, int mouseButton) {
        if (clickStack.isEmpty() || mouseButton == 1) {
            this.setFluidStack(null);
        } else if (mouseButton == 0) {
            final Optional<FluidStack> fluidOpt = FluidUtil.getFluidContained(clickStack);
            fluidOpt.ifPresent(fluid -> {
                this.setFluidStack(AEFluidStack.fromFluidStack(fluid));
            });
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        final IAEFluidStack fluid = this.getFluidStack();
        if (fluid != null) {
            return Collections.singletonList(new TranslatableComponent(fluid.getFluidStack().getTranslationKey()));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return true;
    }

    public IAEFluidStack getFluidStack() {
        return this.fluids.getFluidInSlot(getId());
    }

    public void setFluidStack(final IAEFluidStack stack) {
        this.fluids.setFluidInSlot(getId(), stack);
        NetworkHandler.instance()
                .sendToServer(new FluidSlotPacket(Collections.singletonMap(this.getId(), this.getFluidStack())));
    }

    @Nullable
    @Override
    public FluidStack getFluidIngredient() {
        IAEFluidStack fluidStack = getFluidStack();
        if (fluidStack != null) {
            return fluidStack.getFluidStack();
        }
        return null;
    }

}
