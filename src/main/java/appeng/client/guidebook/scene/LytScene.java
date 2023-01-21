package appeng.client.guidebook.scene;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.document.interaction.TextTooltip;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.ColorRef;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Shows a pseudo-in-world scene within the guidebook.
 */
public class LytScene extends LytBlock implements InteractiveElement {
    private final GuidebookLevel level;
    private final GuidebookLightmap lightmap;

    private float scale = 1f;

    @Nullable
    private Matrix4f lastViewMatrix;
    @Nullable
    private Matrix4f lastProjectionMatrix;

    public LytScene() {
        level = new GuidebookLevel();
        lightmap = new GuidebookLightmap();
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
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

        var projectionMatrix = getProjectionMatrix(window, viewport.width(), viewport.height());
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        applyViewMatrix(modelViewStack);
        // Get the center and move the origin there
        var bounds = level.getBounds();
        var centerX = (bounds.max().getX() + bounds.min().getX()) / 2f;
        var centerY = (bounds.max().getY() + bounds.min().getY()) / 2f;
        var centerZ = (bounds.max().getZ() + bounds.min().getZ()) / 2f;
//        modelViewStack.mulPose(new Quaternionf().rotationY(
//                ((System.currentTimeMillis() % 6000) - 3000) / 3000f * Mth.PI
//        ));
        modelViewStack.translate(-centerX, -centerY, -centerZ);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix);
        var viewMatrix = modelViewStack.last().pose();

        var randomSource = level.random;
        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

        var renderBuffers = Minecraft.getInstance().renderBuffers();

        var poseStack = new PoseStack();
        var bufferSource = renderBuffers.bufferSource();
        level.getBlocks().forEach(pos -> {
            BlockState blockState = level.getBlockState(pos);
            if (blockState.isSolidRender(level, pos)) {
//                visGraph.setOpaque(pos);
            }

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

        final var lightDirection = new Vector4f(15 / 90f, .35f, 1, 0);
        final var lightTransform = new Matrix4f(viewMatrix);
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

        // The layer order comes from levelrenderer
        /*for (var renderType : new RenderType[]{RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout(), RenderType.translucent(), RenderType.tripwire()}) {
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
        }*/

        ModelBlockRenderer.clearCache();

        // Clear any unused buffers
        /*for (var renderType : renderTypes) {
            var builder = fixedBuffers.builder(renderType);
            if (builder.building()) {
                builder.endOrDiscardIfEmpty();
            }
        }*/

        RenderSystem.viewport(0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();

        var p = worldToScreen(viewMatrix, projectionMatrix, 0.5f, 0.5f, 0.5f);
        context.fillRect(
                Math.round(p.x), Math.round(p.y), 1, 1, ColorRef.WHITE
        );

        lastViewMatrix = viewMatrix;
        lastProjectionMatrix = projectionMatrix;

        var style = resolveStyle();
        var annotationWidth = context.getWidth("Inscriber", style);
        context.renderText("Inscriber", style, p.x, p.y - context.font().lineHeight / 2f);
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        if (lastViewMatrix == null || lastProjectionMatrix == null) {
            return Optional.empty();
        }

        var hitResult = pickBlock(lastViewMatrix, lastProjectionMatrix, x, y);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            var blockState = level.getBlockState(hitResult.getBlockPos());

            var text = Component.translatable(blockState.getBlock().getDescriptionId());
            return Optional.of(
                    new TextTooltip(text)
            );
        }

        return Optional.empty();
    }

    private Vector2f worldToScreen(Matrix4f viewMatrix, Matrix4f projectionMatrix, float x, float y, float z) {
        Vector3f screenPos = new Vector3f();
        viewMatrix.transformPosition(x, y, z, screenPos);
        projectionMatrix.transformProject(screenPos);
        var screenX = this.bounds.x() + (screenPos.x + 1) * this.bounds.width() / 2;
        var screenY = this.bounds.bottom() - (screenPos.y + 1) * this.bounds.height() / 2;
        return new Vector2f(screenX, screenY);
    }

    private BlockHitResult pickBlock(Matrix4f viewMatrix, Matrix4f projectionMatrix, float screenX, float screenY) {
        var rayOrigin = new Vector3f();
        var rayDir = new Vector3f();

        var viewProj = new Matrix4f(projectionMatrix);
        viewProj.mul(viewMatrix);
        viewProj.unprojectRay(
                screenX - bounds.x(), bounds.bottom() - screenY,
                new int[]{
                        0, 0,
                        bounds.width(), bounds.height()
                },
                rayOrigin,
                rayDir
        );

        var levelBounds = level.getBounds();
        var intersection = new Vector2f();
        if (!Intersectionf.intersectRayAab(
                rayOrigin,
                rayDir,
                new Vector3f(levelBounds.min().getX(), levelBounds.min().getY(), levelBounds.min().getZ()),
                new Vector3f(levelBounds.max().getX(), levelBounds.max().getY(), levelBounds.max().getZ()),
                intersection
        )) {
            return BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
        }

        // Move the ray such that the start and end are on the bounding box of the content
        var start = new Vector3f(rayDir).mulAdd(intersection.x, rayOrigin);
        var end = new Vector3f(rayDir).mulAdd(intersection.y, rayOrigin);

        return this.level.clip(
                new ClipContext(new Vec3(start), new Vec3(end), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, Minecraft.getInstance().player)
        );
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

    private void applyViewMatrix(PoseStack poseStack) {
        poseStack.scale(0.625f * 16 * scale, 0.625f * 16 * scale, 0.625f * 16 * scale);
//        poseStack.mulPose(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 30));
//        poseStack.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * 135));

        poseStack.mulPose(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 30));
        poseStack.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * 225));
    }

    @NotNull
    private static Matrix4f getProjectionMatrix(Window window, int width, int height) {
        var projectionMatrix = new Matrix4f();
        projectionMatrix.orthoSymmetric(width, height, -1000, 3000);
        return projectionMatrix;
    }

    public GuidebookLevel getLevel() {
        return level;
    }
}
