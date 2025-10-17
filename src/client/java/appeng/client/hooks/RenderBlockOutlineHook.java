package appeng.client.hooks;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
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
            if (selectedPart.facade != null) {
                evt.addCustomRenderer(
                        new FacadeOutlineRenderer(selectedPart.facade, selectedPart.side, cameraRelativePos));
                return;
            }
            if (selectedPart.part != null) {
                evt.addCustomRenderer(new PartOutlineRenderer(selectedPart.part, selectedPart.side, cameraRelativePos));
                return;
            }
        }
    }

    record PartPlacementPreviewRenderer(PartPlacement.Placement placement,
            IPart part, Vec3 cameraRelativePos) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState blockOutlineRenderState,
                MultiBufferSource.BufferSource bufferSource,
                PoseStack poseStack,
                boolean translucentPass,
                LevelRenderState levelRenderState) {
            // Render without depth test to also have a preview for parts inside blocks.
            renderPart(poseStack, bufferSource, cameraRelativePos, part, placement.side(), true, true);
            renderPart(poseStack, bufferSource, cameraRelativePos, part, placement.side(), true, false);
            return false;
        }
    }

    record FacadePlacementPreviewRenderer(Direction side, IFacadePart facade,
            boolean renderAnchor,
            Vec3 cameraRelativePos) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState blockOutlineRenderState,
                MultiBufferSource.BufferSource bufferSource,
                PoseStack poseStack,
                boolean b,
                LevelRenderState levelRenderState) {
            // Use same rendering inside blocks as part preview.
            showFacadePlacementPreview(poseStack, cameraRelativePos, bufferSource, true);
            showFacadePlacementPreview(poseStack, cameraRelativePos, bufferSource, false);
            return false;
        }

        private void showFacadePlacementPreview(PoseStack poseStack,
                Vec3 cameraRelativePos,
                MultiBufferSource buffers,
                boolean insideBlock) {
            if (renderAnchor) {
                var cableAnchor = AEParts.CABLE_ANCHOR.get().createPart();
                renderPart(poseStack, buffers, cameraRelativePos, cableAnchor, side, true, insideBlock);
            }

            renderFacade(poseStack, buffers, cameraRelativePos, facade, side, true, insideBlock);
        }
    }

    record FacadeOutlineRenderer(IFacadePart facade, Direction side,
            Vec3 cameraRelativePos) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState blockOutlineRenderState,
                MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean b,
                LevelRenderState levelRenderState) {
            renderFacade(poseStack, bufferSource, cameraRelativePos, facade, side, false,
                    false);

            return true;
        }
    }

    record PartOutlineRenderer(IPart part, Direction side,
            Vec3 cameraRelativePos) implements CustomBlockOutlineRenderer {
        @Override
        public boolean render(BlockOutlineRenderState blockOutlineRenderState,
                MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean b,
                LevelRenderState levelRenderState) {
            renderPart(poseStack, bufferSource, cameraRelativePos, part, side, false, false);
            return true;
        }
    }

    private static void renderPart(PoseStack poseStack,
            MultiBufferSource buffers,
            Vec3 cameraRelativePos,
            IPart part,
            Direction side,
            boolean preview,
            boolean insideBlock) {
        var boxes = new ArrayList<AABB>();
        var helper = new BusCollisionHelper(boxes, side, true);
        part.getBoxes(helper);
        renderBoxes(poseStack, buffers, cameraRelativePos, boxes, preview, insideBlock);
    }

    private static void renderFacade(PoseStack poseStack,
            MultiBufferSource buffers,
            Vec3 cameraRelativePos,
            IFacadePart facade,
            Direction side,
            boolean preview,
            boolean insideBlock) {
        var boxes = new ArrayList<AABB>();
        var helper = new BusCollisionHelper(boxes, side, true);
        facade.getBoxes(helper, false);
        renderBoxes(poseStack, buffers, cameraRelativePos, boxes, preview, insideBlock);
    }

    private static void renderBoxes(PoseStack poseStack,
            MultiBufferSource buffers,
            Vec3 cameraRelativePos,
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
                    cameraRelativePos.x,
                    cameraRelativePos.y,
                    cameraRelativePos.z,
                    ARGB.colorFromFloat(alpha,
                            preview ? 1 : 0,
                            preview ? 1 : 0,
                            preview ? 1 : 0));
        }
    }
}
