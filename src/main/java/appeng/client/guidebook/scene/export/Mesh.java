package appeng.client.guidebook.scene.export;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import org.joml.Vector2f;
import org.joml.Vector4i;

import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Captured rendering data.
 */
record Mesh(BufferBuilder.DrawState drawState,
        ByteBuffer vertexBuffer,
        ByteBuffer indexBuffer,
        RenderType renderType) {

    /**
     * Checks if the mesh contains any texture atlases that are animated.
     */
    public Stream<TextureAtlasSprite> getSprites() {

        var textureManager = Minecraft.getInstance().getTextureManager();

        // We only implement this for quads since standard block vertex-format
        // uses BakedQuads
        if (drawState.mode() != VertexFormat.Mode.QUADS) {
            return Stream.of();
        }

        // Get texture bound to sampler 0
        var samplers = RenderTypeIntrospection.getSamplers(renderType);
        if (samplers.isEmpty()) {
            return Stream.of(); // No textures
        }
        var texture = textureManager.getTexture(samplers.get(0).texture());
        if (!(texture instanceof TextureAtlas textureAtlas)) {
            return Stream.of(); // Only atlases can have sprites
        }

        var offset = 0;
        VertexFormatElement uvElement = null;
        for (var element : renderType.format().getElements()) {
            if (element.getUsage() == VertexFormatElement.Usage.UV && element.getIndex() == 0
                    && element.getCount() == 2) {
                uvElement = element;
                break;
            }
            offset += element.getByteSize();
        }

        if (uvElement == null) {
            return Stream.of(); // No UV coordinates
        }

        var uvSupplier = getUvSupplier(offset, uvElement);
        var spriteFinder = SpriteFinder.get(textureAtlas);

        return streamQuadMidpoints(uvSupplier)
                .map(uvPos -> spriteFinder.find(uvPos.x, uvPos.y))
                .filter(Objects::nonNull);
    }

    private Stream<Vector2f> streamQuadMidpoints(IntFunction<Vector2f> uvSupplier) {
        return streamIndices().map(indices -> getQuadMidpoint(indices.x, indices.y, indices.z, indices.w, uvSupplier));
    }

    private Stream<Vector4i> streamIndices() {
        if (drawState.sequentialIndex()) {
            var quadCount = drawState.vertexCount() / 4;
            return IntStream.range(0, quadCount)
                    .mapToObj(quadIdx -> new Vector4i(quadIdx * 4, quadIdx * 4 + 1, quadIdx * 4 + 2, quadIdx * 4 + 3));
        } else if (drawState.indexType() == VertexFormat.IndexType.INT) {
            var quadCount = drawState.indexCount() / 4;
            return IntStream.range(0, quadCount)
                    .mapToObj(quadIdx -> new Vector4i(
                            indexBuffer.getInt(quadIdx * 4 * 4),
                            indexBuffer.getInt(quadIdx * 4 * 4 + 4),
                            indexBuffer.getInt(quadIdx * 4 * 4 + 8),
                            indexBuffer.getInt(quadIdx * 4 * 4 + 12)));
        } else if (drawState.indexType() == VertexFormat.IndexType.SHORT) {
            var quadCount = drawState.indexCount() / 4;
            return IntStream.range(0, quadCount)
                    .mapToObj(quadIdx -> new Vector4i(
                            indexBuffer.getShort(quadIdx * 4 * 2),
                            indexBuffer.getShort(quadIdx * 4 * 2 + 2),
                            indexBuffer.getShort(quadIdx * 4 * 2 + 4),
                            indexBuffer.getShort(quadIdx * 4 * 2 + 6)));
        } else {
            throw new IllegalArgumentException("Unsupported index type: " + drawState.indexType());
        }
    }

    private IntFunction<Vector2f> getUvSupplier(int offset, VertexFormatElement uvElement) {
        return idx -> getUV(idx, offset, uvElement);
    }

    private Vector2f getQuadMidpoint(int i1, int i2, int i3, int i4, IntFunction<Vector2f> uvSupplier) {
        var uv1 = uvSupplier.apply(i1);
        var uv2 = uvSupplier.apply(i2);
        var uv3 = uvSupplier.apply(i3);
        var uv4 = uvSupplier.apply(i4);
        // We're making the assumption that a rectangle of the texture is used since
        // all atlas-entries are rectangular
        var avgX = (uv1.x + uv2.x + uv3.x + uv4.x) / 4f;
        var avgY = (uv1.y + uv2.y + uv3.y + uv4.y) / 4f;
        return new Vector2f(avgX, avgY);
    }

    private Vector2f getUV(int index, int offset, VertexFormatElement uvElement) {
        var stride = drawState.format().getVertexSize();
        var dataStart = index * stride + offset;
        return new Vector2f(
                readFloat(uvElement.getType(), dataStart),
                readFloat(uvElement.getType(), dataStart + uvElement.getType().getSize()));
    }

    private float readFloat(VertexFormatElement.Type type, int offset) {
        return switch (type) {
            case FLOAT -> vertexBuffer.getFloat(offset);
            case UBYTE -> ((int) vertexBuffer.get(offset)) & 0xFF;
            case BYTE -> vertexBuffer.get(offset);
            case USHORT -> ((int) vertexBuffer.getShort(offset)) & 0xFFFF;
            case SHORT -> vertexBuffer.getShort(offset);
            case UINT, INT -> vertexBuffer.getInt(offset);
        };
    }
}
