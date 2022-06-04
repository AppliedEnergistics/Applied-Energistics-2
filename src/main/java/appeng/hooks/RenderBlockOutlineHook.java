package appeng.hooks;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.common.MinecraftForge;

import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.core.AEConfig;
import appeng.core.definitions.AEParts;
import appeng.facade.IFacadeItem;
import appeng.items.parts.FacadeItem;
import appeng.parts.BusCollisionHelper;
import appeng.parts.PartPlacement;

public class RenderBlockOutlineHook {
    private RenderBlockOutlineHook() {
    }

    public static void install() {
        MinecraftForge.EVENT_BUS.addListener(RenderBlockOutlineHook::handleEvent);
    }

    private static void handleEvent(RenderHighlightEvent.Block evt) {
        var level = Minecraft.getInstance().level;
        var poseStack = evt.getPoseStack();
        var buffers = evt.getMultiBufferSource();
        var camera = evt.getCamera();
        if (level == null || buffers == null) {
            return;
        }

        var blockHitResult = evt.getTarget();
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        if (replaceBlockOutline(level, poseStack, buffers, camera, blockHitResult)) {
            evt.setCanceled(true);
        }
    }

    /*
     * Changes block outline rendering such that it renders only for individual parts, not for the entire part host.
     */
    private static boolean replaceBlockOutline(ClientLevel level,
            PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockHitResult hitResult) {

        var player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        if (AEConfig.instance().isPlacementPreviewEnabled()) {
            var itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            showPartPlacementPreview(player, poseStack, buffers, camera, hitResult, itemInHand);
        }

        // Hit test against all attached parts to highlight the part that is relevant
        var pos = hitResult.getBlockPos();
        if (level.getBlockEntity(pos) instanceof IPartHost partHost) {

            // Rendering a preview of what is currently in hand has priority
            // If the item in hand is a facade and a block is hit, attempt facade placement
            if (AEConfig.instance().isPlacementPreviewEnabled()) {
                var itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
                showFacadePlacementPreview(poseStack, buffers, camera, hitResult, partHost, itemInHand);
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

    private static void showPartPlacementPreview(
            Player player,
            PoseStack poseStack,
            MultiBufferSource buffers,
            Camera camera,
            BlockHitResult blockHitResult,
            ItemStack itemInHand) {
        if (itemInHand.getItem() instanceof IPartItem<?>partItem) {
            var placement = PartPlacement.getPartPlacement(player,
                    player.level,
                    itemInHand,
                    blockHitResult.getBlockPos(),
                    blockHitResult.getDirection());

            if (placement != null) {
                var part = partItem.createPart();
                renderPart(poseStack, buffers, camera, placement.pos(), part, placement.side(), true);
            }
        }
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
