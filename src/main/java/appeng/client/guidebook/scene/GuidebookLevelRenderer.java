package appeng.client.guidebook.scene;

import java.util.Collection;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;

import appeng.client.guidebook.scene.annotation.InWorldAnnotation;
import appeng.client.guidebook.scene.annotation.InWorldAnnotationRenderer;
import appeng.client.guidebook.scene.level.GuidebookLevel;

public class GuidebookLevelRenderer {

    private static GuidebookLevelRenderer instance;

    private final GuidebookLightmap lightmap = new GuidebookLightmap();

    public static GuidebookLevelRenderer getInstance() {
        RenderSystem.assertOnRenderThread();
        if (instance == null) {
            instance = new GuidebookLevelRenderer();
        }
        return instance;
    }

    public void render(GuidebookLevel level,
            CameraSettings cameraSettings,
            Collection<InWorldAnnotation> annotations) {
        lightmap.update(level);

        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.setShaderGameTime(System.currentTimeMillis(), 0);

        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        render(level, cameraSettings, buffers, annotations);
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
    }

    public void render(GuidebookLevel level,
            CameraSettings cameraSettings,
            MultiBufferSource.BufferSource buffers,
            Collection<InWorldAnnotation> annotations) {
        lightmap.update(level);

        var lightEngine = level.getLightEngine();
        while (lightEngine.hasLightWork()) {
            lightEngine.runLightUpdates();
        }

        var projectionMatrix = cameraSettings.getProjectionMatrix();
        var viewMatrix = cameraSettings.getViewMatrix();

        // Essentially disable level fog
        RenderSystem.setShaderFogColor(1, 1, 1, 0);
        RenderSystem.setShaderFogStart(0);
        RenderSystem.setShaderFogEnd(1000);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();
        modelViewStack.mul(viewMatrix);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);

        var lightDirection = new Vector4f(15 / 90f, .35f, 1, 0);
        var lightTransform = new Matrix4f(viewMatrix);
        lightTransform.invert();
        lightTransform.transform(lightDirection);

        var transformedLightDirection = new Vector3f(lightDirection.x, lightDirection.y, lightDirection.z);
        RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection);

        renderContent(level, buffers);

        InWorldAnnotationRenderer.render(buffers, annotations);

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();

        Lighting.setupFor3DItems(); // Reset to GUI lighting
    }

    /**
     * Render without any setup.
     */
    public void renderContent(GuidebookLevel level, MultiBufferSource.BufferSource buffers) {
        RenderSystem.runAsFancy(() -> {
            renderBlocks(level, buffers, false);
            renderBlockEntities(level, buffers);

            // The order comes from LevelRenderer#renderLevel
            buffers.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
            buffers.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
            buffers.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
            buffers.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));

            // These would normally be pre-baked, but they are not for us
            for (var layer : RenderType.chunkBufferLayers()) {
                if (layer != RenderType.translucent()) {
                    buffers.endBatch(layer);
                }
            }

            buffers.endBatch(RenderType.solid());
            buffers.endBatch(RenderType.endPortal());
            buffers.endBatch(RenderType.endGateway());
            buffers.endBatch(Sheets.solidBlockSheet());
            buffers.endBatch(Sheets.cutoutBlockSheet());
            buffers.endBatch(Sheets.bedSheet());
            buffers.endBatch(Sheets.shulkerBoxSheet());
            buffers.endBatch(Sheets.signSheet());
            buffers.endBatch(Sheets.hangingSignSheet());
            buffers.endBatch(Sheets.chestSheet());
            buffers.endLastBatch();

            renderBlocks(level, buffers, true);
            buffers.endBatch(RenderType.translucent());
        });
    }

    private void renderBlocks(GuidebookLevel level, MultiBufferSource buffers, boolean translucent) {
        var randomSource = level.random;
        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        var poseStack = new PoseStack();

        level.getFilledBlocks().forEach(pos -> {
            var blockState = level.getBlockState(pos);
            var fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                var renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                if (renderType != RenderType.translucent() || translucent) {
                    var bufferBuilder = buffers.getBuffer(renderType);

                    var sectionPos = SectionPos.of(pos);
                    var liquidVertexConsumer = new LiquidVertexConsumer(bufferBuilder, sectionPos);
                    blockRenderDispatcher.renderLiquid(pos, level, liquidVertexConsumer, blockState, fluidState);

                    markFluidSpritesActive(fluidState);
                }
            }

            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                var be = level.getBlockEntity(pos);
                ModelData modelData = ModelData.EMPTY;
                if (be != null) {
                    modelData = be.getModelData();
                }

                var model = blockRenderDispatcher.getBlockModel(blockState);
                modelData = model.getModelData(level, pos, blockState, modelData);
                var renderTypes = model.getRenderTypes(blockState, randomSource, modelData);

                for (var renderType : renderTypes) {
                    if (renderType != RenderType.translucent() || translucent) {
                        var bufferBuilder = buffers.getBuffer(renderType);

                        poseStack.pushPose();
                        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                        blockRenderDispatcher.renderBatched(blockState, pos, level, poseStack, bufferBuilder, true,
                                randomSource, modelData, renderType);
                        poseStack.popPose();
                    }
                }
            }
        });
    }

    private void renderBlockEntities(GuidebookLevel level, MultiBufferSource buffers) {
        var poseStack = new PoseStack();

        level.getFilledBlocks().forEach(pos -> {
            var blockState = level.getBlockState(pos);
            if (blockState.hasBlockEntity()) {
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    this.handleBlockEntity(poseStack, blockEntity, buffers);
                }
            }
        });
    }

    private static void markFluidSpritesActive(FluidState fluidState) {
        // For Sodium compatibility, ensure the sprites actually animate even if no block is on-screen
        // that would cause them to, otherwise.
        var props = IClientFluidTypeExtensions.of(fluidState);
        var sprite1 = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(props.getStillTexture());
        SodiumCompat.markSpriteActive(sprite1);
        var sprite2 = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(props.getFlowingTexture());
        SodiumCompat.markSpriteActive(sprite2);
    }

    private <E extends BlockEntity> void handleBlockEntity(PoseStack stack,
            E blockEntity,
            MultiBufferSource buffers) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        var renderer = dispatcher.getRenderer(blockEntity);
        if (renderer != null && renderer.shouldRender(blockEntity, blockEntity.getBlockPos().getCenter())) {
            var pos = blockEntity.getBlockPos();
            stack.pushPose();
            stack.translate(pos.getX(), pos.getY(), pos.getZ());

            int packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());
            renderer.render(blockEntity, 0, stack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }

}
