/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2026, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.gui.style;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;

/**
 * A GUI sprite that is drawn as part of a screen background, on top of the window, but below slots and widgets.
 */
public class SpriteStyle extends Position {
    private Identifier sprite;

    /**
     * Width to draw the sprite with. Defaults to the width of the sprite.
     */
    @Nullable
    private Integer width;

    /**
     * Height to draw the sprite with. Defaults to the height of the sprite.
     */
    @Nullable
    private Integer height;

    public Identifier getSprite() {
        return Objects.requireNonNull(sprite, "sprite");
    }

    public void setSprite(Identifier sprite) {
        this.sprite = sprite;
    }

    @Nullable
    public Integer getWidth() {
        return width;
    }

    public void setWidth(@Nullable Integer width) {
        this.width = width;
    }

    @Nullable
    public Integer getHeight() {
        return height;
    }

    public void setHeight(@Nullable Integer height) {
        this.height = height;
    }

    /**
     * Draws the sprite relative to the given screen bounds.
     */
    public void draw(SpriteRenderer renderer, Rect2i bounds) {
        var pos = resolve(bounds);
        var sprite = getSprite();
        if (width == null || height == null) {
            var info = renderer.getSprite(sprite);
            renderer.blitSprite(sprite, pos.getX(), pos.getY(),
                    width != null ? width : info.width(),
                    height != null ? height : info.height(),
                    -1);
        } else {
            renderer.blitSprite(sprite, pos.getX(), pos.getY(), width, height, -1);
        }
    }
}
