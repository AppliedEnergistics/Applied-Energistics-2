package appeng.client.guidebook.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import appeng.client.guidebook.document.LytRect;

public record SimpleRenderContext(
        @Override LytRect viewport,
        @Override PoseStack poseStack,
        @Override LightDarkMode lightDarkMode) implements RenderContext {

    @Override
    public int resolveColor(ColorRef ref) {
        if (ref.symbolic != null) {
            return ref.symbolic.resolve(lightDarkMode);
        } else {
            return ref.concrete;
        }
    }

    @Override
    public void fillRect(LytRect rect, ColorRef topLeft, ColorRef topRight, ColorRef bottomRight, ColorRef bottomLeft) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var tesselator = Tesselator.getInstance();
        var builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var matrix = poseStack.last().pose();
        final int z = 0;
        builder.vertex(matrix, rect.right(), rect.y(), z).color(resolveColor(topRight)).endVertex();
        builder.vertex(matrix, rect.x(), rect.y(), z).color(resolveColor(topLeft)).endVertex();
        builder.vertex(matrix, rect.x(), rect.bottom(), z).color(resolveColor(bottomLeft)).endVertex();
        builder.vertex(matrix, rect.right(), rect.bottom(), z).color(resolveColor(bottomRight)).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }

    @Override
    public void fillTexturedRect(LytRect rect, AbstractTexture texture, ColorRef topLeft, ColorRef topRight,
            ColorRef bottomRight, ColorRef bottomLeft, float u0, float v0, float u1, float v1) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture.getId());
        var tesselator = Tesselator.getInstance();
        var builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        var matrix = poseStack.last().pose();
        final int z = 0;
        builder.vertex(matrix, rect.right(), rect.y(), z).uv(u1, v0).color(resolveColor(topRight)).endVertex();
        builder.vertex(matrix, rect.x(), rect.y(), z).uv(u0, v0).color(resolveColor(topLeft)).endVertex();
        builder.vertex(matrix, rect.x(), rect.bottom(), z).uv(u0, v1).color(resolveColor(bottomLeft)).endVertex();
        builder.vertex(matrix, rect.right(), rect.bottom(), z).uv(u1, v1).color(resolveColor(bottomRight)).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }

    @Override
    public void fillTriangle(Vec2 p1, Vec2 p2, Vec2 p3, ColorRef color) {
        var resolvedColor = resolveColor(color);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var tesselator = Tesselator.getInstance();
        var builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        var matrix = poseStack.last().pose();
        final int z = 0;
        builder.vertex(matrix, p1.x, p1.y, z).color(resolvedColor).endVertex();
        builder.vertex(matrix, p2.x, p2.y, z).color(resolvedColor).endVertex();
        builder.vertex(matrix, p3.x, p3.y, z).color(resolvedColor).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }

    @Override
    public void renderItem(ItemStack stack, int x, int y, int z, float width, float height) {
        var mc = Minecraft.getInstance();
        var itemRenderer = mc.getItemRenderer();

        poseStack.pushPose();
        poseStack.translate(x, y, z + 1);
        poseStack.scale(width / 16, height / 16, 1);
        itemRenderer.renderGuiItem(poseStack, stack, 0, 0);
        itemRenderer.renderGuiItemDecorations(poseStack, mc.font, stack, 0, 0);
        poseStack.popPose();
    }
}
