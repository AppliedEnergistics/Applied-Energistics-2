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

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.ActionItems;
import appeng.client.gui.Blitter;
import appeng.client.gui.me.common.TerminalStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.container.me.items.PatternTermContainer;
import appeng.container.slot.FakeCraftingMatrixSlot;
import appeng.container.slot.OptionalFakeSlot;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class PatternTermScreen extends ItemTerminalScreen<PatternTermContainer> {

    private static final int PROCESSING_OUTPUT_X = 110;
    private static final int CRAFTING_OUTPUT_X = 110;

    private static final String MODES_TEXTURE = "guis/pattern_modes.png";

    private static final Blitter CRAFTING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 0, 126, 68);

    private static final Blitter PROCESSING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 70, 126, 68);

    private static final String SUBSTITUTION_DISABLE = "0";
    private static final String SUBSTITUTION_ENABLE = "1";

    private static final String CRAFTMODE_CRAFTING = "1";
    private static final String CRAFTMODE_PROCESSING = "0";

    private TabButton tabCraftButton;
    private TabButton tabProcessButton;
    private ActionButton substitutionsEnabledBtn;
    private ActionButton substitutionsDisabledBtn;

    public PatternTermScreen(TerminalStyle style, PatternTermContainer container, PlayerInventory playerInventory,
            ITextComponent title) {
        super(style, container, playerInventory, title);

        // Position pattern-related slots
        anchorSlotToBottom(container.getBlankPatternSlot(), 146, 162);
        anchorSlotToBottom(container.getEncodedPatternSlot(), 146, 119);

        // Position the crafting grid slots
        anchorSlotToBottom(container.getCraftOutputSlot(), CRAFTING_OUTPUT_X, 139);
        FakeCraftingMatrixSlot[] craftingSlots = container.getCraftingGridSlots();
        for (int i = 0; i < craftingSlots.length; i++) {
            int row = i / 3;
            int col = i % 3;

            anchorSlotToBottom(craftingSlots[i], 17 + col * 18, 157 - row * 18);
        }

        // Position the processing pattern output
        OptionalFakeSlot[] processingOutputSlots = container.getProcessingOutputSlots();
        for (int i = 0; i < processingOutputSlots.length; i++) {
            anchorSlotToBottom(processingOutputSlots[i], PROCESSING_OUTPUT_X, 157 - i * 18);
        }
    }

    @Override
    public void init() {
        super.init();

        this.tabCraftButton = new TabButton(this.guiLeft + 173, this.guiTop + this.ySize - 177,
                new ItemStack(Blocks.CRAFTING_TABLE), GuiText.CraftingPattern.text(), this.itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_PROCESSING));
        this.addButton(this.tabCraftButton);

        this.tabProcessButton = new TabButton(this.guiLeft + 173, this.guiTop + this.ySize - 177,
                new ItemStack(Blocks.FURNACE), GuiText.ProcessingPattern.text(), this.itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_CRAFTING));
        this.addButton(this.tabProcessButton);

        this.substitutionsEnabledBtn = new ActionButton(this.guiLeft + 84, this.guiTop + this.ySize - 163,
                ActionItems.ENABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSTITUTION_DISABLE));
        this.substitutionsEnabledBtn.setHalfSize(true);
        this.addButton(this.substitutionsEnabledBtn);

        this.substitutionsDisabledBtn = new ActionButton(this.guiLeft + 84, this.guiTop + this.ySize - 163,
                ActionItems.DISABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSTITUTION_ENABLE));
        this.substitutionsDisabledBtn.setHalfSize(true);
        this.addButton(this.substitutionsDisabledBtn);

        ActionButton clearBtn = new ActionButton(this.guiLeft + 74, this.guiTop + this.ySize - 163, ActionItems.CLOSE,
                act -> clear());
        clearBtn.setHalfSize(true);
        this.addButton(clearBtn);

        ActionButton encodeBtn = new ActionButton(this.guiLeft + 147, this.guiTop + this.ySize - 142,
                ActionItems.ENCODE, act -> encode());
        this.addButton(encodeBtn);
    }

    private void toggleCraftMode(String mode) {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("PatternTerminal.CraftMode", mode));
    }

    private void toggleSubstitutions(String mode) {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("PatternTerminal.Substitute", mode));
    }

    private void encode() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("PatternTerminal.Encode", "1"));
    }

    private void clear() {
        NetworkHandler.instance().sendToServer(new ConfigValuePacket("PatternTerminal.Clear", "1"));
    }

    @Override
    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        Blitter modeBg;
        if (this.container.isCraftingMode()) {
            modeBg = CRAFTING_MODE_BG;

            // Move the processing output slots off-screen and reposition the crafting slot onscreen
            container.getCraftOutputSlot().xPos = CRAFTING_OUTPUT_X;
            for (int y = 0; y < 3; y++) {
                container.getProcessingOutputSlots()[y].xPos = -9000;
            }
        } else {
            modeBg = PROCESSING_MODE_BG;

            // Move the crafting slot off-screen and reposition the processing output slot onscreen
            container.getCraftOutputSlot().xPos = -9000;
            for (int y = 0; y < 3; y++) {
                container.getProcessingOutputSlots()[y].xPos = PROCESSING_OUTPUT_X;
            }
        }

        modeBg.dest(guiLeft + 9, guiTop + ySize - 164).blit(matrixStack, getBlitOffset());
    }

    @Override
    public void drawFG(MatrixStack matrixStack, final int offsetX, final int offsetY, final int mouseX,
            final int mouseY) {
        updateButtonVisibility();
        super.drawFG(matrixStack, offsetX, offsetY, mouseX, mouseY);
        this.font.drawString(matrixStack, GuiText.PatternTerminal.getLocal(), 8,
                this.ySize - 96 + 2 - 81, COLOR_DARK_GRAY);
    }

    private void updateButtonVisibility() {
        // Update button visibility
        if (this.container.isCraftingMode()) {
            this.tabCraftButton.visible = true;
            this.tabProcessButton.visible = false;

            if (this.container.substitute) {
                this.substitutionsEnabledBtn.visible = true;
                this.substitutionsDisabledBtn.visible = false;
            } else {
                this.substitutionsEnabledBtn.visible = false;
                this.substitutionsDisabledBtn.visible = true;
            }
        } else {
            this.tabCraftButton.visible = false;
            this.tabProcessButton.visible = true;
            this.substitutionsEnabledBtn.visible = false;
            this.substitutionsDisabledBtn.visible = false;
        }
    }

}
