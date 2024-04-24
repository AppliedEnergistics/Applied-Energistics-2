package appeng.client.guidebook.document.interaction;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.layout.MinecraftFontMetrics;
import appeng.client.guidebook.render.SimpleRenderContext;
import appeng.siteexport.ExportableResourceProvider;
import appeng.siteexport.ResourceExporter;

/**
 * A {@link GuideTooltip} that renders a {@link LytBlock} as the tooltip content.
 */
public class ContentTooltip implements GuideTooltip {
    private final List<ClientTooltipComponent> components;

    // The window size for which we performed layout
    @Nullable
    private LytRect layoutViewport;
    @Nullable
    private LytRect layoutBox;

    private final LytBlock content;

    public ContentTooltip(LytBlock content) {
        this.content = content;

        this.components = List.of(
                new ClientTooltipComponent() {
                    @Override
                    public int getHeight() {
                        return getLayoutBox().height();
                    }

                    @Override
                    public int getWidth(Font font) {
                        return getLayoutBox().width();
                    }

                    @Override
                    public void renderText(Font font, int x, int y, Matrix4f matrix,
                            MultiBufferSource.BufferSource bufferSource) {
                        getLayoutBox(); // Updates layout

                        var guiGraphics = new GuiGraphics(Minecraft.getInstance(), bufferSource);
                        var poseStack = guiGraphics.pose();
                        poseStack.mulPose(matrix);
                        poseStack.translate(x, y, 0);

                        var ctx = new SimpleRenderContext(layoutViewport, guiGraphics);
                        content.renderBatch(ctx, bufferSource);
                    }

                    @Override
                    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
                        getLayoutBox(); // Updates layout

                        var pose = guiGraphics.pose();
                        pose.pushPose();
                        pose.translate(x, y, 0);
                        var ctx = new SimpleRenderContext(layoutViewport, guiGraphics);
                        content.render(ctx);
                        pose.popPose();
                    }
                });
    }

    @Override
    public List<ClientTooltipComponent> getLines() {
        return components;
    }

    public LytBlock getContent() {
        return content;
    }

    private LytRect getLayoutBox() {
        var window = Minecraft.getInstance().getWindow();
        var currentViewport = new LytRect(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight());
        if (layoutBox == null || !currentViewport.equals(layoutViewport)) {
            layoutViewport = currentViewport;
            var layoutContext = new LayoutContext(new MinecraftFontMetrics());
            layoutBox = content.layout(layoutContext, 0, 0, window.getGuiScaledWidth() / 2);
        }
        return layoutBox;
    }

    @Override
    public void exportResources(ResourceExporter exporter) {
        ExportableResourceProvider.visit(content, exporter);
    }
}
