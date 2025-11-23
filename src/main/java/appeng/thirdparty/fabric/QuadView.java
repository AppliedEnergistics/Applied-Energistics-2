package appeng.thirdparty.fabric;
/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;

/**
 * Interface for reading quad data encoded by {@link MeshBuilder}. Enables models to do analysis, re-texturing or
 * translation without knowing the renderer's vertex formats and without retaining redundant information.
 *
 * <p>
 * Only the renderer should implement or extend this interface.
 */
public interface QuadView {
    /**
     * Retrieve geometric position, x coordinate.
     */
    float x(int vertexIndex);

    /**
     * Retrieve geometric position, y coordinate.
     */
    float y(int vertexIndex);

    /**
     * Retrieve geometric position, z coordinate.
     */
    float z(int vertexIndex);

    /**
     * Convenience: access x, y, z by index 0-2.
     */
    float posByIndex(int vertexIndex, int coordinateIndex);

    /**
     * Pass a non-null target to avoid allocation - will be returned with values. Otherwise returns a new instance.
     */
    Vector3f copyPos(int vertexIndex, @Nullable Vector3f target);

    /**
     * Retrieve vertex color.
     */
    int color(int vertexIndex);

    /**
     * Retrieve horizontal texture coordinates.
     */
    float u(int vertexIndex);

    /**
     * Retrieve vertical texture coordinates.
     */
    float v(int vertexIndex);

    /**
     * Whether this quad should be rendered with diffuse lighting
     */
    boolean hasShade();

    /**
     * Whether this quad should be rendered with ambient occlusion
     */
    boolean hasAmbientOcclusion();

    /**
     * Pass a non-null target to avoid allocation - will be returned with values. Otherwise returns a new instance.
     */
    default Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
        if (target == null) {
            target = new Vector2f();
        }

        target.set(u(vertexIndex), v(vertexIndex));
        return target;
    }

    /**
     * Minimum block brightness. Zero if not set.
     */
    int lightmap(int vertexIndex);

    /**
     * If false, no vertex normal was provided. Lighting should use face normal in that case.
     */
    boolean hasNormal(int vertexIndex);

    /**
     * Will return {@link Float#NaN} if normal not present.
     */
    float normalX(int vertexIndex);

    /**
     * Will return {@link Float#NaN} if normal not present.
     */
    float normalY(int vertexIndex);

    /**
     * Will return {@link Float#NaN} if normal not present.
     */
    float normalZ(int vertexIndex);

    /**
     * Pass a non-null target to avoid allocation - will be returned with values. Otherwise returns a new instance.
     * Returns null if normal not present.
     */
    @Nullable
    Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target);

    /**
     * If non-null, quad should not be rendered in-world if the opposite face of a neighbor block occludes it.
     *
     * @see MutableQuadView#cullFace(Direction)
     */
    @Nullable
    Direction cullFace();

    /**
     * Equivalent to {@link BakedQuad#direction()}. This is the face used for vanilla lighting calculations and will be
     * the block face to which the quad is most closely aligned. Always the same as cull face for quads that are on a
     * block face, but never null.
     */
    @NotNull
    Direction lightFace();

    /**
     * See {@link MutableQuadView#nominalFace(Direction)}.
     */
    Direction nominalFace();

    /**
     * Normal of the quad as implied by geometry. Will be invalid if quad vertices are not co-planar. Typically computed
     * lazily on demand and not encoded.
     *
     * <p>
     * Not typically needed by models. Exposed to enable standard lighting utility functions for use by renderers.
     */
    Vector3f faceNormal();

    /**
     * Retrieves the quad color index serialized with the quad.
     */
    int colorIndex();

    /**
     * Retrieves the integer tag encoded with this quad via {@link MutableQuadView#tag(int)}. Will return zero if no tag
     * was set. For use by models.
     */
    int tag();

    /**
     * Extracts all quad properties except material to the given {@link MutableQuadView} instance. Must be used before
     * calling {link QuadEmitter#emit()} on the target instance. Meant for re-texturing, analysis and static
     * transformation use cases.
     */
    void copyTo(MutableQuadView target);

    /**
     * Generates a new BakedQuad instance with texture coordinates and colors from the given sprite.
     *
     * @param sprite {@link MutableQuadView} does not serialize sprites so the sprite must be provided by the caller.
     *
     * @return A new baked quad instance with the closest-available appearance supported by vanilla features. Will
     *         retain emissive light maps, for example, but the standard Minecraft renderer will not use them.
     */
    default BakedQuad toBakedQuad(TextureAtlasSprite sprite) {
        return new BakedQuad(
                copyPos(0, null),
                copyPos(1, null),
                copyPos(2, null),
                copyPos(3, null),
                UVPair.pack(u(0), v(0)),
                UVPair.pack(u(1), v(1)),
                UVPair.pack(u(2), v(2)),
                UVPair.pack(u(3), v(3)),
                colorIndex(),
                lightFace(),
                sprite,
                hasShade(),
                0 /* emission */,
                BakedNormals.of(packedNormal(0), packedNormal(1), packedNormal(2), packedNormal(3)),
                BakedColors.of(color(0), color(1), color(2), color(3)),
                hasAmbientOcclusion());
    }

    private int packedNormal(int vertexIndex) {
        var x = normalX(vertexIndex);
        var y = normalY(vertexIndex);
        var z = normalZ(vertexIndex);
        return ((int) (x * 127.0f) & 0xFF) |
                (((int) (y * 127.0f) & 0xFF) << 8) |
                (((int) (z * 127.0f) & 0xFF) << 16);
    }

    default BakedQuad toBlockBakedQuad() {
        var finder = SpriteFinder.get(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS));
        return toBakedQuad(finder.find(this));
    }
}
