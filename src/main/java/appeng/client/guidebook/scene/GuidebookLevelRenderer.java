package appeng.client.guidebook.scene;

import appeng.client.guidebook.scene.level.GuidebookLevel;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Collection;

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
                       Collection<BlockHighlight> highlights) {
        lightmap.update(level);

        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

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

        var projectionMatrix = cameraSettings.getProjectionMatrix();
        var viewMatrix = cameraSettings.getViewMatrix();

        RenderSystem.setShaderFogColor(1, 1, 1, 0);
        RenderSystem.setShaderFogStart(0);
        RenderSystem.setShaderFogEnd(1000);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);

        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.mulPoseMatrix(viewMatrix);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix);

        var lightDirection = new Vector4f(15 / 90f, .35f, 1, 0);
        var lightTransform = new Matrix4f(viewMatrix);
        lightTransform.invert();
        lightTransform.transform(lightDirection);

        var transformedLightDirection = new Vector3f(lightDirection.x, lightDirection.y, lightDirection.z);
        RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection);

        var renderBuffers = Minecraft.getInstance().renderBuffers();

        var buffers = renderBuffers.bufferSource();

        renderBlocks(level, buffers);

        for (var highlight : highlights) {
            BlockHighlightRenderer.render(buffers, highlight.pos(), highlight.r(), highlight.g(), highlight.b(), highlight.a());
        }

        buffers.endLastBatch();

        // The order comes from LevelRenderer#renderLevel
        buffers.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        buffers.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        buffers.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        buffers.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));

        // These would normally be pre-baked, but they are not for us
        for (var layer : RenderType.chunkBufferLayers()) {
            buffers.endBatch(layer);
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
        buffers.endBatch();

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
    }

    private void renderBlocks(GuidebookLevel level, MultiBufferSource buffers) {
        var randomSource = level.random;
        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        var poseStack = new PoseStack();

        level.getFilledBlocks().forEach(pos -> {
            var blockState = level.getBlockState(pos);
            var fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                var renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                var bufferBuilder = buffers.getBuffer(renderType);

                var sectionPos = SectionPos.of(pos);
                var liquidVertexConsumer = new LiquidVertexConsumer(bufferBuilder, sectionPos);
                blockRenderDispatcher.renderLiquid(pos, level, liquidVertexConsumer, blockState, fluidState);

                markFluidSpritesActive(fluidState);
            }

            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                var renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
                var bufferBuilder = buffers.getBuffer(renderType);

                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderDispatcher.renderBatched(blockState, pos, level, poseStack, bufferBuilder, true, randomSource);
                poseStack.popPose();
            }

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
        var fluidVariant = FluidVariant.of(fluidState.getType());
        var sprites = FluidVariantRendering.getSprites(fluidVariant);
        for (var sprite : sprites) {
            SodiumCompat.markSpriteActive(sprite);
        }
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

            int packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());
            renderer.render(blockEntity, 0, stack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }

}
