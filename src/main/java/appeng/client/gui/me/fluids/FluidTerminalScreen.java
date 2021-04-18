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

package appeng.client.gui.me.fluids;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fluids.FluidAttributes;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.TerminalStyle;
import appeng.client.gui.widgets.IScrollSource;
import appeng.container.me.common.GridInventoryEntry;
import appeng.container.me.fluids.FluidTerminalContainer;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;

public class FluidTerminalScreen extends MEMonitorableScreen<IAEFluidStack, FluidTerminalContainer> {

    public FluidTerminalScreen(FluidTerminalContainer container, PlayerInventory playerInventory,
            ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
    }

    @Override
    protected Repo<IAEFluidStack> createRepo(IScrollSource scrollSource) {
        return new FluidRepo(scrollSource, this);
    }

    @Override
    protected IPartitionList<IAEFluidStack> createPartitionList(List<ItemStack> viewCells) {
        return null;
    }

    @Override
    protected void renderGridInventoryEntry(MatrixStack matrices, int x, int y,
            GridInventoryEntry<IAEFluidStack> entry) {
        RenderSystem.disableBlend();
        IAEFluidStack fs = entry.getStack();
        final Fluid fluid = fs.getFluid();
        FluidAttributes fluidAttributes = fluid.getAttributes();
        bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        ResourceLocation fluidStillTexture = fluidAttributes.getStillTexture(fs.getFluidStack());
        final TextureAtlasSprite sprite = getMinecraft()
                .getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(fluidStillTexture);

        // Set color for dynamic fluids
        // Convert int color to RGB
        float red = (fluidAttributes.getColor() >> 16 & 255) / 255.0F;
        float green = (fluidAttributes.getColor() >> 8 & 255) / 255.0F;
        float blue = (fluidAttributes.getColor() & 255) / 255.0F;
        RenderSystem.color3f(red, green, blue);

        blit(matrices, x, y, getBlitOffset(), 16, 16, sprite);
        RenderSystem.enableBlend();
    }

    @Override
    protected void renderGridInventoryEntryTooltip(MatrixStack matrices, GridInventoryEntry<IAEFluidStack> entry, int x,
            int y) {
        IAEFluidStack fluidStack = entry.getStack();
        String formattedAmount = NumberFormat.getNumberInstance(Locale.US)
                .format(entry.getStoredAmount() / 1000.0) + " B";

        String modName = Platform.getModName(Platform.getModId(fluidStack));

        List<ITextComponent> list = new ArrayList<>();
        list.add(fluidStack.getFluidStack().getDisplayName());
        list.add(new StringTextComponent(formattedAmount));
        list.add(new StringTextComponent(modName));

        this.func_243308_b(matrices, list, x, y);
    }

    @Override
    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<IAEFluidStack> entry,
            int mouseButton, ClickType clickType) {
        if (clickType == ClickType.PICKUP) {
            // TODO: Allow more options
            if (mouseButton == 0 && entry != null) {
                AELog.debug("mouse0 GUI STACK SIZE %s", entry.getStoredAmount());
                container.handleInteraction(entry.getSerial(), InventoryAction.FILL_ITEM);
            } else {
                if (entry != null) {
                    AELog.debug("mouse1 GUI STACK SIZE %s", entry.getStoredAmount());
                }
                container.handleInteraction(-1, InventoryAction.EMPTY_ITEM);
            }
        }
    }

}
