package appeng.client.guidebook.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import appeng.client.guidebook.color.ColorValue;
import appeng.client.guidebook.color.LightDarkMode;
import appeng.client.guidebook.document.LytRect;

public record SimpleRenderContext(
        @Override LytRect viewport,
        @Override GuiGraphics guiGraphics,
        @Override LightDarkMode lightDarkMode) implements RenderContext {

    public SimpleRenderContext(LytRect viewport, GuiGraphics guiGraphics) {
        this(viewport, guiGraphics, LightDarkMode.current());
    }

    @Override
    public int resolveColor(ColorValue ref) {
        return ref.resolve(lightDarkMode);
    }

    @Override
    public void fillRect(LytRect rect, ColorValue topLeft, ColorValue topRight, ColorValue bottomRight,
            ColorValue bottomLeft) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var tesselator = Tesselator.getInstance();
        var builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var matrix = poseStack().last().pose();
        final int z = 0;
        builder.vertex(matrix, rect.right(), rect.y(), z).color(resolveColor(topRight)).endVertex();
        builder.vertex(matrix, rect.x(), rect.y(), z).color(resolveColor(topLeft)).endVertex();
        builder.vertex(matrix, rect.x(), rect.bottom(), z).color(resolveColor(bottomLeft)).endVertex();
        builder.vertex(matrix, rect.right(), rect.bottom(), z).color(resolveColor(bottomRight)).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }

    @Override
    public void fillTexturedRect(LytRect rect, AbstractTexture texture, ColorValue topLeft, ColorValue topRight,
            ColorValue bottomRight, ColorValue bottomLeft, float u0, float v0, float u1, float v1) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture.getId());
        var tesselator = Tesselator.getInstance();
        var builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        var matrix = poseStack().last().pose();
        final int z = 0;
        builder.vertex(matrix, rect.right(), rect.y(), z).uv(u1, v0).color(resolveColor(topRight)).endVertex();
        builder.vertex(matrix, rect.x(), rect.y(), z).uv(u0, v0).color(resolveColor(topLeft)).endVertex();
        builder.vertex(matrix, rect.x(), rect.bottom(), z).uv(u0, v1).color(resolveColor(bottomLeft)).endVertex();
        builder.vertex(matrix, rect.right(), rect.bottom(), z).uv(u1, v1).color(resolveColor(bottomRight)).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
    }

    @Override
    public void fillTriangle(Vec2 p1, Vec2 p2, Vec2 p3, ColorValue color) {
        var resolvedColor = resolveColor(color);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var tesselator = Tesselator.getInstance();
        var builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        var matrix = poseStack().last().pose();
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

        var pose = poseStack();
        pose.pushPose();
        pose.translate(x, y, z + 1);
        pose.scale(width / 16, height / 16, 1);
        guiGraphics().renderItem(stack, 0, 0);
        guiGraphics().renderItemDecorations(mc.font, stack, 0, 0);
        pose.popPose();
    }
}
