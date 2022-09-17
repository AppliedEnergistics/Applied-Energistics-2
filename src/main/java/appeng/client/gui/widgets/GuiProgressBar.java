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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;


public class GuiProgressBar extends GuiButton implements ITooltip {

    private final IProgressProvider source;
    private final ResourceLocation texture;
    private final int fill_u;
    private final int fill_v;
    private final Direction layout;
    private final String titleName;
    private String fullMsg;

    public GuiProgressBar(final IProgressProvider source, final String texture, final int posX, final int posY, final int u, final int y, final int width, final int height, final Direction dir) {
        this(source, texture, posX, posY, u, y, width, height, dir, null);
    }

    public GuiProgressBar(final IProgressProvider source, final String texture, final int posX, final int posY, final int u, final int y, final int width, final int height, final Direction dir, final String title) {
        super(posX, posY, width, "");
        this.source = source;
        this.x = posX;
        this.y = posY;
        this.texture = new ResourceLocation("appliedenergistics2", "textures/" + texture);
        this.width = width;
        this.height = height;
        this.fill_u = u;
        this.fill_v = y;
        this.layout = dir;
        this.titleName = title;
    }

    @Override
    public void drawButton(final Minecraft par1Minecraft, final int par2, final int par3, final float partial) {
        if (this.visible) {
            par1Minecraft.getTextureManager().bindTexture(this.texture);
            final int max = this.source.getMaxProgress();
            final int current = this.source.getCurrentProgress();

            if (this.layout == Direction.VERTICAL) {
                final int diff = this.height - (max > 0 ? (this.height * current) / max : 0);
                this.drawTexturedModalRect(this.x, this.y + diff, this.fill_u, this.fill_v + diff, this.width, this.height - diff);
            } else {
                final int diff = this.width - (max > 0 ? (this.width * current) / max : 0);
                this.drawTexturedModalRect(this.x, this.y, this.fill_u + diff, this.fill_v, this.width - diff, this.height);
            }

            this.mouseDragged(par1Minecraft, par2, par3);
        }
    }

    public void setFullMsg(final String msg) {
        this.fullMsg = msg;
    }

    @Override
    public String getMessage() {
        if (this.fullMsg != null) {
            return this.fullMsg;
        }

        return (this.titleName != null ? this.titleName : "") + '\n' + this.source.getCurrentProgress() + ' ' + GuiText.Of.getLocal() + ' ' + this.source
                .getMaxProgress();
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
