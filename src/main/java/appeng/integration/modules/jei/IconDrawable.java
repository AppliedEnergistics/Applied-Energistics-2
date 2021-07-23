/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.integration.modules.jei;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.drawable.IDrawable;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;

/**
 * Creates {@link IDrawable} from {@link Icon}
 */
final class IconDrawable implements IDrawable {
    private final Blitter blitter;
    private final int x;
    private final int y;

    IconDrawable(Icon icon, int x, int y) {
        this.blitter = icon.getBlitter();
        this.x = x;
        this.y = y;
    }

    @Override
    public int getWidth() {
        return blitter.getSrcWidth();
    }

    @Override
    public int getHeight() {
        return blitter.getSrcHeight();
    }

    @Override
    public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
        blitter.dest(x + xOffset, y + yOffset).blit(matrixStack, 0);
    }
}
