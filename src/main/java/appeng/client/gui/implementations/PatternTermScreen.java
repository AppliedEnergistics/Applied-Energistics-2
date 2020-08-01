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

package appeng.client.gui.implementations;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import appeng.api.config.ActionItems;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.PatternTermContainer;
import appeng.container.slot.AppEngSlot;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.mixins.SlotMixin;

public class PatternTermScreen extends MEMonitorableScreen<PatternTermContainer> {

    private static final String BACKGROUND_CRAFTING_MODE = "guis/pattern.png";
    private static final String BACKGROUND_PROCESSING_MODE = "guis/pattern2.png";

    private static final String SUBSITUTION_DISABLE = "0";
    private static final String SUBSITUTION_ENABLE = "1";

    private static final String CRAFTMODE_CRFTING = "1";
    private static final String CRAFTMODE_PROCESSING = "0";

    private TabButton tabCraftButton;
    private TabButton tabProcessButton;
    private ActionButton substitutionsEnabledBtn;
    private ActionButton substitutionsDisabledBtn;

    public PatternTermScreen(PatternTermContainer container, PlayerInventory playerInventory, Text title) {
        super(container, playerInventory, title);
        this.setReservedSpace(81);
    }

    @Override
    public void init() {
        super.init();

        this.tabCraftButton = new TabButton(this.x + 173, this.y + this.backgroundHeight - 177,
                new ItemStack(Blocks.CRAFTING_TABLE), GuiText.CraftingPattern.text(), this.itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_PROCESSING));
        this.addButton(this.tabCraftButton);

        this.tabProcessButton = new TabButton(this.x + 173, this.y + this.backgroundHeight - 177,
                new ItemStack(Blocks.FURNACE), GuiText.ProcessingPattern.text(), this.itemRenderer,
                btn -> toggleCraftMode(CRAFTMODE_CRFTING));
        this.addButton(this.tabProcessButton);

        this.substitutionsEnabledBtn = new ActionButton(this.x + 84, this.y + this.backgroundHeight - 163,
                ActionItems.ENABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSITUTION_DISABLE));
        this.substitutionsEnabledBtn.setHalfSize(true);
        this.addButton(this.substitutionsEnabledBtn);

        this.substitutionsDisabledBtn = new ActionButton(this.x + 84, this.y + this.backgroundHeight - 163,
                ActionItems.DISABLE_SUBSTITUTION, act -> toggleSubstitutions(SUBSITUTION_ENABLE));
        this.substitutionsDisabledBtn.setHalfSize(true);
        this.addButton(this.substitutionsDisabledBtn);

        ActionButton clearBtn = new ActionButton(this.x + 74, this.y + this.backgroundHeight - 163, ActionItems.CLOSE,
                act -> clear());
        clearBtn.setHalfSize(true);
        this.addButton(clearBtn);

        ActionButton encodeBtn = new ActionButton(this.x + 147, this.y + this.backgroundHeight - 142,
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
    public void drawFG(MatrixStack matrices, final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        if (this.handler.isCraftingMode()) {
            this.tabCraftButton.visible = true;
            this.tabProcessButton.visible = false;

            if (this.handler.substitute) {
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

        super.drawFG(matrices, offsetX, offsetY, mouseX, mouseY);
        this.textRenderer.method_30883(matrices, GuiText.PatternTerminal.text(), 8,
                this.backgroundHeight - 96 + 2 - this.getReservedSpace(), 4210752);
    }

    @Override
    protected String getBackground() {
        if (this.handler.isCraftingMode()) {
            return BACKGROUND_CRAFTING_MODE;
        }

        return BACKGROUND_PROCESSING_MODE;
    }

    @Override
    protected void repositionSlot(final AppEngSlot s) {
        final int offsetPlayerSide = s.isPlayerSide() ? 5 : 3;

        ((SlotMixin) s).setY(s.getY() + this.backgroundHeight - 78 - offsetPlayerSide);
    }

}
