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

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ToggleButton extends ButtonWidget implements ITooltip {
    public static final Identifier TEXTURE_STATES = new Identifier("appliedenergistics2", "textures/guis/states.png");
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);
    private final int iconIdxOn;
    private final int iconIdxOff;

    private final Text displayName;
    private final Text displayHint;

    private boolean isActive;

    public ToggleButton(final int x, final int y, final int on, final int off, final Text displayName,
            final Text displayHint, PressAction onPress) {
        super(x, y, 16, 16, LiteralText.EMPTY, onPress);
        this.iconIdxOn = on;
        this.iconIdxOff = off;
        this.displayName = displayName;
        this.displayHint = displayHint;
    }

    public void setState(final boolean isOn) {
        this.isActive = isOn;
    }

    @Override
    public void renderButton(MatrixStack matrices, final int mouseX, final int mouseY, final float partial) {
        if (this.visible) {
            final int iconIndex = this.getIconIndex();

            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE_STATES);

            final int uv_y = iconIndex / 16;
            final int uv_x = iconIndex - uv_y * 16;

            drawTexture(matrices, this.x, this.y, 256 - 16, 256 - 16, 16, 16);
            drawTexture(matrices, this.x, this.y, uv_x * 16, uv_y * 16, 16, 16);
        }
    }

    private int getIconIndex() {
        return this.isActive ? this.iconIdxOn : this.iconIdxOff;
    }

    @Override
    public Text getMessage() {
        if (this.displayName != null) {
            String name = this.displayName.getString();
            String value = this.displayHint.getString();

            value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
            final StringBuilder sb = new StringBuilder(value);

            int i = sb.lastIndexOf("\n");
            if (i <= 0) {
                i = 0;
            }
            while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
                sb.replace(i, i + 1, "\n");
            }

            return new LiteralText(name + '\n' + sb);
        }
        return LiteralText.EMPTY;
    }

    @Override
    public int xPos() {
        return this.x;
    }

    @Override
    public int yPos() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }
}
