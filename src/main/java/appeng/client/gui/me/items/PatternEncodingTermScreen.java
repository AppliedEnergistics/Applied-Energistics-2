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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import appeng.api.config.ActionItems;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.menu.SlotSemantics;
import appeng.menu.me.interaction.EmptyingAction;
import appeng.menu.me.interaction.StackInteractions;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.PatternTermSlot;
import appeng.parts.encoding.EncodingMode;

public class PatternEncodingTermScreen<C extends PatternEncodingTermMenu> extends MEStorageScreen<C> {

    private static final String MODES_TEXTURE = "guis/pattern_modes.png";

    private static final Blitter CRAFTING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 0, 126, 68);

    private static final Blitter PROCESSING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 70, 126, 68);

    private final TabButton tabCraftButton;
    private final TabButton tabProcessButton;
    private final ActionButton substitutionsEnabledBtn;
    private final ActionButton substitutionsDisabledBtn;
    private final ActionButton fluidSubstitutionsEnabledBtn;
    private final ActionButton fluidSubstitutionsDisabledBtn;

    public PatternEncodingTermScreen(C menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        ActionButton clearBtn = new ActionButton(ActionItems.CLOSE, act -> menu.clear());
        clearBtn.setHalfSize(true);
        widgets.add("clearPattern", clearBtn);

        this.substitutionsEnabledBtn = new ActionButton(
                ActionItems.ENABLE_SUBSTITUTION, act -> getMenu().setSubstitute(false));
        this.substitutionsEnabledBtn.setHalfSize(true);
        widgets.add("substitutionsEnabled", this.substitutionsEnabledBtn);

        this.substitutionsDisabledBtn = new ActionButton(
                ActionItems.DISABLE_SUBSTITUTION, act -> getMenu().setSubstitute(true));
        this.substitutionsDisabledBtn.setHalfSize(true);
        widgets.add("substitutionsDisabled", this.substitutionsDisabledBtn);

        this.fluidSubstitutionsEnabledBtn = new ActionButton(
                ActionItems.ENABLE_FLUID_SUBSTITUTION, act -> getMenu().setSubstituteFluids(false));
        this.fluidSubstitutionsEnabledBtn.setHalfSize(true);
        widgets.add("fluidSubstitutionsEnabled", this.fluidSubstitutionsEnabledBtn);

        this.fluidSubstitutionsDisabledBtn = new ActionButton(
                ActionItems.DISABLE_FLUID_SUBSTITUTION, act -> getMenu().setSubstituteFluids(true));
        this.fluidSubstitutionsDisabledBtn.setHalfSize(true);
        widgets.add("fluidSubstitutionsDisabled", this.fluidSubstitutionsDisabledBtn);

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

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        var mode = this.menu.getMode();

        // Update button visibility
        if (mode == EncodingMode.CRAFTING) {
            this.tabCraftButton.visible = true;
            this.tabProcessButton.visible = false;

            if (this.menu.substitute) {
                this.substitutionsEnabledBtn.visible = true;
                this.substitutionsDisabledBtn.visible = false;
            } else {
                this.substitutionsEnabledBtn.visible = false;
                this.substitutionsDisabledBtn.visible = true;
            }

            if (this.menu.substituteFluids) {
                this.fluidSubstitutionsEnabledBtn.visible = true;
                this.fluidSubstitutionsDisabledBtn.visible = false;
            } else {
                this.fluidSubstitutionsEnabledBtn.visible = false;
                this.fluidSubstitutionsDisabledBtn.visible = true;
            }
        } else {
            this.tabCraftButton.visible = false;
            this.tabProcessButton.visible = true;
            this.substitutionsEnabledBtn.visible = false;
            this.substitutionsDisabledBtn.visible = false;
            this.fluidSubstitutionsEnabledBtn.visible = false;
            this.fluidSubstitutionsDisabledBtn.visible = false;
        }

        setSlotsHidden(SlotSemantics.CRAFTING_RESULT, mode != EncodingMode.CRAFTING);
        setSlotsHidden(SlotSemantics.PROCESSING_PRIMARY_RESULT, mode != EncodingMode.PROCESSING);
        setSlotsHidden(SlotSemantics.PROCESSING_FIRST_OPTIONAL_RESULT, mode != EncodingMode.PROCESSING);
        setSlotsHidden(SlotSemantics.PROCESSING_SECOND_OPTIONAL_RESULT, mode != EncodingMode.PROCESSING);

        // Only show tooltips for the processing output slots, if we're in processing mode
        widgets.setTooltipAreaEnabled("processing-primary-output", mode == EncodingMode.PROCESSING);
        widgets.setTooltipAreaEnabled("processing-optional-output1", mode == EncodingMode.PROCESSING);
        widgets.setTooltipAreaEnabled("processing-optional-output2", mode == EncodingMode.PROCESSING);
    }

    @Override
    public void drawBG(PoseStack poseStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(poseStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        var mode = menu.getMode();
        Blitter modeBg = mode == EncodingMode.CRAFTING ? CRAFTING_MODE_BG : PROCESSING_MODE_BG;
        modeBg.dest(leftPos + 9, topPos + imageHeight - 164).blit(poseStack, getBlitOffset());

        if (mode == EncodingMode.CRAFTING && menu.substituteFluids
                && fluidSubstitutionsEnabledBtn.isMouseOver(mouseX, mouseY)) {
            for (var slotIndex : menu.slotsSupportingFluidSubstitution) {
                drawSlotGreenBG(poseStack, menu.getCraftingGridSlots()[slotIndex]);
            }
        }
    }

    private void drawSlotGreenBG(PoseStack poseStack, Slot slot) {
        int x = getGuiLeft() + slot.x;
        int y = getGuiTop() + slot.y;
        fill(poseStack, x, y, x + 16, y + 16, 0x7f00FF00);
    }

    @Override
    protected void slotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (mouseButton == InputConstants.MOUSE_BUTTON_MIDDLE && menu.canModifyAmountForSlot(slot)) {
            menu.showModifyAmountMenu(slotIdx);
            return;
        }

        if (slot instanceof PatternTermSlot) {
            if (!slot.getItem().isEmpty()) {
                var packet = new PatternSlotPacket(menu.getCraftingMatrix(), slot.getItem(), hasShiftDown());
                NetworkHandler.instance().sendToServer(packet);
            }
            return;
        }

        super.slotClicked(slot, slotIdx, mouseButton, clickType);
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

}
