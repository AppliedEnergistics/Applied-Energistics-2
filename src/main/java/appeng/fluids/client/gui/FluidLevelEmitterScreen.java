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

package appeng.fluids.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.implementations.NumberEntryWidget;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.container.SlotSemantic;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.container.FluidLevelEmitterContainer;
import net.minecraft.world.entity.player.Inventory;

public class FluidLevelEmitterScreen extends UpgradeableScreen<FluidLevelEmitterContainer> {

    private final SettingToggleButton<RedstoneMode> redstoneMode;

    private final NumberEntryWidget level;

    public FluidLevelEmitterScreen(FluidLevelEmitterContainer container, Inventory playerInventory,
                                   net.minecraft.network.chat.Component title, ScreenStyle style) {
        super(container, playerInventory, title, style);

        addSlot(new FluidSlotWidget(this.menu.getFluidConfigInventory(), 0), SlotSemantic.CONFIG);

        this.level = new NumberEntryWidget(NumberEntryType.LEVEL_FLUID_VOLUME);
        this.level.setTextFieldBounds(25, 44, 75);
        this.level.setValue(container.getReportingValue());
        this.level.setOnChange(this::saveReportingValue);
        this.level.setOnConfirm(this::onClose);
        widgets.add("level", this.level);

        this.redstoneMode = new ServerSettingToggleButton<>(
                Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);
        this.addToLeftToolbar(this.redstoneMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.redstoneMode.set(this.menu.getRedStoneMode());
    }

    private void saveReportingValue() {
        this.level.getLongValue().ifPresent(menu::setReportingValue);
    }

    @Override
    public void drawBG(PoseStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
                       final int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);
        this.level.render(matrixStack, mouseX, mouseY, partialTicks);
    }

}
