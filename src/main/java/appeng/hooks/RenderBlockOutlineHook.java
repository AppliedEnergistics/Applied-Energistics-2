package appeng.hooks;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;

import appeng.api.parts.IPartHost;
import appeng.parts.BusCollisionHelper;

public class RenderBlockOutlineHook {
    private RenderBlockOutlineHook() {
    }

    public static void install() {
        /*
         * Changes block outline rendering such that it renders only for individual parts, not for the entire part host.
         */
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((WorldRenderContext context, @Nullable HitResult hitResult) -> {
            if (hitResult != null) {
                // Hit test against all attached parts to highlight the part that is relevant
                var pos = new BlockPos(hitResult.getLocation());
                if (context.world().getBlockEntity(pos) instanceof IPartHost partHost) {
                    var selectedPart = partHost.selectPartWorld(hitResult.getLocation());
                    if (selectedPart.facade != null) {
                        var boxes = new ArrayList<AABB>();
                        var helper = new BusCollisionHelper(boxes, selectedPart.side, true);
                        selectedPart.facade.getBoxes(helper, false);
                        renderBoxes(context, pos, boxes);
                        return false;
                    }
                    if (selectedPart.part != null) {
                        var boxes = new ArrayList<AABB>();
                        var helper = new BusCollisionHelper(boxes, selectedPart.side, true);
                        selectedPart.part.getBoxes(helper);
                        renderBoxes(context, pos, boxes);
                        return false;
                    }
                }
            }

            return true;
        });
    }

    private static void renderBoxes(WorldRenderContext context, BlockPos pos, List<AABB> boxes) {
        for (var box : boxes) {
            var shape = Shapes.create(box);

            LevelRenderer.renderShape(
                    context.matrixStack(),
                    context.consumers().getBuffer(RenderType.lines()),
                    shape,
                    pos.getX() - context.camera().getPosition().x,
                    pos.getY() - context.camera().getPosition().y,
                    pos.getZ() - context.camera().getPosition().z,
                    0,
                    0,
                    0,
                    0.4f);
        }
    }
}
