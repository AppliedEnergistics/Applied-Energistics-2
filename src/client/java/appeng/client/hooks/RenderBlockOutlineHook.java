package appeng.client.hooks;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.common.NeoForge;

import appeng.api.implementations.items.IFacadeItem;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.client.render.AERenderTypes;
import appeng.core.AEConfig;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import appeng.parts.BusCollisionHelper;
import appeng.parts.PartPlacement;

public class RenderBlockOutlineHook {
    private static final float PREVIEW_LINE_WIDTH = 7F;
    private static final float HIGH_CONTRAST_SECONDARY_LINE_WIDTH = 7F;

    private RenderBlockOutlineHook() {
    }

    public static void install() {
        NeoForge.EVENT_BUS.addListener(RenderBlockOutlineHook::handleEvent);
    }

    /*
     * Changes block outline rendering such that it renders only for individual parts, not for the entire part host.
     */
    private static void handleEvent(ExtractBlockOutlineRenderStateEvent evt) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        var itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        var blockHitResult = evt.getHitResult();

        if (AEConfig.instance().isPlacementPreviewEnabled()) {
            if (!itemInHand.isEmpty() && itemInHand.getItem() instanceof IPartItem<?> partItem) {
                var part = partItem.createPart();
                var placement = PartPlacement.getPartPlacement(player,
                        player.level(),
                        itemInHand,
                        evt.getBlockPos(),
                        blockHitResult.getDirection(),
                        blockHitResult.getLocation());
                if (placement != null) {
                    var cameraRelativePos = new Vec3(
                            placement.pos().getX() - evt.getCamera().position().x,
                            placement.pos().getY() - evt.getCamera().position().y,
                            placement.pos().getZ() - evt.getCamera().position().z);
                    evt.addCustomRenderer(new PartPlacementPreviewRenderer(placement, part, cameraRelativePos));
                }
            }
        }

        // Hit test against all attached parts to highlight the part that is relevant
        var pos = evt.getBlockPos();
        if (evt.getLevel().getBlockEntity(pos) instanceof IPartHost partHost) {
            var cameraRelativePos = new Vec3(
                    evt.getBlockPos().getX() - evt.getCamera().position().x,
                    evt.getBlockPos().getY() - evt.getCamera().position().y,
                    evt.getBlockPos().getZ() - evt.getCamera().position().z);

            // Rendering a preview of what is currently in hand has priority
            // If the item in hand is a facade and a block is hit, attempt facade placement
            if (AEConfig.instance().isPlacementPreviewEnabled()) {
                if (itemInHand.getItem() instanceof IFacadeItem facadeItem) {
                    var side = blockHitResult.getDirection();
                    var facade = facadeItem.createPartFromItemStack(itemInHand, side);
                    if (facade != null && FacadeItem.canPlaceFacade(partHost, facade)) {
                        // Maybe a bit hacky, but if there's no part on the side to support the facade
                        // We would render a cable anchor implicitly
                        boolean renderAnchor = partHost.getPart(side) == null;
                        evt.addCustomRenderer(
                                new FacadePlacementPreviewRenderer(side, facade, renderAnchor, cameraRelativePos));
                    }
                }
            }

            var selectedPart = partHost.selectPartWorld(evt.getHitResult().getLocation());
            boolean highContrast = evt.isHighContrast();
            // GameRenderer#getGameRenderState() -> GameRenderer#gameRenderState()
            float lineWidth = Minecraft.getInstance().gameRenderer
                    .gameRenderState().windowRenderState.appropriateLineWidth;
            if (selectedPart.facade != null) {
                evt.addCustomRenderer(
                        new FacadeOutlineRenderer(selectedPart.facade, selectedPart.side, cameraRelativePos,
                                highContrast, lineWidth));
                return;
            }
            if (selectedPart.part != null) {
                evt.addCustomRenderer(new PartOutlineRenderer(selectedPart.part, selectedPart.side, cameraRelativePos,
                        highContrast, lineWidth));
                return;
            }
        }
    }

    /*
     * CustomBlockOutlineRenderer#render lost its MultiBufferSource.BufferSource and its `translucentPass` boolean and
     * instead receives the SubmitNodeCollector: render(BlockOutlineRenderState, SubmitNodeCollector, PoseStack,
     * LevelRenderState). It is now invoked exactly once per frame (LevelRenderer#submitBlockOutline) rather than once
     * per pass; which pass the geometry ends up in is decided by SubmitNodeCollection#submitShapeOutline from the
     * colour's alpha and the `afterTerrain` flag, which vanilla feeds from BlockOutlineRenderState#isTranslucent().
     */
    record PartPlacementPreviewRenderer(PartPlacement.Placement placement,
            IPart part, Vec3 cameraRelativePos) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState renderState,
                SubmitNodeCollector collector,
                PoseStack poseStack,
                LevelRenderState levelRenderState) {
            // Render without depth test to also have a preview for parts inside blocks.
            renderPart(poseStack, collector, cameraRelativePos, part, placement.side(), PREVIEW_LINE_WIDTH, false,
                    true, true, renderState.isTranslucent());
            renderPart(poseStack, collector, cameraRelativePos, part, placement.side(), PREVIEW_LINE_WIDTH, false,
                    true, false, renderState.isTranslucent());
            return false;
        }
    }

    record FacadePlacementPreviewRenderer(Direction side, IFacadePart facade,
            boolean renderAnchor,
            Vec3 cameraRelativePos) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState renderState,
                SubmitNodeCollector collector,
                PoseStack poseStack,
                LevelRenderState levelRenderState) {
            // Use same rendering inside blocks as part preview.
            showFacadePlacementPreview(poseStack, cameraRelativePos, collector, true, renderState.isTranslucent());
            showFacadePlacementPreview(poseStack, cameraRelativePos, collector, false, renderState.isTranslucent());
            return false;
        }

        private void showFacadePlacementPreview(PoseStack poseStack,
                Vec3 cameraRelativePos,
                SubmitNodeCollector collector,
                boolean insideBlock,
                boolean afterTerrain) {
            if (renderAnchor) {
                var cableAnchor = AEParts.CABLE_ANCHOR.get().createPart();
                renderPart(poseStack, collector, cameraRelativePos, cableAnchor, side, PREVIEW_LINE_WIDTH, false, true,
                        insideBlock, afterTerrain);
            }

            renderFacade(poseStack, collector, cameraRelativePos, facade, side, PREVIEW_LINE_WIDTH, false, true,
                    insideBlock, afterTerrain);
        }
    }

    record FacadeOutlineRenderer(IFacadePart facade, Direction side,
            Vec3 cameraRelativePos,
            boolean highContrast,
            float lineWidth) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState renderState,
                SubmitNodeCollector collector, PoseStack poseStack,
                LevelRenderState levelRenderState) {
            renderFacade(poseStack, collector, cameraRelativePos, facade, side, lineWidth, highContrast, false,
                    false, renderState.isTranslucent());

            return true;
        }
    }

    record PartOutlineRenderer(IPart part, Direction side,
            Vec3 cameraRelativePos,
            boolean highContrast,
            float lineWidth) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState renderState,
                SubmitNodeCollector collector, PoseStack poseStack,
                LevelRenderState levelRenderState) {
            renderPart(poseStack, collector, cameraRelativePos, part, side, lineWidth, highContrast, false, false,
                    renderState.isTranslucent());
            return true;
        }
    }

    private static void renderPart(PoseStack poseStack,
            SubmitNodeCollector collector,
            Vec3 cameraRelativePos,
            IPart part,
            Direction side,
            float lineWidth,
            boolean highContrast,
            boolean preview,
            boolean insideBlock,
            boolean afterTerrain) {
        var boxes = new ArrayList<AABB>();
        var helper = new BusCollisionHelper(boxes, side, true);
        part.getBoxes(helper);
        renderBoxes(poseStack, collector, cameraRelativePos, boxes, lineWidth, highContrast, preview, insideBlock,
                afterTerrain);
    }

    private static void renderFacade(PoseStack poseStack,
            SubmitNodeCollector collector,
            Vec3 cameraRelativePos,
            IFacadePart facade,
            Direction side,
            float lineWidth,
            boolean highContrast,
            boolean preview,
            boolean insideBlock,
            boolean afterTerrain) {
        var boxes = new ArrayList<AABB>();
        var helper = new BusCollisionHelper(boxes, side, true);
        facade.getBoxes(helper, false);
        renderBoxes(poseStack, collector, cameraRelativePos, boxes, lineWidth, highContrast, preview, insideBlock,
                afterTerrain);
    }

    private static void renderBoxes(PoseStack poseStack,
            SubmitNodeCollector collector,
            Vec3 cameraRelativePos,
            List<AABB> boxes,
            float lineWidth,
            boolean highContrast,
            boolean preview,
            boolean insideBlock,
            boolean afterTerrain) {
        if (preview) {
            RenderType renderType = insideBlock ? AERenderTypes.LINES_BEHIND_BLOCK : RenderTypes.linesTranslucent();
            int color = ARGB.white(insideBlock ? 0.2f : 0.6f);
            renderBoxes(poseStack, collector, cameraRelativePos, boxes, renderType, color, lineWidth, afterTerrain);
        } else {
            if (highContrast) {
                renderBoxes(poseStack, collector, cameraRelativePos, boxes, RenderTypes.secondaryBlockOutline(),
                        CommonColors.BLACK, HIGH_CONTRAST_SECONDARY_LINE_WIDTH, afterTerrain);
            }
            // 26.3 split the old blended RenderPipelines.LINES into LINES (ColorTargetState.DEFAULT,
            // i.e. NO blending) and LINES_TRANSLUCENT / LINES_DEPTH_BIAS (BlendFunction.TRANSLUCENT). All the
            // outline colours here carry an alpha < 255, so keeping RenderTypes.lines() would silently render
            // them fully opaque. Vanilla's own LevelRenderer#submitBlockOutline made exactly this switch:
            // linesDepthBias() for the high-contrast outline, linesTranslucent() otherwise.
            int color = highContrast ? CommonColors.HIGH_CONTRAST_DIAMOND : ARGB.black(0.4f);
            RenderType renderType = highContrast ? RenderTypes.linesDepthBias() : RenderTypes.linesTranslucent();
            renderBoxes(poseStack, collector, cameraRelativePos, boxes, renderType, color, lineWidth, afterTerrain);
        }
    }

    private static void renderBoxes(PoseStack poseStack,
            SubmitNodeCollector collector,
            Vec3 cameraRelativePos,
            List<AABB> boxes,
            RenderType renderType,
            int color,
            float lineWidth,
            boolean afterTerrain) {
        // ShapeRenderer#renderShape(poseStack, buffer, shape, x, y, z, color, lineWidth) is gone.
        // Its replacement, SubmitNodeCollector#submitShapeOutline, has no x/y/z offset, so the camera-relative
        // offset that used to be passed per call now has to be baked into the pose - exactly like vanilla's
        // LevelRenderer#submitBlockOutline does before calling submitHitOutline.
        poseStack.pushPose();
        poseStack.translate(cameraRelativePos.x, cameraRelativePos.y, cameraRelativePos.z);

        for (var box : boxes) {
            var shape = Shapes.create(box);

            collector.submitShapeOutline(
                    poseStack,
                    shape,
                    renderType,
                    color,
                    lineWidth,
                    afterTerrain);
        }

        poseStack.popPose();
    }
}
