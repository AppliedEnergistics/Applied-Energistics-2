package appeng.client.guidebook.render;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.screen.GuideScreen;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public record SimpleRenderContext(@Override GuideScreen screen,
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
        RenderSystem.disableTexture();
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
        RenderSystem.enableTexture();
    }

    @Override
    public void fillTriangle(Vec2 p1, Vec2 p2, Vec2 p3, ColorRef color) {
        var resolvedColor = resolveColor(color);

        RenderSystem.disableTexture();
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
        RenderSystem.enableTexture();
    }

    @Override
    public void renderItem(ItemStack stack, int x, int y, int z, float width, float height) {
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var textureManager = Minecraft.getInstance().getTextureManager();

        var model = itemRenderer.getModel(stack, null, null, 0);

        // Essentially the same code as in itemrenderer renderInGui, but we're passing our own posestack
        textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.translate(width / 2, height /2, 0.0);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(width, height, 1f);
        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean flatLighting = !model.usesBlockLight();
        if (flatLighting) {
            Lighting.setupForFlatItems();
        }

        itemRenderer.render(stack,
                ItemTransforms.TransformType.GUI,
                false,
                poseStack,
                buffers,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                model
        );
        buffers.endBatch();
        RenderSystem.enableDepthTest();
        if (flatLighting) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
    }

    private enum GradientDirection {
        HORIZONTAL,
        VERTICAL
    }
}