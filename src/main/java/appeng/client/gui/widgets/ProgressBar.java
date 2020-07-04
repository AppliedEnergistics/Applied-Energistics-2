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

package appeng.client.gui.widgets;

import appeng.container.interfaces.IProgressProvider;
import appeng.core.localization.GuiText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ProgressBar extends AbstractButtonWidget implements ITooltip {

    private final IProgressProvider source;
    private final Identifier texture;
    private final int fill_u;
    private final int fill_v;
    private final Direction layout;
    private final Text titleName;
    private Text fullMsg;

    public ProgressBar(final IProgressProvider source, final String texture, final int posX, final int posY,
            final int u, final int y, final int width, final int height, final Direction dir) {
        this(source, texture, posX, posY, u, y, width, height, dir, null);
    }

    public ProgressBar(final IProgressProvider source, final String texture, final int posX, final int posY,
            final int u, final int y, final int width, final int height, final Direction dir, final Text title) {
        super(posX, posY, width, height, LiteralText.EMPTY);
        this.source = source;
        this.texture = new Identifier("appliedenergistics2", "textures/" + texture);
        this.fill_u = u;
        this.fill_v = y;
        this.layout = dir;
        this.titleName = title;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.visible) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(this.texture);
            final int max = this.source.getMaxProgress();
            final int current = this.source.getCurrentProgress();

            if (this.layout == Direction.VERTICAL) {
                final int diff = this.height - (max > 0 ? (this.height * current) / max : 0);
                drawTexture(matrices, this.x, this.y + diff, this.fill_u, this.fill_v + diff, this.width,
                        this.height - diff);
            } else {
                final int diff = this.width - (max > 0 ? (this.width * current) / max : 0);
                drawTexture(matrices, this.x, this.y, this.fill_u + diff, this.fill_v, this.width - diff,
                        this.height);
            }
        }
    }

    public void setFullMsg(final Text msg) {
        this.fullMsg = msg;
    }

    @Override
    public Text getMessage() {
        if (this.fullMsg != null) {
            return this.fullMsg;
        }

        Text text = this.titleName != null ? this.titleName : LiteralText.EMPTY;
        return text.copy().append("\n" + this.source.getCurrentProgress() + " ")
                .append(GuiText.Of.textComponent())
                .append(" " + this.source.getMaxProgress());
    }

    @Override
    public int xPos() {
        return this.x - 2;
    }

    @Override
    public int yPos() {
        return this.y - 2;
    }

    @Override
    public int getWidth() {
        return this.width + 4;
    }

    @Override
    public int getHeight() {
        return this.height + 4;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public enum Direction {
        HORIZONTAL, VERTICAL
    }
}
