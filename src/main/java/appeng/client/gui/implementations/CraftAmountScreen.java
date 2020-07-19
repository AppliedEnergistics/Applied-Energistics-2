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

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.widgets.NumberBox;
import appeng.container.implementations.CraftAmountContainer;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.CraftRequestPacket;

public class CraftAmountScreen extends AEBaseScreen<CraftAmountContainer> {
    private final AESubScreen subGui;

    private NumberBox amountToCraft;

    private Button next;

    public CraftAmountScreen(CraftAmountContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.subGui = new AESubScreen(this, container.getTarget());
    }

    @Override
    public void init() {
        super.init();

        final int a = AEConfig.instance().craftItemsByStackAmounts(0);
        final int b = AEConfig.instance().craftItemsByStackAmounts(1);
        final int c = AEConfig.instance().craftItemsByStackAmounts(2);
        final int d = AEConfig.instance().craftItemsByStackAmounts(3);

        this.addButton(new Button(this.guiLeft + 20, this.guiTop + 26, 22, 20, "+" + a, btn -> addQty(a)));
        this.addButton(new Button(this.guiLeft + 48, this.guiTop + 26, 28, 20, "+" + b, btn -> addQty(b)));
        this.addButton(new Button(this.guiLeft + 82, this.guiTop + 26, 32, 20, "+" + c, btn -> addQty(c)));
        this.addButton(new Button(this.guiLeft + 120, this.guiTop + 26, 38, 20, "+" + d, btn -> addQty(d)));

        this.addButton(new Button(this.guiLeft + 20, this.guiTop + 75, 22, 20, "-" + a, btn -> addQty(-a)));
        this.addButton(new Button(this.guiLeft + 48, this.guiTop + 75, 28, 20, "-" + b, btn -> addQty(-b)));
        this.addButton(new Button(this.guiLeft + 82, this.guiTop + 75, 32, 20, "-" + c, btn -> addQty(-c)));
        this.addButton(new Button(this.guiLeft + 120, this.guiTop + 75, 38, 20, "-" + d, btn -> addQty(-d)));

        this.next = this.addButton(
                new Button(this.guiLeft + 128, this.guiTop + 51, 38, 20, GuiText.Next.getLocal(), this::confirm));

        subGui.addBackButton(this::addButton, 154, 0);

        this.amountToCraft = new NumberBox(this.font, this.guiLeft + 62, this.guiTop + 57, 59, this.font.FONT_HEIGHT,
                Integer.class, value -> {});
        this.amountToCraft.setEnableBackgroundDrawing(false);
        this.amountToCraft.setMaxStringLength(16);
        this.amountToCraft.setTextColor(0xFFFFFF);
        this.amountToCraft.setVisible(true);
        this.amountToCraft.setFocused2(true);
        this.amountToCraft.setText("1");
    }

    private void confirm(Button button) {
        NetworkHandler.instance()
                .sendToServer(new CraftRequestPacket((int) this.amountToCraft.getValue(), hasShiftDown()));
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.font.drawString(GuiText.SelectAmount.getLocal(), 8, 6, 4210752);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY, float partialTicks) {
        this.next.setMessage(hasShiftDown() ? GuiText.Start.getLocal() : GuiText.Next.getLocal());

        this.bindTexture("guis/craft_amt.png");
        GuiUtils.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize, getBlitOffset());

        try {
            Long.parseLong(this.amountToCraft.getText());
            this.next.active = !this.amountToCraft.getText().isEmpty();
        } catch (final NumberFormatException e) {
            this.next.active = false;
        }

        this.amountToCraft.render(offsetX, offsetY, partialTicks);
    }

    @Override
    public boolean charTyped(char ch, int p_charTyped_2_) {
        // Forward entered text to the craft amount text-field
        return this.amountToCraft.charTyped(ch, p_charTyped_2_);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (!this.checkHotbarKeys(InputMappings.getInputByCode(keyCode, scanCode))) {
            if (keyCode == 28) {
                this.next.onPress();
            }
            if ((keyCode == 211 || keyCode == 205 || keyCode == 203 || keyCode == 14)
                    && this.amountToCraft.keyPressed(keyCode, scanCode, p_keyPressed_3_)) {
                try {
                    String out = this.amountToCraft.getText();

                    boolean fixed = false;
                    while (out.startsWith("0") && out.length() > 1) {
                        out = out.substring(1);
                        fixed = true;
                    }

                    if (fixed) {
                        this.amountToCraft.setText(out);
                    }

                    if (out.isEmpty()) {
                        out = "0";
                    }

                    final long result = Long.parseLong(out);
                    if (result < 0) {
                        this.amountToCraft.setText("1");
                    }
                } catch (final NumberFormatException e) {
                    // :P
                }
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    private void addQty(final int i) {
        try {
            String out = this.amountToCraft.getText();

            boolean fixed = false;
            while (out.startsWith("0") && out.length() > 1) {
                out = out.substring(1);
                fixed = true;
            }

            if (fixed) {
                this.amountToCraft.setText(out);
            }

            if (out.isEmpty()) {
                out = "0";
            }

            long result = Integer.parseInt(out);

            if (result == 1 && i > 1) {
                result = 0;
            }

            result += i;
            if (result < 1) {
                result = 1;
            }

            out = Long.toString(result);
            Integer.parseInt(out);
            this.amountToCraft.setText(out);
        } catch (final NumberFormatException e) {
            // :P
        }
    }

    protected String getBackground() {
        return "guis/craftAmt.png";
    }
}
