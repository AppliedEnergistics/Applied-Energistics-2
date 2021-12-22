package appeng.hooks;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.core.definitions.AEParts;
import appeng.facade.IFacadeItem;
import appeng.items.parts.FacadeItem;
import appeng.parts.BusCollisionHelper;

public class RenderBlockOutlineHook {
    private RenderBlockOutlineHook() {
    }

    public static void install() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((WorldRenderContext context, @Nullable HitResult hitResult) -> {
            var level = context.world();
            var poseStack = context.matrixStack();
            var buffers = context.consumers();
            var camera = context.camera();
            if (level == null || buffers == null) {
                return true;
            }

            return !replaceBlockOutline(level, poseStack, buffers, camera, hitResult);
        });
    }

    /*
     * Changes block outline rendering such that it renders only for individual parts, not for the entire part host.
     */
    private static boolean replaceBlockOutline(ClientLevel level,
            PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            HitResult hitResult) {
        if (hitResult instanceof BlockHitResult blockHitResult) {
            // Hit test against all attached parts to highlight the part that is relevant
            var pos = blockHitResult.getBlockPos();
            if (level.getBlockEntity(pos) instanceof IPartHost partHost) {

                // Rendering a preview of what is currently in hand has priority
                // If the item in hand is a facade and a block is hit, attempt facade placement
                if (camera.getEntity() instanceof Player player) {
                    var itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
                    if (showFacadePlacementPreview(poseStack, buffers, camera, blockHitResult, partHost, itemInHand)) {
                        return true;
                    }
                }

                var selectedPart = partHost.selectPartWorld(hitResult.getLocation());
                if (selectedPart.facade != null) {
                    renderFacade(poseStack, buffers, camera, pos, selectedPart.facade, selectedPart.side, false);
                    return true;
                }
                if (selectedPart.part != null) {
                    renderPart(poseStack, buffers, camera, pos, selectedPart.part, selectedPart.side, false);
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean showFacadePlacementPreview(PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockHitResult blockHitResult,
            IPartHost partHost,
            ItemStack itemInHand) {
        var pos = blockHitResult.getBlockPos();

        if (itemInHand.getItem() instanceof IFacadeItem facadeItem) {
            var side = blockHitResult.getDirection();
            var facade = facadeItem.createPartFromItemStack(itemInHand, side);
            if (facade != null && FacadeItem.canPlaceFacade(partHost, facade)) {
                // Maybe a bit hacky, but if there's no part on the side to support the facade
                // We would render a cable anchor implicitly
                if (partHost.getPart(side) == null) {
                    var cableAnchor = AEParts.CABLE_ANCHOR.asItem().createPart();
                    renderPart(poseStack, buffers, camera, pos, cableAnchor, side, true);
                }

                renderFacade(poseStack, buffers, camera, pos, facade, side, true);
                return true;
            }
        }
        return false;
    }

    private static void renderPart(PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockPos pos,
            IPart part,
            Direction side,
            boolean preview) {
        var boxes = new ArrayList<AABB>();
        var helper = new BusCollisionHelper(boxes, side, true);
        part.getBoxes(helper);
        renderBoxes(poseStack, buffers, camera, pos, boxes, preview);
    }

    private static void renderFacade(PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockPos pos,
            IFacadePart facade,
            Direction side,
            boolean preview) {
        var boxes = new ArrayList<AABB>();
        var helper = new BusCollisionHelper(boxes, side, true);
        facade.getBoxes(helper, false);
        renderBoxes(poseStack, buffers, camera, pos, boxes, preview);
    }

    private static void renderBoxes(PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockPos pos,
            List<AABB> boxes,
            boolean preview) {
        for (var box : boxes) {
            var shape = Shapes.create(box);

            LevelRenderer.renderShape(
                    poseStack,
                    buffers.getBuffer(RenderType.lines()),
                    shape,
                    pos.getX() - camera.getPosition().x,
                    pos.getY() - camera.getPosition().y,
                    pos.getZ() - camera.getPosition().z,
                    preview ? 1 : 0,
                    preview ? 1 : 0,
                    preview ? 1 : 0,
                    0.4f);
        }
    }
}
