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

package appeng.client.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.Identifier;

import guideme.document.LytRect;

import appeng.client.gui.style.SpriteRenderer;
import appeng.client.gui.util.RectangleMerger;
import appeng.core.AppEng;

/**
 * Draws window backgrounds of arbitrary shape. The shape is defined by a set of rectangles, which may overlap or touch
 * each other. Their union is drawn as one contiguous window, including inner corners where rectangles join.
 * <p>
 * The window is drawn using two nine-slice GUI sprites:
 * <ul>
 * <li>{@link #WINDOW_SPRITE} provides the outer corners, the borders and the fill.</li>
 * <li>{@link #WINDOW_INNER_SPRITE} provides the inner corners. Only its four corners are used.</li>
 * </ul>
 * The border sizes of the inner window sprite have to fit those of the window sprite: The width of its left and right
 * borders must match those of the window sprite, its top border must match the bottom border of the window sprite and
 * vice versa.
 */
public final class PanelBlitter {
    public static final Identifier WINDOW_SPRITE = AppEng.makeId("window");
    public static final Identifier WINDOW_INNER_SPRITE = AppEng.makeId("window_inner");

    private static final int TOP = 0;
    private static final int RIGHT = 1;
    private static final int BOTTOM = 2;
    private static final int LEFT = 3;

    // Slice indices of a nine-slice sprite, counted left-to-right, then top-to-bottom.
    private static final int TOP_LEFT = 0;
    private static final int TOP_EDGE = 1;
    private static final int TOP_RIGHT = 2;
    private static final int LEFT_EDGE = 3;
    private static final int CENTER = 4;
    private static final int RIGHT_EDGE = 5;
    private static final int BOTTOM_LEFT = 6;
    private static final int BOTTOM_EDGE = 7;
    private static final int BOTTOM_RIGHT = 8;

    private static final Slice OUTER_TOP_LEFT = new Slice(false, TOP_LEFT);
    private static final Slice OUTER_TOP_RIGHT = new Slice(false, TOP_RIGHT);
    private static final Slice OUTER_BOTTOM_RIGHT = new Slice(false, BOTTOM_RIGHT);
    private static final Slice OUTER_BOTTOM_LEFT = new Slice(false, BOTTOM_LEFT);
    private static final Slice TOP_BORDER = new Slice(false, TOP_EDGE);
    private static final Slice RIGHT_BORDER = new Slice(false, RIGHT_EDGE);
    private static final Slice BOTTOM_BORDER = new Slice(false, BOTTOM_EDGE);
    private static final Slice LEFT_BORDER = new Slice(false, LEFT_EDGE);
    private static final Slice FILL = new Slice(false, CENTER);
    private static final Slice INNER_TOP_LEFT = new Slice(true, TOP_LEFT);
    private static final Slice INNER_TOP_RIGHT = new Slice(true, TOP_RIGHT);
    private static final Slice INNER_BOTTOM_RIGHT = new Slice(true, BOTTOM_RIGHT);
    private static final Slice INNER_BOTTOM_LEFT = new Slice(true, BOTTOM_LEFT);

    private final List<LytRect> rects = new ArrayList<>();

    private Identifier windowSprite = WINDOW_SPRITE;
    private Identifier innerSprite = WINDOW_INNER_SPRITE;

    /**
     * Changes the sprites used to draw the window. See the class documentation for the requirements on these sprites.
     */
    public PanelBlitter setSprites(Identifier windowSprite, Identifier innerSprite) {
        this.windowSprite = windowSprite;
        this.innerSprite = innerSprite;
        return this;
    }

    /**
     * The non-overlapping rectangles computed from {@link #rects}, along with information about which of their edges
     * and corners need to be drawn. Computed lazily.
     */
    @Nullable
    private List<Rectangle> processedRects;

    public PanelBlitter addBounds(Rect2i rect) {
        return addBounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public PanelBlitter addBounds(LytRect rect) {
        return addBounds(rect.x(), rect.y(), rect.width(), rect.height());
    }

    public PanelBlitter addBounds(int x, int y, int width, int height) {
        if (width > 0 && height > 0) {
            rects.add(new LytRect(x, y, width, height));
            processedRects = null;
        }
        return this;
    }

    public boolean isEmpty() {
        return rects.isEmpty();
    }

    public void clear() {
        rects.clear();
        processedRects = null;
    }

    public void blit(GuiGraphicsExtractor guiGraphics, int xOffset, int yOffset) {
        render(SpriteRenderer.of(guiGraphics), xOffset, yOffset, -1);
    }

    public void render(SpriteRenderer renderer, int xOffset, int yOffset, int color) {
        var window = getNineSlice(renderer, windowSprite);
        var inner = getNineSlice(renderer, innerSprite);
        if (window == null || inner == null) {
            // A resource pack replaced the sprites with something we cannot handle, fall back to drawing
            // each rectangle on its own.
            for (var rect : RectangleMerger.merge(rects)) {
                renderer.blitSprite(windowSprite, xOffset + rect.x(), yOffset + rect.y(), rect.width(),
                        rect.height(), color);
            }
            return;
        }

        var context = new RenderContext(renderer, windowSprite, window, innerSprite, inner, xOffset, yOffset, color);
        for (var rect : getProcessedRects()) {
            renderRect(context, rect);
        }
    }

    @Nullable
    private static GuiSpriteScaling.NineSlice getNineSlice(SpriteRenderer renderer, Identifier sprite) {
        if (renderer.getSprite(sprite).scaling() instanceof GuiSpriteScaling.NineSlice nineSlice) {
            return nineSlice;
        }
        return null;
    }

    private List<Rectangle> getProcessedRects() {
        if (processedRects == null) {
            processedRects = new ArrayList<>();
            for (var rect : RectangleMerger.merge(rects)) {
                processedRects.add(new Rectangle(rect.x(), rect.y(), rect.width(), rect.height()));
            }

            // Split or eliminate edges where rectangles touch each other
            for (var rect : processedRects) {
                for (var otherRect : processedRects) {
                    if (rect != otherRect) {
                        for (int side = 0; side < 4; side++) {
                            rect.mergeEdges(otherRect, side);
                        }
                    }
                }
            }
        }
        return processedRects;
    }

    private static void renderRect(RenderContext context, Rectangle rect) {
        var border = context.window.border();
        var outerTop = rect.outerTop();
        var outerRight = rect.outerRight();
        var outerBottom = rect.outerBottom();
        var outerLeft = rect.outerLeft();
        var innerTop = rect.topEdges.isEmpty() ? outerTop : outerTop + border.top();
        var innerRight = rect.rightEdges.isEmpty() ? outerRight : outerRight - border.right();
        var innerBottom = rect.bottomEdges.isEmpty() ? outerBottom : outerBottom - border.bottom();
        var innerLeft = rect.leftEdges.isEmpty() ? outerLeft : outerLeft + border.left();

        for (int side = 0; side < 4; side++) {
            for (var edge : rect.getEdgesForSide(side)) {
                int left, top, right, bottom;
                switch (side) {
                    case TOP -> {
                        left = Math.max(innerLeft, outerLeft + edge.start);
                        right = Math.min(innerRight, outerLeft + edge.end);
                        top = outerTop;
                        bottom = innerTop;
                    }
                    case RIGHT -> {
                        left = innerRight;
                        right = outerRight;
                        top = Math.max(innerTop, outerTop + edge.start);
                        bottom = Math.min(innerBottom, outerTop + edge.end);
                    }
                    case BOTTOM -> {
                        left = Math.max(innerLeft, outerLeft + edge.start);
                        right = Math.min(innerRight, outerLeft + edge.end);
                        top = innerBottom;
                        bottom = outerBottom;
                    }
                    case LEFT -> {
                        left = outerLeft;
                        right = innerLeft;
                        top = Math.max(innerTop, outerTop + edge.start);
                        bottom = Math.min(innerBottom, outerTop + edge.end);
                    }
                    default -> throw new IndexOutOfBoundsException("side");
                }

                renderEdge(context, edge.style, side, left, top, right, bottom);
            }
        }

        for (int i = 0; i < rect.corners.length; i++) {
            var corner = rect.corners[i];
            if (corner == null) {
                continue;
            }

            // We must use the width/height the corner would normally be, in case it is filled in
            // with an edge slice.
            var width = (i == 1 || i == 2) ? border.right() : border.left();
            var height = (i == 2 || i == 3) ? border.bottom() : border.top();
            var x = (i == 1 || i == 2) ? outerRight - width : outerLeft;
            var y = (i == 2 || i == 3) ? outerBottom - height : outerTop;
            context.draw(corner, x, y, width, height);
        }

        context.draw(FILL, innerLeft, innerTop, innerRight - innerLeft, innerBottom - innerTop);
    }

    /**
     * Rendering an edge potentially involves rendering a start or end cap if the edge hits an adjacent rectangle.
     */
    private static void renderEdge(RenderContext context, EdgeStyle style, int side, int left, int top, int right,
            int bottom) {
        if (right <= left || bottom <= top) {
            return;
        }

        if (style == EdgeStyle.NORMAL) {
            var slice = switch (side) {
                case TOP -> TOP_BORDER;
                case RIGHT -> RIGHT_BORDER;
                case BOTTOM -> BOTTOM_BORDER;
                case LEFT -> LEFT_BORDER;
                default -> throw new IndexOutOfBoundsException("side");
            };
            context.draw(slice, left, top, right - left, bottom - top);
            return;
        }

        Slice startCap, endCap;
        switch (side) {
            case TOP -> {
                startCap = INNER_BOTTOM_RIGHT;
                endCap = INNER_BOTTOM_LEFT;
            }
            case RIGHT -> {
                startCap = INNER_BOTTOM_LEFT;
                endCap = INNER_TOP_LEFT;
            }
            case BOTTOM -> {
                startCap = INNER_TOP_RIGHT;
                endCap = INNER_TOP_LEFT;
            }
            case LEFT -> {
                startCap = INNER_BOTTOM_RIGHT;
                endCap = INNER_TOP_RIGHT;
            }
            default -> throw new IndexOutOfBoundsException("side");
        }

        if (style == EdgeStyle.INNER_FILL_NO_START) {
            startCap = null;
        } else if (style == EdgeStyle.INNER_FILL_NO_END) {
            endCap = null;
        }

        if (side == LEFT || side == RIGHT) {
            if (startCap != null) {
                var height = context.getHeight(startCap);
                context.draw(startCap, left, top, context.getWidth(startCap), height);
                top += height;
            }
            if (endCap != null) {
                var height = context.getHeight(endCap);
                context.draw(endCap, left, bottom - height, context.getWidth(endCap), height);
                bottom -= height;
            }
        } else {
            if (startCap != null) {
                var width = context.getWidth(startCap);
                context.draw(startCap, left, top, width, context.getHeight(startCap));
                left += width;
            }
            if (endCap != null) {
                var width = context.getWidth(endCap);
                context.draw(endCap, right - width, top, width, context.getHeight(endCap));
                right -= width;
            }
        }

        context.draw(FILL, left, top, right - left, bottom - top);
    }

    private enum EdgeStyle {
        NORMAL,
        INNER_FILL,
        INNER_FILL_NO_START,
        INNER_FILL_NO_END
    }

    private record Edge(EdgeStyle style, int start, int end) {
        Edge(int start, int end) {
            this(EdgeStyle.NORMAL, start, end);
        }
    }

    /**
     * One of the nine slices of either the window sprite or the inner window sprite.
     */
    private record Slice(boolean inner, int index) {
    }

    private record RenderContext(SpriteRenderer renderer,
            Identifier windowSprite,
            GuiSpriteScaling.NineSlice window,
            Identifier innerSprite,
            GuiSpriteScaling.NineSlice inner,
            int xOffset,
            int yOffset,
            int color) {

        int getWidth(Slice slice) {
            var border = (slice.inner ? inner : window).border();
            return slice.index % 3 == 2 ? border.right() : border.left();
        }

        int getHeight(Slice slice) {
            var border = (slice.inner ? inner : window).border();
            return slice.index / 3 == 2 ? border.bottom() : border.top();
        }

        void draw(Slice slice, int x, int y, int width, int height) {
            if (width <= 0 || height <= 0) {
                return;
            }

            var nineSlice = slice.inner ? inner : window;
            var border = nineSlice.border();
            int col = slice.index % 3;
            int row = slice.index / 3;

            int srcX = switch (col) {
                case 0 -> 0;
                case 1 -> border.left();
                default -> nineSlice.width() - border.right();
            };
            int srcWidth = switch (col) {
                case 0 -> border.left();
                case 1 -> nineSlice.width() - border.left() - border.right();
                default -> border.right();
            };
            int srcY = switch (row) {
                case 0 -> 0;
                case 1 -> border.top();
                default -> nineSlice.height() - border.bottom();
            };
            int srcHeight = switch (row) {
                case 0 -> border.top();
                case 1 -> nineSlice.height() - border.top() - border.bottom();
                default -> border.bottom();
            };

            // Corners are never tiled, while edges and the center are unless the sprite asks for stretching
            boolean tile = (col == 1 || row == 1) && !nineSlice.stretchInner();
            renderer.blitRegion(
                    slice.inner ? innerSprite : windowSprite,
                    srcX, srcY, srcWidth, srcHeight,
                    xOffset + x, yOffset + y, width, height,
                    tile,
                    color);
        }
    }

    private static final class Rectangle {
        private final Slice[] corners = new Slice[] {
                OUTER_TOP_LEFT,
                OUTER_TOP_RIGHT,
                OUTER_BOTTOM_RIGHT,
                OUTER_BOTTOM_LEFT,
        };
        private final List<Edge> leftEdges = new ArrayList<>();
        private final List<Edge> topEdges = new ArrayList<>();
        private final List<Edge> rightEdges = new ArrayList<>();
        private final List<Edge> bottomEdges = new ArrayList<>();
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            this.leftEdges.add(new Edge(0, height));
            this.topEdges.add(new Edge(0, width));
            this.rightEdges.add(new Edge(0, height));
            this.bottomEdges.add(new Edge(0, width));
        }

        int outerLeft() {
            return x;
        }

        int outerTop() {
            return y;
        }

        int outerRight() {
            return x + width;
        }

        int outerBottom() {
            return y + height;
        }

        List<Edge> getEdgesForSide(int side) {
            return switch (side) {
                case TOP -> topEdges;
                case RIGHT -> rightEdges;
                case BOTTOM -> bottomEdges;
                case LEFT -> leftEdges;
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };
        }

        /**
         * If the other rectangle touches the given side of this rectangle, the parts of the edges on that side which
         * touch the other rectangle are replaced by a fill, possibly with inner corners at its ends.
         */
        void mergeEdges(Rectangle otherRect, int side) {
            var edges = getEdgesForSide(side);
            if (edges.isEmpty()) {
                return;
            }

            // Determine if the two rects touch on the given side
            var touching = switch (side) {
                case TOP -> outerTop() == otherRect.outerBottom();
                case RIGHT -> outerRight() == otherRect.outerLeft();
                case BOTTOM -> outerBottom() == otherRect.outerTop();
                case LEFT -> outerLeft() == otherRect.outerRight();
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };
            if (!touching) {
                return;
            }

            boolean horizontal = side == TOP || side == BOTTOM;
            var ourStart = horizontal ? x : y;
            var otherStart = horizontal ? otherRect.outerLeft() : otherRect.outerTop();
            var otherEnd = horizontal ? otherRect.outerRight() : otherRect.outerBottom();

            // Indices into corners for the corners at the start and end of this side
            var startCorner = switch (side) {
                case TOP, LEFT -> 0;
                case RIGHT -> 1;
                case BOTTOM -> 3;
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };
            var endCorner = switch (side) {
                case TOP -> 1;
                case RIGHT, BOTTOM -> 2;
                case LEFT -> 3;
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };

            var newEdges = new ArrayList<Edge>();
            for (var ourEdge : edges) {
                var edgeStart = ourStart + ourEdge.start;
                var edgeEnd = ourStart + ourEdge.end;

                // Keep edges that do not overlap the other rectangle
                if (edgeStart >= otherEnd || edgeEnd <= otherStart) {
                    newEdges.add(ourEdge);
                    continue;
                }

                // Keep the part of the edge that starts before the other rectangle does
                var overhangStart = otherStart - edgeStart;
                if (overhangStart > 0) {
                    newEdges.add(new Edge(ourEdge.start, ourEdge.start + overhangStart));
                }
                // Keep the part of the edge that extends beyond the other rectangle
                var overhangEnd = edgeEnd - otherEnd;
                if (overhangEnd > 0) {
                    newEdges.add(new Edge(ourEdge.end - overhangEnd, ourEdge.end));
                }

                if (overhangStart > 0 || overhangEnd > 0) {
                    // Add a fill only if the edge hasn't been entirely eliminated
                    var fillType = EdgeStyle.INNER_FILL;
                    if (overhangStart <= 0) {
                        fillType = EdgeStyle.INNER_FILL_NO_START;
                    } else if (overhangEnd <= 0) {
                        fillType = EdgeStyle.INNER_FILL_NO_END;
                    }

                    newEdges.add(new Edge(fillType,
                            ourEdge.start + Math.max(0, overhangStart),
                            ourEdge.end - Math.max(0, overhangEnd)));

                    // If the other rectangle is flush with our corner, the perpendicular border of
                    // the other rectangle continues through our corner.
                    if (overhangStart <= 0) {
                        corners[startCorner] = horizontal ? LEFT_BORDER : TOP_BORDER;
                    }
                    if (overhangEnd <= 0) {
                        corners[endCorner] = horizontal ? RIGHT_BORDER : BOTTOM_BORDER;
                    }
                } else {
                    // If the edge has been eliminated, also eliminate the corners
                    corners[startCorner] = null;
                    corners[endCorner] = null;
                }
            }
            edges.clear();
            edges.addAll(newEdges);
        }
    }
}
