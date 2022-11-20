package appeng.client.guidebook.screen;

import appeng.client.gui.DashPattern;
import appeng.client.gui.DashedRectangle;
import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.client.guidebook.layout.SimpleLayoutContext;
import appeng.client.guidebook.render.LightDarkMode;
import appeng.client.guidebook.render.SimpleRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class GuideScreen extends Screen {
    private static final DashPattern DEBUG_NODE_OUTLINE = new DashPattern(1f, 4, 3, 0xFFFFFFFF, 500);
    private static final DashPattern DEBUG_CONTENT_OUTLINE = new DashPattern(0.5f, 2, 1, 0x7FFFFFFF, 500);

    private final GuidePage currentPage;
    private final GuideScrollbar scrollbar;

    public GuideScreen(GuidePage currentPage) {
        super(Component.literal("AE2 Guidebook"));
        this.currentPage = Objects.requireNonNull(currentPage);
        this.scrollbar = new GuideScrollbar();
    }

    @Override
    protected void init() {
        super.init();

        var docViewport = getDocumentViewport();
        var context = new SimpleLayoutContext(
                minecraft.font,
                docViewport
        );

        // Build layout if needed
        var document = currentPage.getDocument();
        document.updateLayout(context, docViewport.width());

        // Add and re-position scrollbar
        var docRect = getDocumentRect();
        addRenderableWidget(scrollbar);
        scrollbar.move(
                docRect.right(),
                docRect.y(),
                docRect.height()
        );
        scrollbar.setContentHeight(document.getContentHeight());
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        // Set scissor rectangle to rect that we show the document in
        var documentRect = getDocumentRect();

        fill(poseStack, documentRect.x(), documentRect.y(), documentRect.right(), documentRect.bottom(), 0x80333333);

        // Move rendering to anchor @ 0,0 in the document rect
        var documentViewport = getDocumentViewport();
        poseStack.pushPose();
        poseStack.translate(documentRect.x() - documentViewport.x(), documentRect.y() - documentViewport.y(), 0);

        var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        var document = currentPage.getDocument();
        var context = new SimpleRenderContext(this,
                documentViewport,
                poseStack,
                bufferSource,
                LightDarkMode.LIGHT_MODE);

        enableScissor(documentRect.x(), documentRect.y(), documentRect.right(), documentRect.bottom());

        document.render(context);
        bufferSource.endBatch();

        disableScissor();

        var hoveredElement = document.getHoveredElement();
        if (hoveredElement != null) {
            DashedRectangle.render(context.poseStack(), hoveredElement.node().getBounds(), DEBUG_NODE_OUTLINE, 0);
            if (hoveredElement.content() != null) {
                if (hoveredElement.node() instanceof LytFlowContainer flowContainer) {
                    flowContainer.enumerateContentBounds(hoveredElement.content())
                            .forEach(bound -> {
                                DashedRectangle.render(context.poseStack(), bound, DEBUG_CONTENT_OUTLINE, 0);
                            });
                }
            }
        }

        poseStack.popPose();

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public void afterMouseMove() {
        super.afterMouseMove();

        var document = currentPage.getDocument();

        var documentRect = getDocumentRect();
        var mouseHandler = minecraft.mouseHandler;
        var scale = (double) minecraft.getWindow().getGuiScaledWidth() / (double) minecraft.getWindow().getScreenWidth();
        var x = mouseHandler.xpos() * scale - documentRect.x();
        var y = mouseHandler.ypos() * scale - documentRect.y();

        if (x >= 0 && x < documentRect.width() && y >= 0 && y < documentRect.height()) {
            var docX = (int) Math.round(x);
            var docY = (int) Math.round(y + scrollbar.getScrollAmount());

            var hoveredEl = document.hitTest(docX, docY);
            document.setHoveredElement(hoveredEl);
        } else {
            document.setHoveredElement(null);
        }
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
}
