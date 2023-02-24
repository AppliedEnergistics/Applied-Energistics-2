package appeng.client.guidebook.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.joml.Matrix4f;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;

final class TooltipFrame extends GuiComponent {
    private TooltipFrame() {
    }

    public static void render(PoseStack poseStack, int x, int y, int totalWidth, int totalHeight, int z) {
        int o = 0xf0100010;
        int p = 0x505000ff;
        int q = 0x5028007f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix4f = poseStack.last().pose();
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 4, x + totalWidth + 3, y - 3, z, o, o);
        fillGradient(matrix4f, bufferBuilder, x - 3, y + totalHeight + 3, x + totalWidth + 3, y + totalHeight + 4, z, o,
                o);
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + totalWidth + 3, y + totalHeight + 3, z, o, o);
        fillGradient(matrix4f, bufferBuilder, x - 4, y - 3, x - 3, y + totalHeight + 3, z, o, o);
        fillGradient(matrix4f, bufferBuilder, x + totalWidth + 3, y - 3, x + totalWidth + 4, y + totalHeight + 3, z, o,
                o);
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3 + 1, x - 3 + 1, y + totalHeight + 3 - 1, z, p, q);
        fillGradient(matrix4f, bufferBuilder, x + totalWidth + 2, y - 3 + 1, x + totalWidth + 3,
                y + totalHeight + 3 - 1, z, p, q);
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + totalWidth + 3, y - 3 + 1, z, p, p);
        fillGradient(matrix4f, bufferBuilder, x - 3, y + totalHeight + 2, x + totalWidth + 3, y + totalHeight + 3, z, q,
                q);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }
}
