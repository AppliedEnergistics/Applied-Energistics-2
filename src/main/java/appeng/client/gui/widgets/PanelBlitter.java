package appeng.client.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

import appeng.client.gui.assets.GuiAssets;
import appeng.client.gui.util.RectangleMerger;
import appeng.client.guidebook.document.LytRect;

public class PanelBlitter {

    private final List<LytRect> rects = new ArrayList<>();

    private final List<Rectangle> processedRects = new ArrayList<>();

    private final SpriteSlice CENTER;
    private final SpriteSlice OUTER_TOP_LEFT;
    private final SpriteSlice OUTER_TOP_RIGHT;
    private final SpriteSlice OUTER_BOTTOM_RIGHT;
    private final SpriteSlice OUTER_BOTTOM_LEFT;
    private final SpriteSlice TOP_BORDER;
    private final SpriteSlice RIGHT_BORDER;
    private final SpriteSlice BOTTOM_BORDER;
    private final SpriteSlice LEFT_BORDER;
    private final SpriteSlice INNER_TOP_LEFT;
    private final SpriteSlice INNER_TOP_RIGHT;
    private final SpriteSlice INNER_BOTTOM_RIGHT;
    private final SpriteSlice INNER_BOTTOM_LEFT;

    public PanelBlitter() {
        var window = GuiAssets.getNineSliceSprite(GuiAssets.WINDOW_SPRITE);
        var inner = GuiAssets.getNineSliceSprite(GuiAssets.INNER_BORDER_SPRITE);

        CENTER = new SpriteSlice(window, 4);
        OUTER_TOP_LEFT = new SpriteSlice(window, 0);
        OUTER_TOP_RIGHT = new SpriteSlice(window, 2);
        OUTER_BOTTOM_RIGHT = new SpriteSlice(window, 8);
        OUTER_BOTTOM_LEFT = new SpriteSlice(window, 6);
        TOP_BORDER = new SpriteSlice(window, 1);
        RIGHT_BORDER = new SpriteSlice(window, 5);
        BOTTOM_BORDER = new SpriteSlice(window, 7);
        LEFT_BORDER = new SpriteSlice(window, 3);
        INNER_TOP_LEFT = new SpriteSlice(inner, 0);
        INNER_TOP_RIGHT = new SpriteSlice(inner, 2);
        INNER_BOTTOM_RIGHT = new SpriteSlice(inner, 8);
        INNER_BOTTOM_LEFT = new SpriteSlice(inner, 6);
    }

    public void addBounds(Rect2i rect) {
        addBounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public void addBounds(int x, int y, int width, int height) {
        rects.add(new LytRect(x, y, width, height));
        processedRects.clear();
    }

    public void blit(GuiGraphics graphics, int xOffset, int yOffset) {
        blit(graphics, xOffset, yOffset, 0, 0xFFFFFFFF);
    }

    public void blit(GuiGraphics graphics, int xOffset, int yOffset, int zOffset, int color) {
        SpriteLayer layer = new SpriteLayer();
        render(layer, 0, color);
        layer.render(graphics.pose(), xOffset, yOffset, zOffset);
    }

    public void render(SpriteLayer layer, int z, int color) {
        // Update processed rectangles lazily
        if (processedRects.size() != rects.size()) {
            processedRects.clear();
            for (var rect : RectangleMerger.merge(rects)) {
                processedRects.add(new Rectangle(rect.x(), rect.y(), rect.width(), rect.height()));
            }

            // Merge/Split Edges with other rectangles
            for (var rect : processedRects) {
                for (var otherRect : processedRects) {
                    if (rect == otherRect) {
                        continue;
                    }

                    // Split/eliminate left edges that touch the other rectangle
                    rect.mergeEdges(otherRect, 0);
                    rect.mergeEdges(otherRect, 1);
                    rect.mergeEdges(otherRect, 2);
                    rect.mergeEdges(otherRect, 3);
                }
            }
        }

        for (var rect : processedRects) {
            var outerTop = rect.outerTop();
            var outerRight = rect.outerRight();
            var outerBottom = rect.outerBottom();
            var outerLeft = rect.outerLeft();
            var innerTop = rect.topEdges.isEmpty() ? outerTop : outerTop + TOP_BORDER.height();
            var innerRight = rect.rightEdges.isEmpty() ? outerRight : outerRight - RIGHT_BORDER.width();
            var innerBottom = rect.bottomEdges.isEmpty() ? outerBottom : outerBottom - BOTTOM_BORDER.height();
            var innerLeft = rect.leftEdges.isEmpty() ? outerLeft : outerLeft + LEFT_BORDER.width();

            for (int side = 0; side < 4; side++) {
                var edges = rect.getEdgesForSide(side);

                for (var edge : edges) {
                    int el, et, eb, er;
                    switch (side) {
                        case 0 -> {
                            el = Math.max(innerLeft, outerLeft + edge.start);
                            er = Math.min(innerRight, outerLeft + edge.end);
                            et = outerTop;
                            eb = innerTop;
                        }
                        case 1 -> {
                            el = innerRight;
                            er = outerRight;
                            et = Math.max(innerTop, outerTop + edge.start);
                            eb = Math.min(innerBottom, outerTop + edge.end);
                        }
                        case 2 -> {
                            el = Math.max(innerLeft, outerLeft + edge.start);
                            er = Math.min(innerRight, outerLeft + edge.end);
                            et = innerBottom;
                            eb = outerBottom;
                        }
                        case 3 -> {
                            el = outerLeft;
                            er = innerLeft;
                            et = Math.max(innerTop, outerTop + edge.start);
                            eb = Math.min(innerBottom, outerTop + edge.end);
                        }
                        default -> throw new IndexOutOfBoundsException("side");
                    }

                    renderEdge(layer, edge.style, side, el, et, er, eb, z, color);
                }
            }

            for (int i = 0; i < rect.corners.length; i++) {
                var cornerStyle = rect.corners[i];
                if (cornerStyle == null) {
                    continue;
                }

                // We must use the width/height the corner would normally be, in case it is filled in
                // with an edge sprite.
                var border = cornerStyle.nineSlice.padding();
                var width = switch (i) {
                    default -> border.left();
                    case 1, 2 -> border.right();
                };
                var height = switch (i) {
                    default -> border.top();
                    case 2, 3 -> border.bottom();
                };

                var x = switch (i) {
                    // Left aligned
                    default -> rect.x;
                    // Right aligned
                    case 1, 2 -> rect.outerRight() - width;
                };
                var y = switch (i) {
                    // Top aligned
                    default -> rect.y;
                    // Bottom aligned
                    case 2, 3 -> rect.outerBottom() - height;
                };

                cornerStyle.addQuad(layer, x, y, z, width, height, color);
            }

            CENTER.addQuad(layer, innerLeft, innerTop, z,
                    innerRight - innerLeft, innerBottom - innerTop,
                    color);
        }
    }

    /**
     * Rendering an edge potentially involves rendering a start or end cap if the edge hits an adjacent rectangle.
     */
    private void renderEdge(
            SpriteLayer layer,
            EdgeStyle style,
            int side,
            int left,
            int top,
            int right,
            int bottom,
            int z,
            int color) {
        if (right <= left || bottom <= top) {
            return;
        }

        if (style == EdgeStyle.NORMAL) {
            var edgeStyle = switch (side) {
                default -> TOP_BORDER;
                case 1 -> RIGHT_BORDER;
                case 2 -> BOTTOM_BORDER;
                case 3 -> LEFT_BORDER;
            };
            edgeStyle.addQuad(layer, left, top, z, right - left, bottom - top, color);
        } else {
            SpriteSlice innerStartCorner;
            SpriteSlice innerEndCorner;
            switch (side) {
                case 0 -> {
                    innerStartCorner = INNER_BOTTOM_RIGHT;
                    innerEndCorner = INNER_BOTTOM_LEFT;
                }
                case 1 -> {
                    innerStartCorner = INNER_BOTTOM_LEFT;
                    innerEndCorner = INNER_TOP_LEFT;
                }
                case 2 -> {
                    innerStartCorner = INNER_TOP_RIGHT;
                    innerEndCorner = INNER_TOP_LEFT;
                }
                case 3 -> {
                    innerStartCorner = INNER_BOTTOM_RIGHT;
                    innerEndCorner = INNER_TOP_RIGHT;
                }
                default -> throw new IndexOutOfBoundsException("side");
            }

            if (style == EdgeStyle.INNER_FILL_NO_START) {
                innerStartCorner = null;
            } else if (style == EdgeStyle.INNER_FILL_NO_END) {
                innerEndCorner = null;
            }

            if (side == 1 || side == 3) {
                // Vertical
                if (innerStartCorner != null) {
                    innerStartCorner.addQuad(layer, left, top, z, innerStartCorner.width(),
                            innerStartCorner.height(), color);
                    top += innerStartCorner.height();
                }
                if (innerEndCorner != null) {
                    innerEndCorner.addQuad(layer, left, bottom - innerEndCorner.height(), z,
                            innerEndCorner.width(), innerEndCorner.height(), color);
                    bottom -= innerEndCorner.height();
                }
            } else {
                // Horizontal
                if (innerStartCorner != null) {
                    innerStartCorner.addQuad(layer, left, top, z, innerStartCorner.width(),
                            innerStartCorner.height(), color);
                    left += innerStartCorner.width();
                }
                if (innerEndCorner != null) {
                    innerEndCorner.addQuad(layer, right - innerEndCorner.width(), top, z,
                            innerEndCorner.width(), innerEndCorner.height(), color);
                    right -= innerEndCorner.width();
                }
            }
            if (right - left > 0 && bottom - top > 0) {
                CENTER.addQuad(
                        layer,
                        left, top, z,
                        right - left, bottom - top,
                        color);
            }
        }
    }

    private record Edge(EdgeStyle style, int start, int end) {
        public Edge(int start, int end) {
            this(EdgeStyle.NORMAL, start, end);
        }
    }

    private final class Rectangle {
        SpriteSlice[] corners = new SpriteSlice[] {
                OUTER_TOP_LEFT,
                OUTER_TOP_RIGHT,
                OUTER_BOTTOM_RIGHT,
                OUTER_BOTTOM_LEFT,
        };
        private final List<Edge> leftEdges = new ArrayList<>();
        private final List<Edge> topEdges = new ArrayList<>();
        private final List<Edge> rightEdges = new ArrayList<>();
        private final List<Edge> bottomEdges = new ArrayList<>();
        int x;
        int y;
        int width;
        int height;

        public Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            this.leftEdges.add(new Edge(0, height));
            this.topEdges.add(new Edge(0, width));
            this.rightEdges.add(new Edge(0, height));
            this.bottomEdges.add(new Edge(0, width));
        }

        public int outerLeft() {
            return x;
        }

        public int outerTop() {
            return y;
        }

        public int outerRight() {
            return x + width;
        }

        public int outerBottom() {
            return y + height;
        }

        public List<Edge> getEdgesForSide(int side) {
            return switch (side) {
                case 0 -> topEdges;
                case 1 -> rightEdges;
                case 2 -> bottomEdges;
                case 3 -> leftEdges;
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };
        }

        public Rectangle copy() {
            var result = new Rectangle(
                    x, y, width, height);
            System.arraycopy(corners, 0, result.corners, 0, result.corners.length);
            for (int i = 0; i < 4; i++) {
                result.getEdgesForSide(i).clear();
                result.getEdgesForSide(i).addAll(getEdgesForSide(i));
            }
            return result;
        }

        public void mergeEdges(Rectangle otherRect, int side) {
            var edges = getEdgesForSide(side);

            if (edges.isEmpty()) {
                return;
            }
            // Determine if the two rects touch on the given side
            var touching = switch (side) {
                case 0 -> outerTop() == otherRect.outerBottom();
                case 1 -> outerRight() == otherRect.outerLeft();
                case 2 -> outerBottom() == otherRect.outerTop();
                case 3 -> outerLeft() == otherRect.outerRight();
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };
            if (!touching) {
                return;
            }

            var ourStart = switch (side) {
                case 0, 2 -> x;
                case 1, 3 -> y;
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };
            var otherStart = switch (side) {
                case 0, 2 -> otherRect.outerLeft();
                case 1, 3 -> otherRect.outerTop();
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };
            var otherEnd = switch (side) {
                case 0, 2 -> otherRect.outerRight();
                case 1, 3 -> otherRect.outerBottom();
                default -> throw new IllegalStateException("Unexpected value: " + side);
            };

            var tempEdges = new ArrayList<Edge>();
            for (var ourEdge : edges) {
                var edgeStart = ourStart + ourEdge.start;
                var edgeEnd = ourStart + ourEdge.end;

                // Determine overlap
                if (edgeStart < otherEnd && edgeEnd > otherStart) {
                    // Add split edge for top part that starts before the other rectangle does
                    var overhangStart = otherStart - edgeStart;
                    if (overhangStart > 0) {
                        tempEdges.add(new Edge(edgeStart - ourStart, edgeStart - ourStart + overhangStart));
                    }
                    // Add split edge for bottom part that starts before the other rectangle does
                    var overhangEnd = edgeEnd - otherEnd;
                    if (overhangEnd > 0) {
                        tempEdges.add(new Edge(edgeEnd - ourStart - overhangEnd, edgeEnd));
                    }

                    var startCorner = switch (side) {
                        case 0, 3 -> 0;
                        case 1 -> 1;
                        case 2 -> 3;
                        default -> throw new IllegalStateException("Unexpected value: " + side);
                    };
                    var endCorner = switch (side) {
                        case 0 -> 1;
                        case 1, 2 -> 2;
                        case 3 -> 3;
                        default -> throw new IllegalStateException("Unexpected value: " + side);
                    };

                    // Add a fill only if the edge hasn't been entirely eliminated
                    if (overhangStart > 0 || overhangEnd > 0) {
                        var fillType = EdgeStyle.INNER_FILL;
                        if (overhangStart <= 0) {
                            fillType = EdgeStyle.INNER_FILL_NO_START;
                        } else if (overhangEnd <= 0) {
                            fillType = EdgeStyle.INNER_FILL_NO_END;
                        }

                        tempEdges.add(
                                new Edge(fillType, edgeStart - ourStart + overhangStart,
                                        edgeEnd - ourStart - overhangEnd));
                        if (overhangStart <= 0) {
                            corners[startCorner] = switch (side) {
                                case 0, 2 -> LEFT_BORDER;
                                case 1, 3 -> TOP_BORDER;
                                default -> throw new IllegalStateException("Unexpected value: " + side);
                            };
                        }
                        if (overhangEnd <= 0) {
                            corners[endCorner] = switch (side) {
                                case 0, 2 -> RIGHT_BORDER;
                                case 1, 3 -> BOTTOM_BORDER;
                                default -> throw new IllegalStateException("Unexpected value: " + side);
                            };
                        }
                    } else {
                        // If the edge has been eliminated, also eliminate the corners
                        corners[startCorner] = null;
                        corners[endCorner] = null;
                    }
                } else {
                    tempEdges.add(ourEdge);
                }
            }
            edges.clear();
            edges.addAll(tempEdges);
        }

        public boolean contains(Rectangle rect) {
            var right = rect.x + rect.width;
            var bottom = rect.y + rect.height;
            return rect.x >= x && rect.y >= y && right <= x + width && bottom <= y + height;
        }
    }

    private enum EdgeStyle {
        NORMAL,
        INNER_FILL,
        INNER_FILL_NO_START,
        INNER_FILL_NO_END
    }

    /**
     * We use a 9-slice sprite for the normal window as well as the "inner" borders used to draw corners between
     * adjacent rectangles. This record represents one slice of the 9-slice sprite. The slice index is counted
     * left-to-right, then top-to-bottom starting at 0.
     */
    private record SpriteSlice(GuiAssets.NineSliceSprite nineSlice, int slice) {
        int height() {
            return slice / 3 == 2 ? nineSlice.padding().bottom() : nineSlice.padding().top();
        }

        int width() {
            return slice % 3 == 2 ? nineSlice.padding().right() : nineSlice.padding().left();
        }

        public void addQuad(SpriteLayer layer, float x, float y, float z, float width, float height, int color) {
            int row = slice / 3;
            int col = slice % 3;

            var uv = nineSlice.uv();
            var minU = uv[col];
            var maxU = uv[col + 1];
            var minV = uv[4 + row];
            var maxV = uv[4 + row + 1];

            layer.addQuad(x, y, z, width, height, color, minU, maxU, minV, maxV);
        }
    }
}
