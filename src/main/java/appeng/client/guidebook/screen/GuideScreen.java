package appeng.client.guidebook.screen;

import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.document.LytRect;
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
    private GuidePage currentPage;
    private int scrollX;
    private int scrollY;

    public GuideScreen(GuidePage currentPage) {
        super(Component.literal("AE2 Guidebook"));
        this.currentPage = Objects.requireNonNull(currentPage);
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
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        // Set scissor rectangle to rect that we show the document in
        var documentRect = getDocumentRect();

        fill(poseStack, documentRect.x(), documentRect.y(), documentRect.right(), documentRect.bottom(), 0x80333333);

        enableScissor(documentRect.x(), documentRect.y(), documentRect.right(), documentRect.bottom());

        // Move rendering to anchor @ 0,0 in the document rect
        poseStack.pushPose();
        poseStack.translate(documentRect.x(), documentRect.y(), 0);

        var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        var document = currentPage.getDocument();
        var context = new SimpleRenderContext(this,
                getDocumentViewport(),
                poseStack,
                bufferSource,
                LightDarkMode.LIGHT_MODE);
        document.render(context);

        bufferSource.endBatch();

        poseStack.popPose();
        disableScissor();

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    private LytRect getDocumentRect() {
        // 20 virtual px margin
        var margin = 20;

        return new LytRect(margin, margin, width - 2 * margin, height - 2 * margin);
    }

    private LytRect getDocumentViewport() {
        var documentRect = getDocumentRect();
        return new LytRect(scrollX, scrollY, documentRect.width(), documentRect.height());
    }
}
