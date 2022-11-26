package appeng.client.guidebook.screen;

import appeng.client.Point;
import appeng.client.guidebook.GuideManager;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytParagraph;
import appeng.client.guidebook.document.flow.LytFlowSpan;
import appeng.client.guidebook.layout.SimpleLayoutContext;
import appeng.client.guidebook.navigation.NavigationNode;
import appeng.client.guidebook.navigation.NavigationTree;
import appeng.client.guidebook.render.LightDarkMode;
import appeng.client.guidebook.render.SimpleRenderContext;
import appeng.client.guidebook.render.SymbolicColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GuideNavBar extends AbstractWidget {
    private static final int WIDTH_CLOSED = 15;
    private static final int WIDTH_OPEN = 150;
    private static final int CHILD_ROW_INDENT = 10;
    private static final int PARENT_ROW_INDENT = 7;

    private NavigationTree navTree;

    private final List<Row> rows = new ArrayList<>();

    private final GuideScreen screen;

    private int scrollOffset;

    private State state = State.CLOSED;

    public GuideNavBar(GuideScreen screen) {
        super(0, 0, WIDTH_CLOSED, screen.height, Component.literal("Navigation Tree"));
        this.screen = screen;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (state != State.OPENING && state != State.OPEN) {
            return;
        }

        var row = pickRow(mouseX, mouseY);
        if (row != null) {
            row.expanded = !row.expanded;
            updateLayout();

            screen.navigateTo(row.node.pageId());
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (state != State.OPENING && state != State.OPEN) {
            return false;
        }

        setScrollOffset((int) Math.round(scrollOffset - dragY));
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (state != State.OPENING && state != State.OPEN) {
            return false;
        }

        setScrollOffset((int) Math.round(scrollOffset - delta * 20));
        return true;
    }

    private void setScrollOffset(int offset) {
        var maxScrollOffset = 0;
        if (!rows.isEmpty()) {
            var contentHeight = rows.get(rows.size() - 1).bottom - rows.get(0).top;
            maxScrollOffset = Math.max(0, contentHeight - height);
        }
        scrollOffset = Mth.clamp(offset, 0, maxScrollOffset);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var viewport = new LytRect(0, scrollOffset, width, height);
        var renderContext = new SimpleRenderContext(screen, viewport, poseStack, LightDarkMode.LIGHT_MODE);

        boolean containsMouse = (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY <= y + height);
        switch (state) {
            case CLOSED -> {
                if (containsMouse) {
                    state = State.OPENING;
                }
            }
            case OPENING -> {
                width = Math.round(width + Math.max(1, partialTick * (WIDTH_OPEN - WIDTH_CLOSED)));
                if (width >= WIDTH_OPEN) {
                    width = WIDTH_OPEN;
                    state = State.OPEN;
                }
            }
            case OPEN -> {
                if (!containsMouse) {
                    state = State.CLOSING;
                }
            }
            case CLOSING -> {
                width = Math.round(width - Math.max(1, partialTick * (WIDTH_OPEN - WIDTH_CLOSED)));
                if (width <= WIDTH_CLOSED) {
                    width = WIDTH_CLOSED;
                    state = State.CLOSED;
                }
            }
        }

        updateMousePos(mouseX, mouseY);

        // Check if we need to re-layout
        var currentNavTree = GuideManager.INSTANCE.getNavigationTree();
        if (currentNavTree != this.navTree) {
            recreateRows();
        }

        if (state == State.CLOSED) {
            renderContext.fillGradientHorizontal(x, y, width, height, SymbolicColor.NAVBAR_BG_TOP.ref(), SymbolicColor.NAVBAR_BG_BOTTOM.ref());

            var p1 = new Vec2(width - 4, height / 2f);
            var p2 = new Vec2(4, height / 2f - 5);
            var p3 = new Vec2(4, height / 2f + 5);

            renderContext.fillTriangle(p1, p2, p3, SymbolicColor.NAVBAR_EXPAND_ARROW.ref());
        } else {
            renderContext.fillGradientVertical(x, y, width, height, SymbolicColor.NAVBAR_BG_TOP.ref(), SymbolicColor.NAVBAR_BG_BOTTOM.ref());
        }

        if (state != State.CLOSED) {
            enableScissor(x, y, width, height);

            poseStack.pushPose();
            poseStack.translate(x, y - scrollOffset, 0);

            // Draw a backdrop on the hovered row before starting batch rendering
            var hoveredRow = pickRow(mouseX, mouseY);
            if (hoveredRow != null) {
                renderContext.fillRect(hoveredRow.getBounds(), SymbolicColor.NAVBAR_ROW_HOVER.ref());
            }

            // Render Text in batch
            var buffers = renderContext.beginBatch();
            for (var row : rows) {
                if (!row.isVisible(viewport)) {
                    continue; // Cull this row, it's not in the viewport
                }
                row.paragraph.renderBatch(renderContext, buffers);
            }
            renderContext.endBatch(buffers);

            // Render decorations, icons, etc.
            for (var row : rows) {
                if (!row.isVisible(viewport)) {
                    continue; // Cull this row, it's not in the viewport
                }
                if (row.hasChildren) {
                    float x = row.getBounds().x();
                    x += 5;
                    float y = row.getBounds().y();
                    y += 2f;
                    Vec2 p1, p2, p3;
                    if (row.expanded) {
                        // Triangle points down
                        p1 = new Vec2(x + 5, y);
                        p2 = new Vec2(x, y);
                        p3 = new Vec2(x + 2.5f, y + 5);
                    } else {
                        // Triangle points right
                        p1 = new Vec2(x + 5, y + 2.5f);
                        p2 = new Vec2(x, y);
                        p3 = new Vec2(x, y + 5);
                    }

                    var color = row == hoveredRow ? SymbolicColor.LINK : SymbolicColor.BODY_TEXT;
                    renderContext.fillTriangle(p1, p2, p3, color.ref());
                }

                var icon = row.node.icon();
                if (!icon.isEmpty()) {
                    renderContext.renderItem(icon, row.paragraph.getBounds().x() - 9, row.paragraph.getBounds().y(), 8, 8);
                }
            }

            poseStack.popPose();

            disableScissor();
        }
    }

    @Nullable
    private Row pickRow(double x, double y) {
        var vpPos = getViewportPoint(x, y);
        if (vpPos != null) {
            for (var row : rows) {
                if (row.isVisible() && vpPos.getY() >= row.top && vpPos.getY() < row.bottom) {
                    return row;
                }
            }
        }
        return null;
    }

    private void updateMousePos(double x, double y) {
        var vpPos = getViewportPoint(x, y);
        for (Row row : rows) {
            if (!row.isVisible()) {
                continue;
            }

            if (vpPos != null && row.contains(vpPos.getX(), vpPos.getY())) {
                row.paragraph.onMouseEnter(row.span);
            } else {
                row.paragraph.onMouseLeave();
            }
        }
    }

    private void recreateRows() {
        this.navTree = GuideManager.INSTANCE.getNavigationTree();
        // Save Freeze expanded / scroll position
        this.rows.clear();

        for (var rootNode : this.navTree.getRootNodes()) {
            var row = new Row(rootNode, null);
            rows.add(row);

            // Add second level
            for (var child : row.node.children()) {
                row.hasChildren = true;
                var childRow = new Row(child, row);
                rows.add(childRow);
            }
        }

        updateLayout();
    }

    private void updateLayout() {
        var context = new SimpleLayoutContext(
                Minecraft.getInstance().font,
                new LytRect(0, 0, WIDTH_OPEN, height)
        );

        var currentY = 0;
        for (var row : this.rows) {
            if (!row.isVisible()) {
                continue;
            }

            // Child-Rows should be indented and their parents
            // need an indent too for the expand/collapse indicator
            int indent;
            if (row.hasChildren) {
                indent = PARENT_ROW_INDENT;
            } else if (row.parent != null) {
                indent = CHILD_ROW_INDENT;
            } else {
                indent = 0;
            }

            if (!row.node.icon().isEmpty()) {
                indent += 8; // Indent for icon;
            }

            var x = indent;
            var width = WIDTH_OPEN - indent;
            var bounds = row.paragraph.layout(context, x, currentY, width);
            row.top = bounds.y();
            row.bottom = bounds.bottom();
            currentY = bounds.bottom();
        }
    }

    /**
     * Gets a point in the coordinate space of the scrollable viewport.
     */
    @Nullable
    private Point getViewportPoint(double screenX, double screenY) {
        if (state != State.OPENING && state != State.OPEN) {
            return null;
        }

        if (screenX >= x && screenX < x + width
                && screenY >= y && screenY < y + height) {
            var vpX = (int) Math.round(screenX - x);
            var vpY = (int) Math.round(screenY + scrollOffset - y);
            return new Point(vpX, vpY);
        }

        return null; // Outside the viewport
    }

    private class Row {
        private final NavigationNode node;
        private final LytParagraph paragraph = new LytParagraph();
        public final LytFlowSpan span;
        private boolean expanded;
        private final Row parent;
        private boolean hasChildren;
        public int top;
        public int bottom;

        public Row(NavigationNode node, Row parent) {
            this.node = node;
            this.parent = parent;

            span = new LytFlowSpan();
            span.appendText(node.title());
            span.modifyHoverStyle(style -> style.color(SymbolicColor.LINK.ref()));
            this.paragraph.append(span);
        }

        public LytRect getBounds() {
            return new LytRect(0, top, width, bottom - top);
        }

        public boolean contains(int x, int y) {
            return x >= 0 && x < width && y >= top && y < bottom;
        }

        public boolean isVisible() {
            return parent == null || parent.expanded;
        }

        // Is this row visible in the given viewport?
        public boolean isVisible(LytRect viewport) {
            return isVisible() && bottom > viewport.y() && top < viewport.bottom();
        }
    }

    enum State {
        CLOSED,
        OPENING,
        OPEN,
        CLOSING
    }
}
