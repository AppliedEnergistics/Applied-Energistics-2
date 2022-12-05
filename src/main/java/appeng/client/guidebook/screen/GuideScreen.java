package appeng.client.guidebook.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.client.Point;
import appeng.client.gui.DashPattern;
import appeng.client.gui.DashedRectangle;
import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.PageCollection;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.block.LytDocument;
import appeng.client.guidebook.document.block.LytHeading;
import appeng.client.guidebook.document.block.LytParagraph;
import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.layout.MinecraftFontMetrics;
import appeng.client.guidebook.render.ColorRef;
import appeng.client.guidebook.render.GuidePageTexture;
import appeng.client.guidebook.render.LightDarkMode;
import appeng.client.guidebook.render.SimpleRenderContext;
import appeng.core.AppEng;

public class GuideScreen extends Screen {
    private static final int HISTORY_SIZE = 100;
    private static final List<PageAnchor> history = new ArrayList<>();
    private static int historyPosition;
    private static final DashPattern DEBUG_NODE_OUTLINE = new DashPattern(1f, 4, 3, 0xFFFFFFFF, 500);
    private static final DashPattern DEBUG_CONTENT_OUTLINE = new DashPattern(0.5f, 2, 1, 0x7FFFFFFF, 500);
    private final PageCollection pages;

    private final GuideScrollbar scrollbar;
    private GuidePage currentPage;

    private Button backButton;
    private Button forwardButton;

    private GuideScreen(PageCollection pages, PageAnchor anchor) {
        super(Component.literal("AE2 Guidebook"));
        this.pages = pages;
        this.scrollbar = new GuideScrollbar();
        loadPage(anchor);
    }

    /**
     * Opens and resets history.
     */
    public static GuideScreen openNew(PageCollection pages, PageAnchor anchor) {
        // Append to history if it's not already appended
        if (history.lastIndexOf(anchor) != history.size()) {
            historyPosition = history.size();
            history.add(anchor);
        }

        return new GuideScreen(pages, anchor);
    }

    /**
     * Opens at current history position and only falls back to the index if the history is empty.
     */
    public static GuideScreen openAtPreviousPage(PageCollection pages, PageAnchor anchor) {
        if (historyPosition < history.size()) {
            anchor = history.get(historyPosition);
        } else {
            return openNew(pages, anchor);
        }

        return new GuideScreen(pages, anchor);
    }

    @Override
    protected void init() {
        super.init();

        updatePageLayout();

        // Add and re-position scrollbar
        var docRect = getDocumentRect();
        addRenderableWidget(scrollbar);
        scrollbar.move(
                docRect.right(),
                docRect.y(),
                docRect.height());

        GuideNavBar navbar = new GuideNavBar(this);
        addRenderableWidget(navbar);

        backButton = new Button(docRect.right() - 40, docRect.y() - 15, 20, 15, Component.literal("<"),
                button -> navigateBack());
        addRenderableWidget(backButton);
        forwardButton = new Button(docRect.right() - 20, docRect.y() - 15, 20, 15, Component.literal(">"),
                button -> navigateForward());
        addRenderableWidget(forwardButton);
        updateNavigationButtons();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        updateNavigationButtons();

        renderBackground(poseStack);

        // Set scissor rectangle to rect that we show the document in
        var documentRect = getDocumentRect();

        fill(poseStack, documentRect.x(), documentRect.y(), documentRect.right(), documentRect.bottom(), 0x80333333);

        // Move rendering to anchor @ 0,0 in the document rect
        var documentViewport = getDocumentViewport();
        poseStack.pushPose();
        poseStack.translate(documentRect.x() - documentViewport.x(), documentRect.y() - documentViewport.y(), 0);

        var document = currentPage.getDocument();
        var context = new SimpleRenderContext(
                documentViewport,
                poseStack,
                LightDarkMode.LIGHT_MODE);

        enableScissor(documentRect.x(), documentRect.y(), documentRect.right(), documentRect.bottom());

        // Render all text content in one large batch to improve performance
        var buffers = context.beginBatch();
        document.renderBatch(context, buffers);
        context.endBatch(buffers);

        document.render(context);

        disableScissor();

        // renderHoverOutline(document, context);

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 0, 100);

        super.render(poseStack, mouseX, mouseY, partialTick);

        poseStack.popPose();

        // Render tooltip
        if (document.getHoveredElement() != null) {
            renderTooltip(poseStack, mouseX, mouseY);
        }

    }

    private void renderTooltip(PoseStack poseStack, int x, int y) {
        var docPos = getDocumentPoint(x, y);
        if (docPos == null) {
            return;
        }

        var tooltip = dispatchInteraction(docPos.getX(), docPos.getY(), InteractiveElement::getTooltip)
                .orElse(null);
        if (tooltip != null) {
            renderTooltip(poseStack, tooltip, x, y);
        }
    }

    private static void renderHoverOutline(LytDocument document, SimpleRenderContext context) {
        var hoveredElement = document.getHoveredElement();
        if (hoveredElement != null) {
            // Fill a rectangle highlighting margins
            if (hoveredElement.node() instanceof LytBlock block) {
                var bounds = block.getBounds();
                if (block.getMarginTop() > 0) {
                    context.fillRect(
                            bounds.withHeight(block.getMarginTop()).move(0, -block.getMarginTop()),
                            new ColorRef(0x7FFFFF00));
                }
                if (block.getMarginBottom() > 0) {
                    context.fillRect(
                            bounds.withHeight(block.getMarginBottom()).move(0, bounds.height()),
                            new ColorRef(0x7FFFFF00));
                }
                if (block.getMarginLeft() > 0) {
                    context.fillRect(
                            bounds.withWidth(block.getMarginLeft()).move(-block.getMarginLeft(), 0),
                            new ColorRef(0x7FFFFF00));
                }
                if (block.getMarginRight() > 0) {
                    context.fillRect(
                            bounds.withWidth(block.getMarginRight()).move(bounds.width(), 0),
                            new ColorRef(0x7FFFFF00));
                }
            }

            // Fill the content rectangle
            DashedRectangle.render(context.poseStack(), hoveredElement.node().getBounds(), DEBUG_NODE_OUTLINE, 0);

            // Also outline any inline-elements in the block
            if (hoveredElement.content() != null) {
                if (hoveredElement.node() instanceof LytFlowContainer flowContainer) {
                    flowContainer.enumerateContentBounds(hoveredElement.content())
                            .forEach(bound -> {
                                DashedRectangle.render(context.poseStack(), bound, DEBUG_CONTENT_OUTLINE, 0);
                            });
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        var docPoint = getDocumentPoint(mouseX, mouseY);
        if (docPoint != null) {
            if (button == 3) {
                navigateBack();
            } else if (button == 4) {
                navigateForward();
            }

            return dispatchEvent(docPoint.getX(), docPoint.getY(), el -> {
                return el.mouseClicked(this, docPoint.getX(), docPoint.getY(), button);
            });
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }

        var docPoint = getDocumentPoint(mouseX, mouseY);
        if (docPoint != null) {
            return dispatchEvent(docPoint.getX(), docPoint.getY(), el -> {
                return el.mouseReleased(this, docPoint.getX(), docPoint.getY(), button);
            });
        } else {
            return false;
        }
    }

    public void navigateTo(ResourceLocation pageId) {
        navigateTo(new PageAnchor(pageId, null));
    }

    public void navigateTo(PageAnchor anchor) {
        if (currentPage.getId().equals(anchor.pageId())) {
            // TODO -> scroll up (?)
            return;
        }

        loadPage(anchor);

        // Remove anything from the history after the current page when we navigate to a new one
        if (historyPosition + 1 < history.size()) {
            history.subList(historyPosition + 1, history.size()).clear();
        }
        // Clamp history length
        if (history.size() >= HISTORY_SIZE) {
            history.subList(0, history.size() - HISTORY_SIZE).clear();
        }
        // Append to history
        historyPosition = history.size();
        history.add(anchor);
    }

    // Navigate to next page in history (only possible if we've navigated back previously)
    private void navigateForward() {
        if (historyPosition + 1 < history.size()) {
            loadPage(history.get(++historyPosition));
        }
    }

    // Navigate to previous page in history
    private void navigateBack() {
        if (historyPosition > 0) {
            loadPage(history.get(--historyPosition));
        }
    }

    private void loadPage(PageAnchor anchor) {
        GuidePageTexture.releaseUsedTextures();
        currentPage = pages.getPage(anchor.pageId());

        if (currentPage == null) {
            // Build a "not found" page dynamically
            currentPage = buildNotFoundPage(anchor);
        }

        scrollbar.setScrollAmount(0);
        updatePageLayout();

        // TODO ANCHOR
    }

    private GuidePage buildNotFoundPage(PageAnchor anchor) {
        var document = new LytDocument();
        var title = new LytHeading();
        title.appendText("Page not Found");
        title.setDepth(1);
        document.append(title);
        var body = new LytParagraph();
        body.appendText("Page " + anchor.pageId() + " could not be found.");
        document.append(body);

        return new GuidePage(
                AppEng.MOD_ID,
                anchor.pageId(),
                document);
    }

    @Override
    public void removed() {
        super.removed();
        GuidePageTexture.releaseUsedTextures();
    }

    public void reloadPage() {
        GuidePageTexture.releaseUsedTextures();
        currentPage = pages.getPage(currentPage.getId());
        updatePageLayout();
    }

    @FunctionalInterface
    interface EventInvoker {
        boolean invoke(InteractiveElement el);
    }

    private boolean dispatchEvent(int x, int y, EventInvoker invoker) {
        return dispatchInteraction(x, y, el -> {
            if (invoker.invoke(el)) {
                return Optional.of(true);
            } else {
                return Optional.empty();
            }
        }).orElse(false);
    }

    private <T> Optional<T> dispatchInteraction(int x, int y, Function<InteractiveElement, Optional<T>> invoker) {
        var underCursor = currentPage.getDocument().pick(x, y);
        if (underCursor != null) {
            // Iterate through content ancestors
            for (var el = underCursor.content(); el != null; el = el.getFlowParent()) {
                if (el instanceof InteractiveElement interactiveEl) {
                    var result = invoker.apply(interactiveEl);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }

            // Iterate through node ancestors
            for (var node = underCursor.node(); node != null; node = node.getParent()) {
                if (node instanceof InteractiveElement interactiveEl) {
                    var result = invoker.apply(interactiveEl);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void afterMouseMove() {
        super.afterMouseMove();

        var document = currentPage.getDocument();

        var mouseHandler = minecraft.mouseHandler;
        var scale = (double) minecraft.getWindow().getGuiScaledWidth()
                / (double) minecraft.getWindow().getScreenWidth();
        var x = mouseHandler.xpos() * scale;
        var y = mouseHandler.ypos() * scale;

        // If there's a widget under the cursor, ignore document hit-testing
        if (getChildAt(x, y).isPresent()) {
            document.setHoveredElement(null);
            return;
        }

        var docPoint = getDocumentPoint(x, y);
        if (docPoint != null) {
            var hoveredEl = document.pick(docPoint.getX(), docPoint.getY());
            document.setHoveredElement(hoveredEl);
        } else {
            document.setHoveredElement(null);
        }
    }

    @Nullable
    private Point getDocumentPoint(double screenX, double screenY) {
        var documentRect = getDocumentRect();

        if (screenX >= documentRect.x() && screenX < documentRect.right()
                && screenY >= documentRect.y() && screenY < documentRect.bottom()) {
            var docX = (int) Math.round(screenX - documentRect.x());
            var docY = (int) Math.round(screenY + scrollbar.getScrollAmount() - documentRect.y());
            return new Point(docX, docY);
        }

        return null; // Outside the document
    }

    private LytRect getDocumentRect() {
        // 20 virtual px margin
        var margin = 20;

        return new LytRect(margin, margin, width - 2 * margin, height - 2 * margin);
    }

    private LytRect getDocumentViewport() {
        var documentRect = getDocumentRect();
        return new LytRect(0, scrollbar.getScrollAmount(), documentRect.width(), documentRect.height());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!super.mouseScrolled(mouseX, mouseY, delta)) {
            return scrollbar.mouseScrolled(mouseX, mouseY, delta);
        }
        return true;
    }

    private void renderTooltip(PoseStack poseStack, GuideTooltip tooltip, int mouseX, int mouseY) {
        var minecraft = Minecraft.getInstance();
        var clientLines = tooltip.getLines(this);

        if (clientLines.isEmpty()) {
            return;
        }

        int frameWidth = 0;
        int frameHeight = clientLines.size() == 1 ? -2 : 0;

        for (var clientTooltipComponent : clientLines) {
            frameWidth = Math.max(frameWidth, clientTooltipComponent.getWidth(minecraft.font));
            frameHeight += clientTooltipComponent.getHeight();
        }

        if (!tooltip.getIcon().isEmpty()) {
            frameWidth += 18;
            frameHeight = Math.max(frameHeight, 18);
        }

        int x = mouseX + 12;
        int y = mouseY - 12;
        if (x + frameWidth > this.width) {
            x -= 28 + frameWidth;
        }

        if (y + frameHeight + 6 > this.height) {
            y = this.height - frameHeight - 6;
        }

        int zOffset = 400;

        TooltipFrame.render(poseStack, x, y, frameWidth, frameHeight, zOffset);

        float prevZOffset = itemRenderer.blitOffset;
        itemRenderer.blitOffset = zOffset;

        if (!tooltip.getIcon().isEmpty()) {
            x += 18;
        }

        var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, zOffset);
        int currentY = y;

        // Batch-render tooltip text first
        for (int i = 0; i < clientLines.size(); ++i) {
            var line = clientLines.get(i);
            line.renderText(minecraft.font, x, currentY, poseStack.last().pose(), bufferSource);
            currentY += line.getHeight() + (i == 0 ? 2 : 0);
        }

        bufferSource.endBatch();
        poseStack.popPose();

        // Then render tooltip decorations, items, etc.
        currentY = y;
        if (!tooltip.getIcon().isEmpty()) {
            itemRenderer.renderGuiItem(tooltip.getIcon(), x - 18, y);
        }

        for (int i = 0; i < clientLines.size(); ++i) {
            var line = clientLines.get(i);
            line.renderImage(minecraft.font, x, currentY, poseStack, this.itemRenderer, zOffset);
            currentY += line.getHeight() + (i == 0 ? 2 : 0);
        }
        this.itemRenderer.blitOffset = prevZOffset;
    }

    private void updatePageLayout() {
        var docViewport = getDocumentViewport();
        var context = new LayoutContext(new MinecraftFontMetrics(), docViewport);

        // Build layout if needed
        var document = currentPage.getDocument();
        document.updateLayout(context, docViewport.width());
        scrollbar.setContentHeight(document.getContentHeight());
    }

    public ResourceLocation getCurrentPageId() {
        return currentPage.getId();
    }

    private void updateNavigationButtons() {
        backButton.active = historyPosition > 0;
        forwardButton.active = historyPosition + 1 < history.size();
    }

    public PageCollection getPages() {
        return pages;
    }
}
