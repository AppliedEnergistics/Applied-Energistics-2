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
import appeng.menu.SlotSemantic;
import appeng.menu.me.items.PatternTermMenu;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;

public class PatternTermScreen extends ItemTerminalScreen<PatternTermMenu> {

    private static final String MODES_TEXTURE = "guis/pattern_modes.png";

    private static final Blitter CRAFTING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 0, 126, 68);

    private static final Blitter PROCESSING_MODE_BG = Blitter.texture(MODES_TEXTURE).src(0, 70, 126, 68);

    private static final String SUBSTITUTION_DISABLE = "0";
    private static final String SUBSTITUTION_ENABLE = "1";

    private static final String CRAFTMODE_CRAFTING = "1";
    private static final String CRAFTMODE_PROCESSING = "0";

    private final TabButton tabCraftButton;
    private final TabButton tabProcessButton;
    private final ActionButton substitutionsEnabledBtn;
    private final ActionButton substitutionsDisabledBtn;

    public PatternTermScreen(PatternTermMenu menu, Inventory playerInventory,
                             Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.tabCraftButton = new TabButton(
                new ItemStack(Blocks.CRAFTING_TABLE), GuiText.CraftingPattern.text(), this.itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_PROCESSING));
        widgets.add("craftingPatternMode", this.tabCraftButton);

        this.tabProcessButton = new TabButton(
                new ItemStack(Blocks.FURNACE), GuiText.ProcessingPattern.text(), this.itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_CRAFTING));
        widgets.add("processingPatternMode", this.tabProcessButton);

        this.substitutionsEnabledBtn = new ActionButton(
                ActionItems.ENABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSTITUTION_DISABLE));
        this.substitutionsEnabledBtn.setHalfSize(true);
        widgets.add("substitutionsEnabled", this.substitutionsEnabledBtn);

        this.substitutionsDisabledBtn = new ActionButton(
                ActionItems.DISABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSTITUTION_ENABLE));
        this.substitutionsDisabledBtn.setHalfSize(true);
        widgets.add("substitutionsDisabled", this.substitutionsDisabledBtn);

        ActionButton clearBtn = new ActionButton(ActionItems.CLOSE, act -> clear());
        clearBtn.setHalfSize(true);
        widgets.add("clearPattern", clearBtn);

        ActionButton encodeBtn = new ActionButton(ActionItems.ENCODE, act -> encode());
        widgets.add("encodePattern", encodeBtn);
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
        } else {
            this.tabCraftButton.visible = false;
            this.tabProcessButton.visible = true;
            this.substitutionsEnabledBtn.visible = false;
            this.substitutionsDisabledBtn.visible = false;
        }

        setSlotsHidden(SlotSemantic.CRAFTING_RESULT, !this.menu.isCraftingMode());
        setSlotsHidden(SlotSemantic.PROCESSING_RESULT, this.menu.isCraftingMode());
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
    public void drawBG(PoseStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(matrixStack, offsetX, offsetY, mouseX, mouseY, partialTicks);

        Blitter modeBg = this.menu.isCraftingMode() ? CRAFTING_MODE_BG : PROCESSING_MODE_BG;
        modeBg.dest(leftPos + 9, topPos + imageHeight - 164).blit(matrixStack, getBlitOffset());
    }

}
