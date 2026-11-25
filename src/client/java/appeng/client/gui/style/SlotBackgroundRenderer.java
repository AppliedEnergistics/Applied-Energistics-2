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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Collects the backgrounds of slots to draw them in one go. Slot backgrounds are centered on the 16x16 item area of
 * each slot. Contiguous groups of slot backgrounds (even across different slot semantics) get a common border.
 */
public final class SlotBackgroundRenderer {
    private final List<Entry> entries = new ArrayList<>();

    /**
     * @param x       The x position of the slot's item area.
     * @param y       The y position of the slot's item area.
     * @param opacity The opacity of the background. The border is always opaque.
     */
    public void add(SpriteRenderer renderer, SlotBackground background, int x, int y, float opacity) {
        if (!background.isVisible()) {
            return;
        }

        var sprite = background.getSprite();
        var info = renderer.getSprite(sprite);
        var bounds = new Rect2i(x + 8 - info.width() / 2, y + 8 - info.height() / 2, info.width(),
                info.height());
        entries.add(new Entry(sprite, bounds, ARGB.white(opacity), background.hasBorder()));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void render(SpriteRenderer renderer, int xOffset, int yOffset) {
        for (var entry : entries) {
            var bounds = entry.bounds;
            renderer.blitSprite(entry.sprite, xOffset + bounds.getX(), yOffset + bounds.getY(), bounds.getWidth(),
                    bounds.getHeight(), entry.color);
        }

        for (var group : getBorderGroups()) {
            renderer.blitSprite(SlotBackground.BORDER_SPRITE, xOffset + group.getX(), yOffset + group.getY(),
                    group.getWidth(), group.getHeight(), -1);
        }
    }

    /**
     * Computes the bounding boxes of groups of slot backgrounds that touch or overlap each other.
     */
    private List<Rect2i> getBorderGroups() {
        var groups = new ArrayList<Rect2i>();
        for (var entry : entries) {
            if (!entry.border) {
                continue;
            }

            var group = entry.bounds;
            // Merge with all groups that touch this one. Since merging grows the group, it may now touch groups
            // that were checked before, so keep going until nothing changes anymore.
            boolean merged;
            do {
                merged = false;
                for (var it = groups.iterator(); it.hasNext();) {
                    var other = it.next();
                    if (touches(group, other)) {
                        group = union(group, other);
                        it.remove();
                        merged = true;
                    }
                }
            } while (merged);
            groups.add(group);
        }
        return groups;
    }

    private static boolean touches(Rect2i a, Rect2i b) {
        return a.getX() <= b.getX() + b.getWidth()
                && b.getX() <= a.getX() + a.getWidth()
                && a.getY() <= b.getY() + b.getHeight()
                && b.getY() <= a.getY() + a.getHeight()
                // Rectangles that only touch at their corners are not considered to be touching
                && !touchesOnlyAtCorner(a, b);
    }

    private static boolean touchesOnlyAtCorner(Rect2i a, Rect2i b) {
        boolean touchX = a.getX() == b.getX() + b.getWidth() || b.getX() == a.getX() + a.getWidth();
        boolean touchY = a.getY() == b.getY() + b.getHeight() || b.getY() == a.getY() + a.getHeight();
        return touchX && touchY;
    }

    private static Rect2i union(Rect2i a, Rect2i b) {
        var x = Math.min(a.getX(), b.getX());
        var y = Math.min(a.getY(), b.getY());
        var right = Math.max(a.getX() + a.getWidth(), b.getX() + b.getWidth());
        var bottom = Math.max(a.getY() + a.getHeight(), b.getY() + b.getHeight());
        return new Rect2i(x, y, right - x, bottom - y);
    }

    private record Entry(Identifier sprite, Rect2i bounds, int color, boolean border) {
    }
}
