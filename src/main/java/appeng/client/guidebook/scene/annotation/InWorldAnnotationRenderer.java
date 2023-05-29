package appeng.client.guidebook.scene.annotation;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;

import appeng.client.guidebook.color.MutableColor;
import appeng.core.AppEng;

public final class InWorldAnnotationRenderer {

    private static final RenderType OCCLUDED = RenderType.create(
            "annotation_occluded",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            0x100000,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setShaderState(RenderType.RENDERTYPE_TRANSLUCENT_NO_CRUMBLING_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard(">", GL11.GL_GREATER))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    private InWorldAnnotationRenderer() {
    }

    public static void render(MultiBufferSource.BufferSource buffers, Iterable<InWorldAnnotation> annotations) {
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(AppEng.makeId("block/noise"));

        var occludedConsumer = buffers.getBuffer(OCCLUDED);
        for (var annotation : annotations) {
            if (annotation instanceof InWorldBoxAnnotation boxAnnotation) {
                var color = MutableColor.of(boxAnnotation.color());
                color.darker(50).setAlpha(color.alpha() * 0.5f);
                if (boxAnnotation.isHovered()) {
                    color.lighter(50);
                }
                render(occludedConsumer,
                        boxAnnotation.min(),
                        boxAnnotation.max(),
                        color.toArgb32(),
                        boxAnnotation.thickness(),
                        sprite);
            }
        }
        buffers.endBatch(OCCLUDED);

        var consumer = buffers.getBuffer(RenderType.translucent());
        for (var annotation : annotations) {
            if (annotation instanceof InWorldBoxAnnotation boxAnnotation) {
                var color = MutableColor.of(boxAnnotation.color());
                if (boxAnnotation.isHovered()) {
                    color.lighter(50);
                }
                render(consumer,
                        boxAnnotation.min(),
                        boxAnnotation.max(),
                        color.toArgb32(),
                        boxAnnotation.thickness(),
                        sprite);
            }
        }
        buffers.endBatch(RenderType.translucent());
        buffers.endBatch();
    }

    public static void render(VertexConsumer consumer,
            Vector3f min,
            Vector3f max,
            int color,
            float thickness,
            TextureAtlasSprite sprite) {
        var thickHalf = thickness * 0.5f;

        var u = new Vector3f(max.x - min.x, 0, 0);
        var v = new Vector3f(0, max.y - min.y, 0);
        var t = new Vector3f(0, 0, max.z - min.z);
        var uNorm = new Vector3f(u).normalize();
        var vNorm = new Vector3f(v).normalize();
        var tNorm = new Vector3f(t).normalize();

        Vector3f[] corners = new Vector3f[8];
        corners[0] = new Vector3f(min);
        corners[1] = new Vector3f(min).add(u);
        corners[2] = new Vector3f(min).add(v);
        corners[3] = new Vector3f(min).add(t);
        corners[4] = new Vector3f(max);
        corners[5] = new Vector3f(max).sub(u);
        corners[6] = new Vector3f(max).sub(v);
        corners[7] = new Vector3f(max).sub(t);

        // Along X-Axis
        // Extend these out to cover past the corner (half the extrude thickness)
        strut(consumer, new Vector3f(uNorm).mulAdd(-thickHalf, corners[0]),
                new Vector3f(uNorm).mulAdd(thickHalf, corners[1]), color, thickness, true, true, sprite);
        strut(consumer, new Vector3f(uNorm).mulAdd(-thickHalf, corners[2]),
                new Vector3f(uNorm).mulAdd(thickHalf, corners[7]), color, thickness, true, true, sprite);
        strut(consumer, new Vector3f(uNorm).mulAdd(-thickHalf, corners[3]),
                new Vector3f(uNorm).mulAdd(thickHalf, corners[6]), color, thickness, true, true, sprite);
        strut(consumer, new Vector3f(uNorm).mulAdd(-thickHalf, corners[5]),
                new Vector3f(uNorm).mulAdd(thickHalf, corners[4]), color, thickness, true, true, sprite);

        // Along Y-Axis
        strut(consumer, new Vector3f(vNorm).mulAdd(thickHalf, corners[0]),
                new Vector3f(vNorm).mulAdd(-thickHalf, corners[2]), color, thickness, false, false, sprite);
        strut(consumer, new Vector3f(vNorm).mulAdd(thickHalf, corners[1]),
                new Vector3f(vNorm).mulAdd(-thickHalf, corners[7]), color, thickness, false, false, sprite);
        strut(consumer, new Vector3f(vNorm).mulAdd(thickHalf, corners[3]),
                new Vector3f(vNorm).mulAdd(-thickHalf, corners[5]), color, thickness, false, false, sprite);
        strut(consumer, new Vector3f(vNorm).mulAdd(thickHalf, corners[6]),
                new Vector3f(vNorm).mulAdd(-thickHalf, corners[4]), color, thickness, false, false, sprite);

        // Along Z-Axis
        strut(consumer, new Vector3f(tNorm).mulAdd(thickHalf, corners[0]),
                new Vector3f(tNorm).mulAdd(-thickHalf, corners[3]), color, thickness, false, false, sprite);
        strut(consumer, new Vector3f(tNorm).mulAdd(thickHalf, corners[1]),
                new Vector3f(tNorm).mulAdd(-thickHalf, corners[6]), color, thickness, false, false, sprite);
        strut(consumer, new Vector3f(tNorm).mulAdd(thickHalf, corners[2]),
                new Vector3f(tNorm).mulAdd(-thickHalf, corners[5]), color, thickness, false, false, sprite);
        strut(consumer, new Vector3f(tNorm).mulAdd(thickHalf, corners[7]),
                new Vector3f(tNorm).mulAdd(-thickHalf, corners[4]), color, thickness, false, false, sprite);
    }

    private static void strut(VertexConsumer consumer, Vector3f from, Vector3f to, int color, float thickness,
            boolean startCap, boolean endCap, TextureAtlasSprite sprite) {
        var norm = new Vector3f(to).sub(from).normalize();
        Vector3f upNorm;
        if (Math.abs(from.y - to.y) < 0.01f) {
            upNorm = new Vector3f(0, 1, 0);
        } else {
            upNorm = new Vector3f(1, 0, 0);
        }
        var rightNorm = new Vector3f(norm).cross(upNorm);
        var leftNorm = new Vector3f(rightNorm).negate();
        var downNorm = new Vector3f(norm).negate();

        var up = new Vector3f(upNorm).mul(thickness * 0.5f);
        var right = new Vector3f(rightNorm).mul(thickness * 0.5f);

        if (startCap) {
            quad(
                    consumer, downNorm, color,
                    new Vector3f(from).add(up).sub(right),
                    new Vector3f(from).sub(up).sub(right),
                    new Vector3f(from).sub(up).add(right),
                    new Vector3f(from).add(up).add(right),
                    sprite);
        }

        if (endCap) {
            quad(
                    consumer, norm, color,
                    new Vector3f(to).add(up).add(right),
                    new Vector3f(to).sub(up).add(right),
                    new Vector3f(to).sub(up).sub(right),
                    new Vector3f(to).add(up).sub(right),
                    sprite);
        }

        quad(
                consumer, leftNorm, color,
                new Vector3f(from).sub(right).add(up),
                new Vector3f(to).sub(right).add(up),
                new Vector3f(to).sub(right).sub(up),
                new Vector3f(from).sub(right).sub(up),
                sprite);
        quad(
                consumer, rightNorm, color,
                new Vector3f(to).add(right).sub(up),
                new Vector3f(to).add(right).add(up),
                new Vector3f(from).add(right).add(up),
                new Vector3f(from).add(right).sub(up),
                sprite);
        quad(
                consumer, upNorm, color,
                new Vector3f(from).add(up).sub(right),
                new Vector3f(from).add(up).add(right),
                new Vector3f(to).add(up).add(right),
                new Vector3f(to).add(up).sub(right),
                sprite);
        quad(
                consumer, downNorm, color,
                new Vector3f(to).sub(up).sub(right),
                new Vector3f(to).sub(up).add(right),
                new Vector3f(from).sub(up).add(right),
                new Vector3f(from).sub(up).sub(right),
                sprite);
    }

    private static void quad(VertexConsumer consumer, Direction face, int color, Vector3f v1, Vector3f v2,
            Vector3f offset, TextureAtlasSprite sprite) {
        vertex(consumer, face, color, v2, sprite.getU0(), sprite.getV0());
        vertex(consumer, face, color, v1, sprite.getV0(), sprite.getV1());
        vertex(consumer, face, color, new Vector3f(v1).add(offset), sprite.getV1(), sprite.getV1());
        vertex(consumer, face, color, new Vector3f(v2).add(offset), sprite.getV1(), sprite.getV0());
    }

    private static void vertex(VertexConsumer consumer,
            Direction face,
            int color,
            Vector3f bottomLeft,
            float u, float v) {
        consumer.vertex(bottomLeft.x, bottomLeft.y, bottomLeft.z)
                .color(color)
                .uv(u, v)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(face.getStepX(), face.getStepY(), face.getStepZ())
                .endVertex();
    }

    private static void quad(VertexConsumer consumer, Vector3f faceNormal, int color,
            Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4,
            TextureAtlasSprite sprite) {
        var d = Direction.getNearest(faceNormal.x, faceNormal.y, faceNormal.z);
        var shade = switch (d) {
            case DOWN -> 0.5F;
            case NORTH, SOUTH -> 0.8F;
            case WEST, EAST -> 0.6F;
            default -> 1.0F;
        };
        color = FastColor.ARGB32.multiply(
                FastColor.ARGB32.color(255, (int) (shade * 255), (int) (shade * 255), (int) (shade * 255)),
                color);

        vertex(consumer, faceNormal, color, v1, sprite.getU0(), sprite.getV0());
        vertex(consumer, faceNormal, color, v2, sprite.getV0(), sprite.getV1());
        vertex(consumer, faceNormal, color, v3, sprite.getV1(), sprite.getV1());
        vertex(consumer, faceNormal, color, v4, sprite.getV1(), sprite.getV0());
    }

    private static void vertex(VertexConsumer consumer,
            Vector3f faceNormal,
            int color,
            Vector3f bottomLeft,
            float u, float v) {
        consumer.vertex(bottomLeft.x, bottomLeft.y, bottomLeft.z)
                .color(color)
                .uv(u, v)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(faceNormal.x(), faceNormal.y(), faceNormal.z())
                .endVertex();
    }
}
