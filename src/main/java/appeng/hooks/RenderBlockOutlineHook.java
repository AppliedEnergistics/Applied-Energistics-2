package appeng.hooks;

import static net.minecraft.client.renderer.RenderStateShard.COLOR_WRITE;
import static net.minecraft.client.renderer.RenderStateShard.ITEM_ENTITY_TARGET;
import static net.minecraft.client.renderer.RenderStateShard.NO_CULL;
import static net.minecraft.client.renderer.RenderStateShard.RENDERTYPE_LINES_SHADER;
import static net.minecraft.client.renderer.RenderStateShard.TRANSLUCENT_TRANSPARENCY;
import static net.minecraft.client.renderer.RenderStateShard.VIEW_OFFSET_Z_LAYERING;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
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

import appeng.api.implementations.items.IFacadeItem;
import appeng.api.parts.IFacadePart;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.core.AEConfig;
import appeng.core.definitions.AEParts;
import appeng.items.parts.FacadeItem;
import appeng.parts.BusCollisionHelper;
import appeng.parts.PartPlacement;

public class RenderBlockOutlineHook {
    private RenderBlockOutlineHook() {
    }

    /**
     * Similar to {@link RenderType#LINES}, but with inverted depth test.
     */
    public static final RenderType.CompositeRenderType LINES_BEHIND_BLOCK = RenderType.create(
            "lines_behind_block",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard(">", GL11.GL_GREATER))
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    public static void install() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((WorldRenderContext context, @Nullable HitResult hitResult) -> {
            var level = context.world();
            var poseStack = context.matrixStack();
            var buffers = context.consumers();
            var camera = context.camera();
            if (level == null || buffers == null) {
                return true;
            }

            if (!(hitResult instanceof BlockHitResult blockHitResult)
                    || blockHitResult.getType() != HitResult.Type.BLOCK) {
                return true;
            }

            return !replaceBlockOutline(level, poseStack, buffers, camera, blockHitResult);
        });
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
            // Render without depth test to also have a preview for parts inside blocks.
            showPartPlacementPreview(player, poseStack, buffers, camera, hitResult, itemInHand, true);
            showPartPlacementPreview(player, poseStack, buffers, camera, hitResult, itemInHand, false);
        }

        // Hit test against all attached parts to highlight the part that is relevant
        var pos = hitResult.getBlockPos();
        if (level.getBlockEntity(pos) instanceof IPartHost partHost) {

            // Rendering a preview of what is currently in hand has priority
            // If the item in hand is a facade and a block is hit, attempt facade placement
            if (AEConfig.instance().isPlacementPreviewEnabled()) {
                var itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
                // Use same rendering inside blocks as part preview.
                showFacadePlacementPreview(poseStack, buffers, camera, hitResult, partHost, itemInHand, true);
                showFacadePlacementPreview(poseStack, buffers, camera, hitResult, partHost, itemInHand, false);
            }

            var selectedPart = partHost.selectPartWorld(hitResult.getLocation());
            if (selectedPart.facade != null) {
                renderFacade(poseStack, buffers, camera, pos, selectedPart.facade, selectedPart.side, false, false);
                return true;
            }
            if (selectedPart.part != null) {
                renderPart(poseStack, buffers, camera, pos, selectedPart.part, selectedPart.side, false, false);
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
            ItemStack itemInHand,
            boolean insideBlock) {
        var pos = blockHitResult.getBlockPos();

        if (itemInHand.getItem() instanceof IFacadeItem facadeItem) {
            var side = blockHitResult.getDirection();
            var facade = facadeItem.createPartFromItemStack(itemInHand, side);
            if (facade != null && FacadeItem.canPlaceFacade(partHost, facade)) {
                // Maybe a bit hacky, but if there's no part on the side to support the facade
                // We would render a cable anchor implicitly
                if (partHost.getPart(side) == null) {
                    var cableAnchor = AEParts.CABLE_ANCHOR.asItem().createPart();
                    renderPart(poseStack, buffers, camera, pos, cableAnchor, side, true, insideBlock);
                }

                renderFacade(poseStack, buffers, camera, pos, facade, side, true, insideBlock);
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
            ItemStack itemInHand,
            boolean insideBlock) {
        if (itemInHand.getItem() instanceof IPartItem<?>partItem) {
            var placement = PartPlacement.getPartPlacement(player,
                    player.level(),
                    itemInHand,
                    blockHitResult.getBlockPos(),
                    blockHitResult.getDirection(),
                    blockHitResult.getLocation());

            if (placement != null) {
                var part = partItem.createPart();
                renderPart(poseStack, buffers, camera, placement.pos(), part, placement.side(), true, insideBlock);
            }
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
        RenderType renderType = insideBlock ? LINES_BEHIND_BLOCK : RenderType.lines();
        var buffer = buffers.getBuffer(renderType);
        float alpha = insideBlock ? 0.2f : preview ? 0.6f : 0.4f;

        for (var box : boxes) {
            var shape = Shapes.create(box);

            LevelRenderer.renderShape(
                    poseStack,
                    buffer,
                    shape,
                    pos.getX() - camera.getPosition().x,
                    pos.getY() - camera.getPosition().y,
                    pos.getZ() - camera.getPosition().z,
                    preview ? 1 : 0,
                    preview ? 1 : 0,
                    preview ? 1 : 0,
                    alpha);
        }
    }
}
