package appeng.client.guidebook.scene;

import appeng.client.guidebook.scene.level.GuidebookLevel;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
                       CameraSettings cameraSettings) {
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

        var randomSource = level.random;
        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

        var renderBuffers = Minecraft.getInstance().renderBuffers();

        var poseStack = new PoseStack();
        var bufferSource = renderBuffers.bufferSource();
        level.getFilledBlocks().forEach(pos -> {
            var blockState = level.getBlockState(pos);
            var fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                var renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                var bufferBuilder = bufferSource.getBuffer(renderType);

                blockRenderDispatcher.renderLiquid(pos, level, bufferBuilder, blockState, fluidState);
            }

            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                var renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
                var bufferBuilder = bufferSource.getBuffer(renderType);

                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderDispatcher.renderBatched(blockState, pos, level, poseStack, bufferBuilder, true, randomSource);
                poseStack.popPose();
            }

            if (blockState.hasBlockEntity()) {
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    this.handleBlockEntity(poseStack, blockEntity, bufferSource);
                }
            }
        });

        var lightDirection = new Vector4f(15 / 90f, .35f, 1, 0);
        var lightTransform = new Matrix4f(viewMatrix);
        lightTransform.invert();
        lightTransform.transform(lightDirection);

        var transformedLightDirection = new Vector3f(lightDirection.x, lightDirection.y, lightDirection.z);
        RenderSystem.setShaderLights(transformedLightDirection, transformedLightDirection);

        bufferSource.endLastBatch();

        // The order comes from LevelRenderer#renderLevel
        bufferSource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        bufferSource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));

        // These would normally be pre-baked, but they are not for us
        for (var layer : RenderType.chunkBufferLayers()) {
            bufferSource.endBatch(layer);
        }

        bufferSource.endBatch(RenderType.solid());
        bufferSource.endBatch(RenderType.endPortal());
        bufferSource.endBatch(RenderType.endGateway());
        bufferSource.endBatch(Sheets.solidBlockSheet());
        bufferSource.endBatch(Sheets.cutoutBlockSheet());
        bufferSource.endBatch(Sheets.bedSheet());
        bufferSource.endBatch(Sheets.shulkerBoxSheet());
        bufferSource.endBatch(Sheets.signSheet());
        bufferSource.endBatch(Sheets.hangingSignSheet());
        bufferSource.endBatch(Sheets.chestSheet());
        bufferSource.endBatch();

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
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
