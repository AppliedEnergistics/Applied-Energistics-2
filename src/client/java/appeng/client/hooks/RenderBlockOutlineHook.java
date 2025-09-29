package appeng.client.hooks;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
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
                    evt.addCustomRenderer(new PartPlacementPreviewRenderer(placement, part));
                }
            }
        }

        // Hit test against all attached parts to highlight the part that is relevant
        var pos = evt.getBlockPos();
        if (evt.getLevel().getBlockEntity(pos) instanceof IPartHost partHost) {

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
                        evt.addCustomRenderer(new FacadePlacementPreviewRenderer(side, facade, renderAnchor));
                    }
                }
            }

            var selectedPart = partHost.selectPartWorld(evt.getHitResult().getLocation());
            if (selectedPart.facade != null) {
                evt.addCustomRenderer(new FacadeOutlineRenderer());
                return;
            }
            if (selectedPart.part != null) {
                evt.addCustomRenderer(new PartOutlineRenderer());
                return;
            }
        }
    }

    record PartPlacementPreviewRenderer(PartPlacement.Placement placement,
            IPart part) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState blockOutlineRenderState,
                MultiBufferSource.BufferSource bufferSource,
                PoseStack poseStack,
                boolean translucentPass,
                LevelRenderState levelRenderState) {
            // Render without depth test to also have a preview for parts inside blocks.
            // TODO 1.21.5 renderPart(poseStack, bufferSource, camera, placement.pos(), part, placement.side(), true,
            // true);
            // TODO 1.21.5 renderPart(poseStack, bufferSource, camera, placement.pos(), part, placement.side(), true,
            // false);
            return false;
        }
    }

    record FacadePlacementPreviewRenderer(Direction side, IFacadePart facade,
            boolean renderAnchor) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState blockOutlineRenderState,
                MultiBufferSource.BufferSource bufferSource,
                PoseStack poseStack,
                boolean b,
                LevelRenderState levelRenderState) {
            var pos = blockOutlineRenderState.pos();

            // Use same rendering inside blocks as part preview.
            showFacadePlacementPreview(poseStack, pos, bufferSource, true);
            showFacadePlacementPreview(poseStack, pos, bufferSource, false);
            return false;
        }

        private void showFacadePlacementPreview(PoseStack poseStack,
                BlockPos pos,
                MultiBufferSource buffers,
                boolean insideBlock) {
            if (renderAnchor) {
                var cableAnchor = AEParts.CABLE_ANCHOR.get().createPart();
                renderPart(poseStack, buffers, null, pos, cableAnchor, side, true, insideBlock);
            }

            renderFacade(poseStack, buffers, null, pos, facade, side, true, insideBlock);
        }
    }

    static class FacadeOutlineRenderer implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState blockOutlineRenderState,
                MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean b,
                LevelRenderState levelRenderState) {
            // TODO 1.21.9 renderFacade(poseStack, buffers, camera, pos, selectedPart.facade, selectedPart.side, false,
            // false);
            return false;
        }
    }

    static class PartOutlineRenderer implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState blockOutlineRenderState,
                MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean b,
                LevelRenderState levelRenderState) {
            // TODO 1.21.9 renderPart(poseStack, buffers, camera, pos, selectedPart.part, selectedPart.side, false,
            // false);
            return false;
        }
    }

    private static void renderPart(PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockPos pos,
            IPart part,
            Direction side,
            boolean preview,
            boolean insideBlock) {
        var boxes = new ArrayList<AABB>();
        var helper = new BusCollisionHelper(boxes, side, true);
        part.getBoxes(helper);
        renderBoxes(poseStack, buffers, camera, pos, boxes, preview, insideBlock);
    }

    private static void renderFacade(PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockPos pos,
            IFacadePart facade,
            Direction side,
            boolean preview,
            boolean insideBlock) {
        var boxes = new ArrayList<AABB>();
        var helper = new BusCollisionHelper(boxes, side, true);
        facade.getBoxes(helper, false);
        renderBoxes(poseStack, buffers, camera, pos, boxes, preview, insideBlock);
    }

    private static void renderBoxes(PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockPos pos,
            List<AABB> boxes,
            boolean preview,
            boolean insideBlock) {
        RenderType renderType = insideBlock ? AERenderTypes.LINES_BEHIND_BLOCK : RenderType.lines();
        var buffer = buffers.getBuffer(renderType);
        float alpha = insideBlock ? 0.2f : preview ? 0.6f : 0.4f;

        for (var box : boxes) {
            var shape = Shapes.create(box);

            ShapeRenderer.renderShape(
                    poseStack,
                    buffer,
                    shape,
                    pos.getX() - camera.getPosition().x,
                    pos.getY() - camera.getPosition().y,
                    pos.getZ() - camera.getPosition().z,
                    ARGB.colorFromFloat(alpha,
                            preview ? 1 : 0,
                            preview ? 1 : 0,
                            preview ? 1 : 0));
        }
    }
}
