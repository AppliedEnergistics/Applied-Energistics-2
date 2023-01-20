package appeng.client.guidebook.scene;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Shows a pseudo-in-world scene within the guidebook.
 */
public class LytScene extends LytBlock {
    private final GuidebookLevel level;
    private final GuidebookLightmap lightmap;

    public LytScene() {
        level = new GuidebookLevel();
        lightmap = new GuidebookLightmap();
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        var bounds = level.getBounds();

        var stack = new PoseStack();
        applyViewMatrix(stack);

        var screenPoints = new ArrayList<Vector3f>();
        BoundingBox.fromCorners(bounds.min(), bounds.max()).forAllCorners(cornerPos -> {
            var corner = new Vector3f();
            corner.set(cornerPos.getX(), cornerPos.getY(), cornerPos.getZ());
            stack.last().pose().transformPosition(corner);
            screenPoints.add(corner);
        });

        var vMin = new Vector3f(screenPoints.get(0));
        var vMax = new Vector3f(screenPoints.get(0));
        for (int i = 1; i < screenPoints.size(); i++) {
            vMin.min(screenPoints.get(i));
            vMax.max(screenPoints.get(i));
        }

        var width = (int) Math.ceil(Math.abs(vMax.x - vMin.x));
        var height = (int) Math.ceil(Math.abs(vMax.y - vMin.y));

        return new LytRect(x, y, width, height);
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
    }

    @Override
    public void render(RenderContext context) {

        lightmap.update(level);

        var chunkPos = new ChunkPos(BlockPos.ZERO);
        var lightEngine = level.getLightEngine();
        lightEngine.retainData(chunkPos, false);
        lightEngine.enableLightSources(chunkPos, false);

        for (int i = lightEngine.getMinLightSection(); i < lightEngine.getMaxLightSection(); ++i) {
            lightEngine.queueSectionData(LightLayer.BLOCK, SectionPos.of(chunkPos, i), null, true);
            lightEngine.queueSectionData(LightLayer.SKY, SectionPos.of(chunkPos, i), null, true);
        }
        lightEngine.enableLightSources(chunkPos, true);

        level.getLightEngine().updateSectionStatus(BlockPos.ZERO, false);
        level.getLightEngine().runUpdates(Integer.MAX_VALUE, true, true);

        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();

        var viewport = bounds.transform(context.poseStack().last().pose());

        // Render into the bounds of our little box...
        RenderSystem.viewport(
                (int) (viewport.x() * window.getGuiScale()),
                (int) (window.getHeight() - viewport.bottom() * window.getGuiScale()),
                (int) (viewport.width() * window.getGuiScale()),
                (int) (viewport.height() * window.getGuiScale())
        );

        RenderSystem.setShaderFogColor(1, 1, 1, 0);
        RenderSystem.setShaderFogStart(0);
        RenderSystem.setShaderFogEnd(1000);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);

        var randomSource = RandomSource.create();
        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

        var renderBuffers = Minecraft.getInstance().renderBuffers();
        var renderTypes = new HashSet<RenderType>();

        var poseStack = new PoseStack();
        level.getBlocks().forEach(pos -> {
            BlockState blockState = level.getBlockState(pos);
            if (blockState.isSolidRender(level, pos)) {
//                visGraph.setOpaque(pos);
            }

            var fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                var renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                var bufferBuilder = renderBuffers.bufferSource().getBuffer(renderType);

                blockRenderDispatcher.renderLiquid(pos, level, bufferBuilder, blockState, fluidState);
            }

            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                var renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
                var bufferBuilder = renderBuffers.bufferSource().getBuffer(renderType);

                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderDispatcher.renderBatched(blockState, pos, level, poseStack, bufferBuilder, true, randomSource);
                poseStack.popPose();
            }

            if (blockState.hasBlockEntity()) {
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    this.handleBlockEntity(poseStack, blockEntity, renderBuffers.bufferSource());
                }
            }
        });

        var projectionMatrix = getProjectionMatrix(window, viewport.width(), viewport.height());

        applyViewMatrix(poseStack);
        // Get the center and move the origin there
        var bounds = level.getBounds();
        var centerX = (bounds.max().getX() + bounds.min().getX()) / 2f;
        var centerY = (bounds.max().getY() + bounds.min().getY()) / 2f;
        var centerZ = (bounds.max().getZ() + bounds.min().getZ()) / 2f;
        //poseStack.mulPose(new Quaternionf().rotationY(
        //         ((System.currentTimeMillis() % 2000) - 1000) / 1000f * Mth.PI
        //));
        poseStack.translate(-centerX, -centerY, -centerZ);

        var viewMatrix = poseStack.last().pose();

        final var lightDirection = new Vector4f(15 / 90f, .35f, 1, 0);
        final var lightTransform = new Matrix4f(viewMatrix);
        lightTransform.invert();
        lightTransform.transform(lightDirection);

        var transformedLightDirection = new Vector3f(lightDirection.x, lightDirection.y, lightDirection.z);
        RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection);

        // The layer order comes from levelrenderer
        for (var renderType : new RenderType[]{RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout(), RenderType.translucent(), RenderType.tripwire()}) {
            if (!renderTypes.contains(renderType)) {
                continue;
            }

            renderBuffers.bufferSource().endLastBatch();

            BufferBuilder.RenderedBuffer renderedBuffer = fixedBuffers.builder(renderType).endOrDiscardIfEmpty();
            if (renderedBuffer != null) {

                // TODO DRAW
//                compileResults.renderedLayers.put(renderType2, renderedBuffer);
                renderChunkLayer(renderType, renderedBuffer, projectionMatrix, viewMatrix);
            }
        }

        ModelBlockRenderer.clearCache();

        // Clear any unused buffers
        for (var renderType : renderTypes) {
            var builder = fixedBuffers.builder(renderType);
            if (builder.building()) {
                builder.endOrDiscardIfEmpty();
            }
        }

        RenderSystem.viewport(0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
    }

    private <E extends BlockEntity> void handleBlockEntity(PoseStack stack,
                                                           E blockEntity,
                                                           MultiBufferSource buffers) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        var renderer = dispatcher.getRenderer(blockEntity);
        if (renderer != null) {
            var pos = blockEntity.getBlockPos();
            stack.pushPose();
            stack.translate(pos.getX(), pos.getY(), pos.getZ());

            int packedLight = LevelRenderer.getLightColor(level, blockEntity.getBlockPos());
            renderer.render(blockEntity, 0, stack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }

    private static void applyViewMatrix(PoseStack poseStack) {
        poseStack.scale(16, 16, 16);
        poseStack.mulPose(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 30));
        poseStack.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * 135));
    }

    private LytRect getScreenViewport(Window window, LytRect documentViewport) {
        // Transform into window space
        var viewportLeft = (int) Math.round(bounds.x() * window.getGuiScale());
        var viewportTop = (int) Math.round((bounds.y() - documentViewport.y()) * window.getGuiScale());
        var viewportWidth = (int) Math.round(bounds.width() * window.getGuiScale());
        var viewportHeight = (int) Math.round(bounds.height() * window.getGuiScale());
        return new LytRect(viewportLeft, viewportTop, viewportWidth, viewportHeight);
    }

    @NotNull
    private static Matrix4f getProjectionMatrix(Window window, int width, int height) {
        var projectionMatrix = new Matrix4f();
        projectionMatrix.ortho(-width / 2f, width / 2f, -height / 2f, height / 2f, -1000, 3000);
        return projectionMatrix;
    }

    public GuidebookLevel getLevel() {
        return level;
    }

    /**
     * Based on {@link LevelRenderer} (renderChunkLayer)
     */
    private void renderChunkLayer(RenderType renderType, BufferBuilder.RenderedBuffer renderedBuffer, Matrix4f projection, Matrix4f modelViewMatrix) {
        RenderSystem.assertOnRenderThread();
        renderType.setupRenderState();

        lightmap.bind();

        var shaderInstance = RenderSystem.getShader();

        for (int k = 0; k < 12; ++k) {
            int l = RenderSystem.getShaderTexture(k);
            shaderInstance.setSampler("Sampler" + k, l);
        }

        if (shaderInstance.MODEL_VIEW_MATRIX != null) {
            shaderInstance.MODEL_VIEW_MATRIX.set(modelViewMatrix);
        }

        if (shaderInstance.PROJECTION_MATRIX != null) {
            shaderInstance.PROJECTION_MATRIX.set(projection);
        }

        if (shaderInstance.COLOR_MODULATOR != null) {
            shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (shaderInstance.FOG_START != null) {
            shaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (shaderInstance.FOG_END != null) {
            shaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (shaderInstance.FOG_COLOR != null) {
            shaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (shaderInstance.FOG_SHAPE != null) {
            shaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (shaderInstance.TEXTURE_MATRIX != null) {
            shaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (shaderInstance.GAME_TIME != null) {
            shaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        RenderSystem.setupShaderLights(shaderInstance);
        shaderInstance.apply();

        try (var vertexBuffer = new VertexBuffer()) {
            vertexBuffer.bind();
            vertexBuffer.upload(renderedBuffer);
            vertexBuffer.draw();
        }

        shaderInstance.clear();
        VertexBuffer.unbind();
        renderType.clearRenderState();
    }
}
