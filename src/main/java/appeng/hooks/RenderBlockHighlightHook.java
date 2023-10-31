package appeng.hooks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import appeng.client.render.overlay.OverlayRenderType;

public class RenderBlockHighlightHook {

    private RenderBlockHighlightHook() {

    }

    private static final float HALF_PI = (float) (Math.PI / 2f);

    public static void install() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(listener -> {
            var ps = listener.matrixStack();
            var buffer = listener.consumers();
            var camera = listener.camera();
            if (buffer != null) {
                tick(ps, buffer, camera);
            }
        });
    }

    private static void tick(PoseStack stack, MultiBufferSource multiBuf, Camera camera) {
        var world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        invalidate();
        var drawList = HighlightHandler.getBlockData();
        if (drawList.isEmpty()) {
            return;
        }
        // --- blinking ---
        if (((System.currentTimeMillis() / 500) & 1) == 0) {
            return;
        }
        for (var block : drawList) {
            if (block.checkDim(world.dimension())) {
                drawBlockOutline(block.pos(), stack, camera, multiBuf);
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    private static void invalidate() {
        while (HighlightHandler.getFirst() != null) {
            var info = HighlightHandler.getFirst();
            if (System.currentTimeMillis() > info.time()) {
                HighlightHandler.expire();
            } else {
                break;
            }
        }
    }

    private static void drawBlockOutline(BlockPos pos, PoseStack stack, Camera camera, MultiBufferSource multiBuf) {
        var r = 1f;
        var g = 0f;
        var b = 0f;
        var a = 1f;
        if (camera.isInitialized()) {
            Vec3 vec3 = camera.getPosition().reverse();
            AABB aabb = (new AABB(pos)).move(vec3);
            var topRight = new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ);
            var bottomRight = new Vec3(aabb.maxX, aabb.minY, aabb.maxZ);
            var bottomLeft = new Vec3(aabb.minX, aabb.minY, aabb.maxZ);
            var topLeft = new Vec3(aabb.minX, aabb.maxY, aabb.maxZ);
            var center = aabb.getCenter();
            for (int i = 0; i < 4; i++) {
                topRight = rotor(topRight, center, Direction.Axis.Y, HALF_PI);
                bottomRight = rotor(bottomRight, center, Direction.Axis.Y, HALF_PI);
                bottomLeft = rotor(bottomLeft, center, Direction.Axis.Y, HALF_PI);
                topLeft = rotor(topLeft, center, Direction.Axis.Y, HALF_PI);
                renderBox(multiBuf, stack, topLeft, bottomLeft, topRight, bottomRight, r, g, b, a);
            }
        }
    }

    private static void renderBox(MultiBufferSource multiBuf, PoseStack stack, Vec3 topLeft, Vec3 bottomLeft,
            Vec3 topRight, Vec3 bottomRight, float r, float g, float b, float a) {
        var buf = multiBuf.getBuffer(OverlayRenderType.getBlockHilightLine());
        renderLine(buf, stack, topLeft, bottomLeft, r, g, b, a);
        renderLine(buf, stack, topLeft, topRight, r, g, b, a);
        renderLine(buf, stack, bottomRight, bottomLeft, r, g, b, a);
    }

    private static void renderLine(VertexConsumer buf, PoseStack pose, Vec3 from, Vec3 to, float r, float g, float b,
            float a) {
        var mat = pose.last().pose();
        var normal = from.subtract(to);
        buf.vertex(mat, (float) from.x, (float) from.y, (float) from.z).color(r, g, b, a)
                .normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
        buf.vertex(mat, (float) to.x, (float) to.y, (float) to.z).color(r, g, b, a)
                .normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
    }

    private static Vec3 rotor(Vec3 point, Vec3 center, Direction.Axis axis, float a) {
        Vec3 normal = Vec3.ZERO;
        switch (axis) {
            case X -> normal = point.subtract(center).xRot(a);
            case Y -> normal = point.subtract(center).yRot(a);
            case Z -> normal = point.subtract(center).zRot(a);
        }
        return normal.add(center);
    }

}
