/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.me.items;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import appeng.api.config.ActionItems;
import appeng.api.stacks.GenericStack;
import appeng.client.Point;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.AEConfig;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantics;
import appeng.menu.me.interaction.EmptyingAction;
import appeng.menu.me.interaction.StackInteractions;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;

public class PatternEncodingTermScreen<C extends PatternEncodingTermMenu> extends MEStorageScreen<C> {

    private static final String MODES_TEXTURE = "guis/pattern_modes.png";

    private static final Blitter CRAFTING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 0, 126, 68);

    private static final Blitter PROCESSING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 70, 126, 68);

    private final TabButton tabCraftButton;
    private final TabButton tabProcessButton;
    private final Multimap<EncodingMode, IconButton> buttonsByMode = ArrayListMultimap.create();
    private final ToggleButton craftingSubstitutionsBtn;
    private final ToggleButton craftingFluidSubstitutionsBtn;
    private final ActionButton processingCycleOutputBtn;
    private final Scrollbar processingScrollbar;

    public PatternEncodingTermScreen(C menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        // Add buttons for the crafting mode
        ActionButton craftingClearBtn = new ActionButton(ActionItems.CLOSE, act -> menu.clear());
        craftingClearBtn.setHalfSize(true);
        widgets.add("craftingClearPattern", craftingClearBtn);
        buttonsByMode.put(EncodingMode.CRAFTING, craftingClearBtn);

        // Add buttons for the processing mode
        ActionButton processingClearBtn = new ActionButton(ActionItems.CLOSE, act -> menu.clear());
        processingClearBtn.setHalfSize(true);
        widgets.add("processingClearPattern", processingClearBtn);
        buttonsByMode.put(EncodingMode.PROCESSING, processingClearBtn);

        this.craftingSubstitutionsBtn = createCraftingSubstitutionButton();
        this.craftingFluidSubstitutionsBtn = createCraftingFluidSubstitutionButton();

        this.processingCycleOutputBtn = new ActionButton(
                ActionItems.CYCLE_PROCESSING_OUTPUT,
                act -> getMenu().cycleProcessingOutput());
        this.processingCycleOutputBtn.setHalfSize(true);
        widgets.add("processingCycleOutput", this.processingCycleOutputBtn);

        this.processingScrollbar = widgets.addScrollBar("processingPatternModeScrollbar", Scrollbar.SMALL);
        // The scrollbar ranges from 0 to the number of rows not visible
        this.processingScrollbar.setRange(0, menu.getProcessingInputSlots().length / 3 - 3, 3);
        this.processingScrollbar.setCaptureMouseWheel(false);

        ActionButton encodeBtn = new ActionButton(ActionItems.ENCODE, act -> menu.encode());
        widgets.add("encodePattern", encodeBtn);

        this.tabCraftButton = new TabButton(
                new ItemStack(Blocks.CRAFTING_TABLE), GuiText.CraftingPattern.text(), this.itemRenderer,
                btn -> getMenu().setMode(EncodingMode.PROCESSING));
        widgets.add("craftingPatternMode", this.tabCraftButton);

        this.tabProcessButton = new TabButton(
                new ItemStack(Blocks.FURNACE), GuiText.ProcessingPattern.text(), this.itemRenderer,
                btn -> getMenu().setMode(EncodingMode.CRAFTING));
        widgets.add("processingPatternMode", this.tabProcessButton);
    }

    private ToggleButton createCraftingSubstitutionButton() {
        var button = new ToggleButton(
                Icon.SUBSTITUTION_ENABLED,
                Icon.SUBSTITUTION_DISABLED,
                getMenu()::setSubstitute);
        button.setHalfSize(true);
        button.setTooltipOn(List.of(
                ButtonToolTips.SubstitutionsOn.text(),
                ButtonToolTips.SubstitutionsDescEnabled.text()));
        button.setTooltipOff(List.of(
                ButtonToolTips.SubstitutionsOff.text(),
                ButtonToolTips.SubstitutionsDescDisabled.text()));
        widgets.add("craftingSubstitutions", button);
        buttonsByMode.put(EncodingMode.CRAFTING, button);
        return button;
    }

    private ToggleButton createCraftingFluidSubstitutionButton() {
        var button = new ToggleButton(
                Icon.FLUID_SUBSTITUTION_ENABLED,
                Icon.FLUID_SUBSTITUTION_DISABLED,
                getMenu()::setSubstituteFluids);
        button.setHalfSize(true);
        button.setTooltipOn(List.of(
                ButtonToolTips.FluidSubstitutions.text(),
                ButtonToolTips.FluidSubstitutionsDescEnabled.text()));
        button.setTooltipOff(List.of(
                ButtonToolTips.FluidSubstitutions.text(),
                ButtonToolTips.FluidSubstitutionsDescDisabled.text()));
        widgets.add("craftingFluidSubstitutions", button);
        buttonsByMode.put(EncodingMode.CRAFTING, button);
        return button;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        for (var entry : buttonsByMode.entries()) {
            entry.getValue().setVisibility(menu.getMode() == entry.getKey());
        }

        var mode = this.menu.getMode();

        processingScrollbar.setVisible(mode == EncodingMode.PROCESSING);

        this.processingCycleOutputBtn.setVisibility(menu.canCycleProcessingOutputs());
        setSlotsHidden(SlotSemantics.CRAFTING_GRID, mode != EncodingMode.CRAFTING);
        setSlotsHidden(SlotSemantics.CRAFTING_RESULT, mode != EncodingMode.CRAFTING);
        setSlotsHidden(SlotSemantics.PROCESSING_INPUTS, mode != EncodingMode.PROCESSING);
        setSlotsHidden(SlotSemantics.PROCESSING_OUTPUTS, mode != EncodingMode.PROCESSING);

        // Update button visibility
        if (mode == EncodingMode.CRAFTING) {
            this.tabCraftButton.visible = true;
            this.tabProcessButton.visible = false;

            this.craftingSubstitutionsBtn.setState(this.menu.substitute);
            this.craftingFluidSubstitutionsBtn.setState(this.menu.substituteFluids);
        } else {
            this.tabCraftButton.visible = false;
            this.tabProcessButton.visible = true;

            // Update the processing slot position/visibility
            repositionSlots(SlotSemantics.PROCESSING_INPUTS);
            repositionSlots(SlotSemantics.PROCESSING_OUTPUTS);

            for (int i = 0; i < menu.getProcessingInputSlots().length; i++) {
                var slot = menu.getProcessingInputSlots()[i];
                var effectiveRow = (i / 3) - processingScrollbar.getCurrentScroll();

                slot.setActive(effectiveRow >= 0 && effectiveRow < 3);
                slot.y -= processingScrollbar.getCurrentScroll() * 18;
            }
            for (int i = 0; i < menu.getProcessingOutputSlots().length; i++) {
                var slot = menu.getProcessingOutputSlots()[i];
                var effectiveRow = i - processingScrollbar.getCurrentScroll();

                slot.setActive(effectiveRow >= 0 && effectiveRow < 3);
                slot.y -= processingScrollbar.getCurrentScroll() * 18;
            }
        }

        // Only show tooltips for the processing output slots, if we're in processing mode
        widgets.setTooltipAreaEnabled("processing-primary-output", mode == EncodingMode.PROCESSING
                && processingScrollbar.getCurrentScroll() == 0);
        widgets.setTooltipAreaEnabled("processing-optional-output1", mode == EncodingMode.PROCESSING
                && processingScrollbar.getCurrentScroll() > 0);
        widgets.setTooltipAreaEnabled("processing-optional-output2", mode == EncodingMode.PROCESSING);
        widgets.setTooltipAreaEnabled("processing-optional-output3", mode == EncodingMode.PROCESSING);
    }

    @Override
    public void drawBG(PoseStack poseStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(poseStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        var mode = menu.getMode();
        getModeBlitter(mode).blit(poseStack, getBlitOffset());
        if (mode == EncodingMode.CRAFTING && menu.substituteFluids
                && craftingFluidSubstitutionsBtn.isMouseOver(mouseX, mouseY)) {
            for (var slotIndex : menu.slotsSupportingFluidSubstitution) {
                drawSlotGreenBG(poseStack, menu.getCraftingGridSlots()[slotIndex]);
            }
        }
    }

    protected Blitter getModeBlitter(EncodingMode mode) {
        Blitter modeBg = mode == EncodingMode.CRAFTING ? CRAFTING_MODE_BG : PROCESSING_MODE_BG;
        modeBg.dest(getGuiLeft() + 9, getGuiTop() + imageHeight - 164);
        return modeBg;
    }

    private void drawSlotGreenBG(PoseStack poseStack, Slot slot) {
        int x = getGuiLeft() + slot.x;
        int y = getGuiTop() + slot.y;
        fill(poseStack, x, y, x + 16, y + 16, 0x7f00FF00);
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        // handler for middle mouse button crafting in survival mode
        if (this.minecraft.options.keyPickItem.matchesMouse(btn)) {
            var slot = this.findSlot(xCoord, yCoord);
            if (menu.canModifyAmountForSlot(slot)) {
                var currentStack = GenericStack.fromItemStack(slot.getItem());
                if (currentStack != null) {
                    var screen = new SetProcessingPatternAmountScreen<>(
                            this,
                            currentStack,
                            newStack -> NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                                    InventoryAction.SET_FILTER, slot.index,
                                    GenericStack.wrapInItemStack(newStack))));
                    switchToScreen(screen);
                    return true;
                }
            }
        }

        return super.mouseClicked(xCoord, yCoord, btn);
    }

    /**
     * When in processing mode, show a hint in the tooltip that middle-click will open the amount entry dialog.
     */
    @Override
    protected void renderTooltip(PoseStack poseStack, int x, int y) {
        if (this.menu.getCarried().isEmpty() && menu.canModifyAmountForSlot(this.hoveredSlot)) {
            var itemTooltip = new ArrayList<>(getTooltipFromItem(this.hoveredSlot.getItem()));
            var unwrapped = GenericStack.fromItemStack(this.hoveredSlot.getItem());
            if (unwrapped != null) {
                itemTooltip.add(Tooltips.getAmountTooltip(ButtonToolTips.Amount, unwrapped));
            }
            itemTooltip.add(Tooltips.getSetAmountTooltip());
            drawTooltip(poseStack, x, y, itemTooltip);
        } else {
            super.renderTooltip(poseStack, x, y);
        }
    }

    @Override
    protected EmptyingAction getEmptyingAction(Slot slot, ItemStack carried) {
        // Since the crafting matrix and output slot are not backed by a config inventory, the default behavior
        // does not work out of the box.
        if (menu.isProcessingPatternSlot(slot)) {
            // See if we should offer the left-/right-click differentiation for setting a different filter
            var emptyingAction = StackInteractions.getEmptyingAction(carried);
            if (emptyingAction != null) {
                return emptyingAction;
            }
        }

        return super.getEmptyingAction(slot, carried);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double wheelDelta) {
        // Forward the mouse-wheel to the processing scrollbar when it is used on the processing pattern overlay,
        // but don't if a slot is hovered and that slot is not a pattern encoding slot
        if (menu.getMode() == EncodingMode.PROCESSING
                && (hoveredSlot == null || menu.isProcessingPatternSlot(hoveredSlot))) {
            var modeBg = getModeBlitter(EncodingMode.PROCESSING);
            if (modeBg.getDestRect().contains((int) x, (int) y)
                    && processingScrollbar.onMouseWheel(new Point((int) x, (int) y), wheelDelta)) {
                return true;
            }
        }
        return super.mouseScrolled(x, y, wheelDelta);
    }

    @Override
    public void onClose() {
        if (AEConfig.instance().isClearGridOnClose()) {
            this.getMenu().clear();
        }
        super.onClose();
    }
}
