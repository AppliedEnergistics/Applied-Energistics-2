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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import appeng.api.config.ActionItems;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantic;
import appeng.menu.me.items.PatternTermMenu;

public class PatternTermScreen extends ItemTerminalScreen<PatternTermMenu> {

    private static final String MODES_TEXTURE = "guis/pattern_modes.png";

    private static final Blitter CRAFTING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 0, 126, 68);

    private static final Blitter PROCESSING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 70, 126, 68);

    private final TabButton tabCraftButton;
    private final TabButton tabProcessButton;
    private final ActionButton substitutionsEnabledBtn;
    private final ActionButton substitutionsDisabledBtn;
    private final ActionButton fluidSubstitutionsEnabledBtn;
    private final ActionButton fluidSubstitutionsDisabledBtn;
    private final ActionButton convertItemsToFluidsBtn;

    public PatternTermScreen(PatternTermMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.tabCraftButton = new TabButton(
                new ItemStack(Blocks.CRAFTING_TABLE), GuiText.CraftingPattern.text(), this.itemRenderer,
                btn -> getMenu().setCraftingMode(false));
        widgets.add("craftingPatternMode", this.tabCraftButton);

        this.tabProcessButton = new TabButton(
                new ItemStack(Blocks.FURNACE), GuiText.ProcessingPattern.text(), this.itemRenderer,
                btn -> getMenu().setCraftingMode(true));
        widgets.add("processingPatternMode", this.tabProcessButton);

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

        ActionButton clearBtn = new ActionButton(ActionItems.CLOSE, act -> menu.clear());
        clearBtn.setHalfSize(true);
        widgets.add("clearPattern", clearBtn);

        ActionButton encodeBtn = new ActionButton(ActionItems.ENCODE, act -> menu.encode());
        widgets.add("encodePattern", encodeBtn);

        convertItemsToFluidsBtn = new ActionButton(ActionItems.FIND_CONTAINED_FLUID,
                act -> menu.convertItemsToFluids());
        convertItemsToFluidsBtn.setHalfSize(true);
        widgets.add("convertItemsToFluids", convertItemsToFluidsBtn);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        // Update button visibility
        if (this.menu.isCraftingMode()) {
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

        setSlotsHidden(SlotSemantic.CRAFTING_RESULT, !this.menu.isCraftingMode());
        setSlotsHidden(SlotSemantic.PROCESSING_PRIMARY_RESULT, this.menu.isCraftingMode());
        setSlotsHidden(SlotSemantic.PROCESSING_FIRST_OPTIONAL_RESULT, this.menu.isCraftingMode());
        setSlotsHidden(SlotSemantic.PROCESSING_SECOND_OPTIONAL_RESULT, this.menu.isCraftingMode());

        // Only show tooltips for the processing output slots, if we're in processing mode
        widgets.setTooltipAreaEnabled("processing-primary-output", !this.menu.isCraftingMode());
        widgets.setTooltipAreaEnabled("processing-optional-output1", !this.menu.isCraftingMode());
        widgets.setTooltipAreaEnabled("processing-optional-output2", !this.menu.isCraftingMode());

        // If the menu allows converting items to fluids, show the button
        this.convertItemsToFluidsBtn.visible = this.menu.canConvertItemsToFluids();
    }

    @Override
    public void drawBG(PoseStack poseStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(poseStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        Blitter modeBg = this.menu.isCraftingMode() ? CRAFTING_MODE_BG : PROCESSING_MODE_BG;
        modeBg.dest(leftPos + 9, topPos + imageHeight - 164).blit(poseStack, getBlitOffset());
    }

}
