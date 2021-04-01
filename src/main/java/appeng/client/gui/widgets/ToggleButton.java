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

import java.util.regex.Pattern;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ToggleButton extends Button implements ITooltip {
    public static final ResourceLocation TEXTURE_STATES = new ResourceLocation("appliedenergistics2",
            "textures/guis/states.png");
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);
    private final int iconIdxOn;
    private final int iconIdxOff;

    private final String displayName;
    private final String displayHint;

    private boolean isActive;

    public ToggleButton(final int x, final int y, final int on, final int off, final String displayName,
            final String displayHint, IPressable onPress) {
        super(x, y, 16, 16, StringTextComponent.EMPTY, onPress);
        this.iconIdxOn = on;
        this.iconIdxOff = off;
        this.displayName = displayName;
        this.displayHint = displayHint;
    }

    public void setState(final boolean isOn) {
        this.isActive = isOn;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partial) {
        if (this.visible) {
            final int iconIndex = this.getIconIndex();

            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            Minecraft.getInstance().textureManager.bindTexture(TEXTURE_STATES);

            final int uv_y = iconIndex / 16;
            final int uv_x = iconIndex - uv_y * 16;

            blit(matrixStack, this.x, this.y, 256 - 16, 256 - 16, 16, 16);
            blit(matrixStack, this.x, this.y, uv_x * 16, uv_y * 16, 16, 16);
        }
    }

    private int getIconIndex() {
        return this.isActive ? this.iconIdxOn : this.iconIdxOff;
    }

    @Override
    public ITextComponent getTooltipMessage() {
        if (this.displayName != null) {
            String name = I18n.format(this.displayName);
            String value = I18n.format(this.displayHint);

            if (name == null || name.isEmpty()) {
                name = this.displayName;
            }
            if (value == null || value.isEmpty()) {
                value = this.displayHint;
            }

            value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
            final StringBuilder sb = new StringBuilder(value);

            int i = sb.lastIndexOf("\n");
            if (i <= 0) {
                i = 0;
            }
            while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
                sb.replace(i, i + 1, "\n");
            }

            return new StringTextComponent(name + '\n' + sb);
        }
        return StringTextComponent.EMPTY;
    }

    @Override
    public int getTooltipAreaX() {
        return this.x;
    }

    @Override
    public int getTooltipAreaY() {
        return this.y;
    }

    @Override
    public int getTooltipAreaWidth() {
        return 16;
    }

    @Override
    public int getTooltipAreaHeight() {
        return 16;
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return this.visible;
    }
}
