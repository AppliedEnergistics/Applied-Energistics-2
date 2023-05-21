package appeng.client.guidebook.scene;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import appeng.core.AppEng;

final class BlockHighlightRenderer {
    private static final float THICKNESS = 0.5f / 16f;

    private static final RenderType OCCLUDED = RenderType.create(
            "highlight_occluded",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            0x100000,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setShaderState(RenderType.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard(">", GL11.GL_GREATER))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(true));

    private BlockHighlightRenderer() {
    }

    public static void render(MultiBufferSource.BufferSource buffers, Iterable<BlockHighlight> highlights) {
        var occludedConsumer = buffers.getBuffer(OCCLUDED);
        for (var highlight : highlights) {
            var h = new BlockHighlight(highlight.pos(), highlight.r() * 0.25f, highlight.g() * 0.25f,
                    highlight.b() * 0.25f, 0.25f * highlight.a());
            render(occludedConsumer, h);
        }
        buffers.endBatch(OCCLUDED);

        var consumer = buffers.getBuffer(RenderType.translucent());
        for (var highlight : highlights) {
            render(consumer, highlight);
        }
        buffers.endBatch(RenderType.translucent());
        buffers.endBatch();
    }

    public static void render(VertexConsumer consumer, BlockHighlight highlight) {
        for (var face : Direction.values()) {
            makeFace(consumer, highlight.pos(), face, THICKNESS, highlight.r(), highlight.g(), highlight.b(),
                    highlight.a());
        }
    }

    /**
     * from and to are in the center of the strut.
     */
    private static void makeFace(VertexConsumer consumer,
            BlockPos pos,
            Direction face,
            float thickness,
            float r, float g, float b, float a) {
        var blockCenter = new Vector3f(
                pos.getX() + 0.5f,
                pos.getY() + 0.5f,
                pos.getZ() + 0.5f);
        var halfNormal = face.step().mul(1.05f).mul(0.5f);
        // Center of the face we're tessellating
        var nearCenter = new Vector3f(blockCenter).add(halfNormal);
        var farCenter = new Vector3f(blockCenter).sub(halfNormal)
                .add(face.step().mul(thickness));

        // This is the hard-baked directional block lighting that vanilla is using
        var shade = switch (face) {
            case DOWN -> 0.5F;
            case NORTH, SOUTH -> 0.8F;
            case WEST, EAST -> 0.6F;
            default -> 1.0F;
        };
        r *= shade;
        g *= shade;
        b *= shade;

        Direction upDir;
        Direction rightDir;
        if (face == Direction.UP) {
            upDir = Direction.NORTH;
            rightDir = Direction.EAST;
        } else if (face == Direction.DOWN) {
            upDir = Direction.SOUTH;
            rightDir = Direction.EAST;
        } else {
            upDir = Direction.UP;
            rightDir = face.getCounterClockWise(upDir.getAxis());
        }

        var up = upDir.step().mul(1.05f);
        var upHalf = up.mul(0.5f);
        var right = rightDir.step().mul(1.05f);
        var rightHalf = right.mul(0.5f);

        int color = FastColor.ARGB32.color(Mth.floor(a * 255f), Mth.floor(r * 255f), Mth.floor(g * 255f),
                Mth.floor(b * 255f));

        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(AppEng.makeId("block/noise"));

        for (var depth = 0; depth < 2; depth++) {
            // Four points on the outer edge
            var faceCenter = depth == 0 ? nearCenter : farCenter;
            var bottomLeft = new Vector3f(faceCenter).sub(rightHalf).sub(upHalf);
            var bottomRight = new Vector3f(faceCenter).add(rightHalf).sub(upHalf);
            var topLeft = new Vector3f(faceCenter).sub(rightHalf).add(upHalf);
            var topRight = new Vector3f(faceCenter).add(rightHalf).add(upHalf);

            var horThick = new Vector3f(rightDir.step()).mul(thickness);
            var verThick = new Vector3f(upDir.step()).mul(thickness);

            // Left strut
            quad(consumer, face, color, bottomLeft, topLeft, horThick, sprite);

            // Right strut
            quad(consumer, face, color, topRight, bottomRight, new Vector3f(horThick).mul(-1), sprite);

            // Bottom strut
            quad(consumer, face, color, bottomRight, bottomLeft, verThick, sprite);

            // Top strut
            quad(consumer, face, color, topLeft, topRight, new Vector3f(verThick).mul(-1), sprite);
        }
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
}
