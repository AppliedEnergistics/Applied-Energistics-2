/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render.cablebus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.AppEng;

/**
 * A helper class that builds quads for cable connections.
 */
class CableBuilder {

    // Textures for the cable core types, one per type/color pair
    private final EnumMap<CableCoreType, EnumMap<AEColor, TextureAtlasSprite>> coreTextures;

    // Textures for rendering the actual connection cubes, one per type/color pair
    private final EnumMap<AECableType, EnumMap<AEColor, TextureAtlasSprite>> connectionTextures;

    private final SmartCableTextures smartCableTextures;

    CableBuilder(Function<RenderMaterial, TextureAtlasSprite> bakedTextureGetter) {
        this.coreTextures = new EnumMap<>(CableCoreType.class);

        for (CableCoreType type : CableCoreType.values()) {
            EnumMap<AEColor, TextureAtlasSprite> colorTextures = new EnumMap<>(AEColor.class);

            for (AEColor color : AEColor.values()) {
                colorTextures.put(color, bakedTextureGetter.apply(type.getTexture(color)));
            }

            this.coreTextures.put(type, colorTextures);
        }

        this.connectionTextures = new EnumMap<>(AECableType.class);

        for (AECableType type : AECableType.VALIDCABLES) {
            EnumMap<AEColor, TextureAtlasSprite> colorTextures = new EnumMap<>(AEColor.class);

            for (AEColor color : AEColor.values()) {
                colorTextures.put(color, bakedTextureGetter.apply(getConnectionTexture(type, color)));
            }

            this.connectionTextures.put(type, colorTextures);
        }

        this.smartCableTextures = new SmartCableTextures(bakedTextureGetter);
    }

    static RenderMaterial getConnectionTexture(AECableType cableType, AEColor color) {
        String textureFolder;
        switch (cableType) {
            case GLASS:
                textureFolder = "part/cable/glass/";
                break;
            case COVERED:
                textureFolder = "part/cable/covered/";
                break;
            case SMART:
                textureFolder = "part/cable/smart/";
                break;
            case DENSE_COVERED:
                textureFolder = "part/cable/dense_covered/";
                break;
            case DENSE_SMART:
                textureFolder = "part/cable/dense_smart/";
                break;
            default:
                throw new IllegalStateException("Cable type " + cableType + " does not support connections.");
        }

        return new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
                new ResourceLocation(AppEng.MOD_ID, textureFolder + color.name().toLowerCase()));
    }

    /**
     * Adds the core of a cable to the given list of quads.
     *
     * The type of cable core is automatically deduced from the given cable type.
     */
    public void addCableCore(AECableType cableType, AEColor color, List<BakedQuad> quadsOut) {
        switch (cableType) {
            case GLASS:
                this.addCableCore(CableCoreType.GLASS, color, quadsOut);
                break;
            case COVERED:
            case SMART:
                this.addCableCore(CableCoreType.COVERED, color, quadsOut);
                break;
            case DENSE_COVERED:
            case DENSE_SMART:
                this.addCableCore(CableCoreType.DENSE, color, quadsOut);
                break;
            default:
        }
    }

    public void addCableCore(CableCoreType coreType, AEColor color, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        TextureAtlasSprite texture = this.coreTextures.get(coreType).get(color);
        cubeBuilder.setTexture(texture);

        switch (coreType) {
            case GLASS:
                cubeBuilder.addCube(6, 6, 6, 10, 10, 10);
                break;
            case COVERED:
                cubeBuilder.addCube(5, 5, 5, 11, 11, 11);
                break;
            case DENSE:
                cubeBuilder.addCube(3, 3, 3, 13, 13, 13);
                break;
        }
    }

    public void addGlassConnection(Direction facing, AEColor cableColor, AECableType connectionType,
            boolean cableBusAdjacent, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        // We render all faces except the one on the connection side
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));

        // For to-machine connections, use a thicker end-cap for the connection
        if (connectionType != AECableType.GLASS && !cableBusAdjacent) {
            TextureAtlasSprite texture = this.connectionTextures.get(AECableType.COVERED).get(cableColor);
            cubeBuilder.setTexture(texture);

            this.addBigCoveredCableSizedCube(facing, cubeBuilder);
        }

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.GLASS).get(cableColor);
        cubeBuilder.setTexture(texture);

        switch (facing) {
            case DOWN:
                cubeBuilder.addCube(6, 0, 6, 10, 6, 10);
                break;
            case EAST:
                cubeBuilder.addCube(10, 6, 6, 16, 10, 10);
                break;
            case NORTH:
                cubeBuilder.addCube(6, 6, 0, 10, 10, 6);
                break;
            case SOUTH:
                cubeBuilder.addCube(6, 6, 10, 10, 10, 16);
                break;
            case UP:
                cubeBuilder.addCube(6, 10, 6, 10, 16, 10);
                break;
            case WEST:
                cubeBuilder.addCube(0, 6, 6, 6, 10, 10);
                break;
        }
    }

    public void addStraightGlassConnection(Direction facing, AEColor cableColor, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        // We render all faces except the connection caps. We can do this because the
        // glass cable is the smallest one
        // and its ends will always be covered by something
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite())));

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.GLASS).get(cableColor);
        cubeBuilder.setTexture(texture);

        switch (facing) {
            case DOWN:
            case UP:
                cubeBuilder.addCube(6, 0, 6, 10, 16, 10);
                break;
            case NORTH:
            case SOUTH:
                cubeBuilder.addCube(6, 6, 0, 10, 10, 16);
                break;
            case EAST:
            case WEST:
                cubeBuilder.addCube(0, 6, 6, 16, 10, 10);
                break;
        }
    }

    public void addConstrainedGlassConnection(Direction facing, AEColor cableColor, int distanceFromEdge,
            List<BakedQuad> quadsOut) {

        // Glass connections reach only 6 voxels from the edge
        if (distanceFromEdge >= 6) {
            return;
        }

        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.GLASS).get(cableColor);
        cubeBuilder.setTexture(texture);

        switch (facing) {
            case DOWN:
                cubeBuilder.addCube(6, distanceFromEdge, 6, 10, 6, 10);
                break;
            case EAST:
                cubeBuilder.addCube(10, 6, 6, 16 - distanceFromEdge, 10, 10);
                break;
            case NORTH:
                cubeBuilder.addCube(6, 6, distanceFromEdge, 10, 10, 6);
                break;
            case SOUTH:
                cubeBuilder.addCube(6, 6, 10, 10, 10, 16 - distanceFromEdge);
                break;
            case UP:
                cubeBuilder.addCube(6, 10, 6, 10, 16 - distanceFromEdge, 10);
                break;
            case WEST:
                cubeBuilder.addCube(distanceFromEdge, 6, 6, 6, 10, 10);
                break;
        }
    }

    public void addCoveredConnection(Direction facing, AEColor cableColor, AECableType connectionType,
            boolean cableBusAdjacent, List<BakedQuad> quadsOut) {

        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        // We render all faces except the one on the connection side
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.COVERED).get(cableColor);
        cubeBuilder.setTexture(texture);

        // Draw a covered connection, if anything but glass is requested
        if (connectionType != AECableType.GLASS && !cableBusAdjacent) {
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);
        }

        addCoveredCableSizedCube(facing, cubeBuilder);
    }

    public void addStraightCoveredConnection(Direction facing, AEColor cableColor, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.COVERED).get(cableColor);
        cubeBuilder.setTexture(texture);

        setStraightCableUVs(cubeBuilder, facing, 5, 11);

        addStraightCoveredCableSizedCube(facing, cubeBuilder);
    }

    private static void setStraightCableUVs(CubeBuilder cubeBuilder, Direction facing, int x, int y) {
        switch (facing) {
            case DOWN:
            case UP:
                cubeBuilder.setCustomUv(Direction.NORTH, x, 0, y, x);
                cubeBuilder.setCustomUv(Direction.EAST, x, 0, y, x);
                cubeBuilder.setCustomUv(Direction.SOUTH, x, 0, y, x);
                cubeBuilder.setCustomUv(Direction.WEST, x, 0, y, x);
                break;
            case EAST:
            case WEST:
                cubeBuilder.setCustomUv(Direction.UP, 0, x, x, y);
                cubeBuilder.setCustomUv(Direction.DOWN, 0, x, x, y);
                cubeBuilder.setCustomUv(Direction.NORTH, 0, x, x, y);
                cubeBuilder.setCustomUv(Direction.SOUTH, 0, x, x, y);
                break;
            case NORTH:
            case SOUTH:
                cubeBuilder.setCustomUv(Direction.UP, x, 0, y, x);
                cubeBuilder.setCustomUv(Direction.DOWN, x, 0, y, x);
                cubeBuilder.setCustomUv(Direction.EAST, 0, x, x, y);
                cubeBuilder.setCustomUv(Direction.WEST, 0, x, x, y);
                break;
        }
    }

    public void addConstrainedCoveredConnection(Direction facing, AEColor cableColor, int distanceFromEdge,
            List<BakedQuad> quadsOut) {
        // The core of a covered cable reaches up to 5 voxels from the block edge, so
        // drawing a connection can only occur from there onwards
        if (distanceFromEdge >= 5) {
            return;
        }

        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.COVERED).get(cableColor);
        cubeBuilder.setTexture(texture);

        addCoveredCableSizedCube(facing, distanceFromEdge, cubeBuilder);
    }

    public void addSmartConnection(Direction facing, AEColor cableColor, AECableType connectionType,
            boolean cableBusAdjacent, int channels, List<BakedQuad> quadsOut) {
        if (connectionType == AECableType.COVERED || connectionType == AECableType.GLASS) {
            this.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
            return;
        }

        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        // We render all faces except the one on the connection side
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.SMART).get(cableColor);
        cubeBuilder.setTexture(texture);

        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForChannels(channels);

        // For to-machine connections, use a thicker end-cap for the connection
        if (connectionType != AECableType.GLASS && !cableBusAdjacent) {
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);

            // Render the channel indicators brightly lit at night
            cubeBuilder.setRenderFullBright(true);

            cubeBuilder.setTexture(oddChannel);
            cubeBuilder.setColorRGB(cableColor.blackVariant);
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);

            cubeBuilder.setTexture(evenChannel);
            cubeBuilder.setColorRGB(cableColor.whiteVariant);
            this.addBigCoveredCableSizedCube(facing, cubeBuilder);

            // Reset back to normal rendering for the rest
            cubeBuilder.setRenderFullBright(false);
            cubeBuilder.setTexture(texture);
        }

        addCoveredCableSizedCube(facing, cubeBuilder);

        // Render the channel indicators brightly lit at night
        cubeBuilder.setRenderFullBright(true);

        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        addCoveredCableSizedCube(facing, cubeBuilder);

        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        addCoveredCableSizedCube(facing, cubeBuilder);
    }

    public void addStraightSmartConnection(Direction facing, AEColor cableColor, int channels,
            List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.SMART).get(cableColor);
        cubeBuilder.setTexture(texture);

        setStraightCableUVs(cubeBuilder, facing, 5, 11);

        addStraightCoveredCableSizedCube(facing, cubeBuilder);

        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForChannels(channels);

        // Render the channel indicators brightly lit at night
        cubeBuilder.setRenderFullBright(true);

        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        addStraightCoveredCableSizedCube(facing, cubeBuilder);

        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        addStraightCoveredCableSizedCube(facing, cubeBuilder);
    }

    public void addConstrainedSmartConnection(Direction facing, AEColor cableColor, int distanceFromEdge, int channels,
            List<BakedQuad> quadsOut) {
        // Same as with covered cables, the smart cable's core extends up to 5 voxels
        // away from the edge.
        // Drawing a connection to any point before that point is fruitless
        if (distanceFromEdge >= 5) {
            return;
        }

        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.SMART).get(cableColor);
        cubeBuilder.setTexture(texture);

        addCoveredCableSizedCube(facing, distanceFromEdge, cubeBuilder);

        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForChannels(channels);

        // Render the channel indicators brightly lit at night
        cubeBuilder.setRenderFullBright(true);

        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        addCoveredCableSizedCube(facing, distanceFromEdge, cubeBuilder);

        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        addCoveredCableSizedCube(facing, distanceFromEdge, cubeBuilder);
    }

    public void addDenseCoveredConnection(Direction facing, AEColor cableColor, AECableType connectionType,
            boolean cableBusAdjacent, List<BakedQuad> quadsOut) {
        // Dense cables only render their connections as dense if the adjacent blocks
        // actually wants that
        if (connectionType == AECableType.COVERED || connectionType == AECableType.SMART
                || connectionType == AECableType.GLASS) {
            this.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
            return;
        }

        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        // We render all faces except the one on the connection side
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.DENSE_COVERED).get(cableColor);
        cubeBuilder.setTexture(texture);

        addDenseCableSizedCube(facing, cubeBuilder);

        // Reset back to normal rendering for the rest
        cubeBuilder.setRenderFullBright(false);
        cubeBuilder.setTexture(texture);
    }

    public void addDenseSmartConnection(Direction facing, AEColor cableColor, AECableType connectionType,
            boolean cableBusAdjacent, int channels, List<BakedQuad> quadsOut) {
        // Dense cables only render their connections as dense if the adjacent blocks
        // actually wants that
        if (connectionType == AECableType.SMART) {
            this.addSmartConnection(facing, cableColor, connectionType, cableBusAdjacent, channels, quadsOut);
            return;
        } else if (connectionType == AECableType.COVERED || connectionType == AECableType.GLASS) {
            this.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
            return;
        } else if (connectionType == AECableType.DENSE_COVERED) {
            this.addDenseCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent, quadsOut);
            return;
        }

        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        // We render all faces except the one on the connection side
        cubeBuilder.setDrawFaces(EnumSet.complementOf(EnumSet.of(facing)));

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.DENSE_SMART).get(cableColor);
        cubeBuilder.setTexture(texture);

        addDenseCableSizedCube(facing, cubeBuilder);

        // Dense cables show used channels in groups of 4, rounded up
        channels = (channels + 3) / 4;

        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForChannels(channels);

        // Render the channel indicators brightly lit at night
        cubeBuilder.setRenderFullBright(true);

        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        addDenseCableSizedCube(facing, cubeBuilder);

        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        addDenseCableSizedCube(facing, cubeBuilder);

        // Reset back to normal rendering for the rest
        cubeBuilder.setRenderFullBright(false);
        cubeBuilder.setTexture(texture);
    }

    public void addStraightDenseCoveredConnection(Direction facing, AEColor cableColor, List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.DENSE_COVERED).get(cableColor);
        cubeBuilder.setTexture(texture);

        setStraightCableUVs(cubeBuilder, facing, 5, 11);

        addStraightDenseCableSizedCube(facing, cubeBuilder);
    }

    public void addStraightDenseSmartConnection(Direction facing, AEColor cableColor, int channels,
            List<BakedQuad> quadsOut) {
        CubeBuilder cubeBuilder = new CubeBuilder(quadsOut);

        TextureAtlasSprite texture = this.connectionTextures.get(AECableType.DENSE_SMART).get(cableColor);
        cubeBuilder.setTexture(texture);

        setStraightCableUVs(cubeBuilder, facing, 5, 11);

        addStraightDenseCableSizedCube(facing, cubeBuilder);

        // Dense cables show used channels in groups of 4, rounded up
        channels = (channels + 3) / 4;

        TextureAtlasSprite oddChannel = this.smartCableTextures.getOddTextureForChannels(channels);
        TextureAtlasSprite evenChannel = this.smartCableTextures.getEvenTextureForChannels(channels);

        // Render the channel indicators brightly lit at night
        cubeBuilder.setRenderFullBright(true);

        cubeBuilder.setTexture(oddChannel);
        cubeBuilder.setColorRGB(cableColor.blackVariant);
        addStraightDenseCableSizedCube(facing, cubeBuilder);

        cubeBuilder.setTexture(evenChannel);
        cubeBuilder.setColorRGB(cableColor.whiteVariant);
        addStraightDenseCableSizedCube(facing, cubeBuilder);
    }

    private static void addDenseCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN:
                cubeBuilder.addCube(4, 0, 4, 12, 5, 12);
                break;
            case EAST:
                cubeBuilder.addCube(11, 4, 4, 16, 12, 12);
                break;
            case NORTH:
                cubeBuilder.addCube(4, 4, 0, 12, 12, 5);
                break;
            case SOUTH:
                cubeBuilder.addCube(4, 4, 11, 12, 12, 16);
                break;
            case UP:
                cubeBuilder.addCube(4, 11, 4, 12, 16, 12);
                break;
            case WEST:
                cubeBuilder.addCube(0, 4, 4, 5, 12, 12);
                break;
        }
    }

    // Adds a cube to the given cube builder that has the size of a dense cable
    // connection and spans the entire block
    // for the given direction
    private static void addStraightDenseCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN:
            case UP:
                cubeBuilder.setUvRotation(Direction.EAST, 3);
                cubeBuilder.addCube(3, 0, 3, 13, 16, 13);
                cubeBuilder.setUvRotation(Direction.EAST, 0);
                break;
            case EAST:
            case WEST:
                cubeBuilder.setUvRotation(Direction.SOUTH, 3);
                cubeBuilder.setUvRotation(Direction.NORTH, 3);
                cubeBuilder.addCube(0, 3, 3, 16, 13, 13);
                cubeBuilder.setUvRotation(Direction.SOUTH, 0);
                cubeBuilder.setUvRotation(Direction.NORTH, 0);
                break;
            case NORTH:
            case SOUTH:
                cubeBuilder.setUvRotation(Direction.EAST, 3);
                cubeBuilder.setUvRotation(Direction.WEST, 3);
                cubeBuilder.addCube(3, 3, 0, 13, 13, 16);
                cubeBuilder.setUvRotation(Direction.EAST, 0);
                cubeBuilder.setUvRotation(Direction.WEST, 0);
                break;
        }
    }

    // Adds a cube to the given cube builder that has the size of a covered cable
    // connection from the core of the cable
    // to the given face
    private static void addCoveredCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN:
                cubeBuilder.addCube(6, 0, 6, 10, 5, 10);
                break;
            case EAST:
                cubeBuilder.addCube(11, 6, 6, 16, 10, 10);
                break;
            case NORTH:
                cubeBuilder.addCube(6, 6, 0, 10, 10, 5);
                break;
            case SOUTH:
                cubeBuilder.addCube(6, 6, 11, 10, 10, 16);
                break;
            case UP:
                cubeBuilder.addCube(6, 11, 6, 10, 16, 10);
                break;
            case WEST:
                cubeBuilder.addCube(0, 6, 6, 5, 10, 10);
                break;
        }
    }

    // Adds a cube to the given cube builder that has the size of a covered cable
    // connection and spans the entire block
    // for the given direction
    private static void addStraightCoveredCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN:
            case UP:
                cubeBuilder.setUvRotation(Direction.EAST, 3);
                cubeBuilder.addCube(5, 0, 5, 11, 16, 11);
                cubeBuilder.setUvRotation(Direction.EAST, 0);
                break;
            case EAST:
            case WEST:
                cubeBuilder.setUvRotation(Direction.SOUTH, 3);
                cubeBuilder.setUvRotation(Direction.NORTH, 3);
                cubeBuilder.addCube(0, 5, 5, 16, 11, 11);
                cubeBuilder.setUvRotation(Direction.SOUTH, 0);
                cubeBuilder.setUvRotation(Direction.NORTH, 0);
                break;
            case NORTH:
            case SOUTH:
                cubeBuilder.setUvRotation(Direction.EAST, 3);
                cubeBuilder.setUvRotation(Direction.WEST, 3);
                cubeBuilder.addCube(5, 5, 0, 11, 11, 16);
                cubeBuilder.setUvRotation(Direction.EAST, 0);
                cubeBuilder.setUvRotation(Direction.WEST, 0);
                break;
        }
    }

    private static void addCoveredCableSizedCube(Direction facing, int distanceFromEdge, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN:
                cubeBuilder.addCube(6, distanceFromEdge, 6, 10, 5, 10);
                break;
            case EAST:
                cubeBuilder.addCube(11, 6, 6, 16 - distanceFromEdge, 10, 10);
                break;
            case NORTH:
                cubeBuilder.addCube(6, 6, distanceFromEdge, 10, 10, 5);
                break;
            case SOUTH:
                cubeBuilder.addCube(6, 6, 11, 10, 10, 16 - distanceFromEdge);
                break;
            case UP:
                cubeBuilder.addCube(6, 11, 6, 10, 16 - distanceFromEdge, 10);
                break;
            case WEST:
                cubeBuilder.addCube(distanceFromEdge, 6, 6, 5, 10, 10);
                break;
        }
    }

    /**
     * This renders a slightly bigger covered cable connection to the specified
     * side. This is used to connect cable cores with adjacent machines that do not
     * want to be connected to using a glass cable connection. This applies to most
     * machines (interfaces, etc.)
     */
    private void addBigCoveredCableSizedCube(Direction facing, CubeBuilder cubeBuilder) {
        switch (facing) {
            case DOWN:
                cubeBuilder.addCube(5, 0, 5, 11, 4, 11);
                break;
            case EAST:
                cubeBuilder.addCube(12, 5, 5, 16, 11, 11);
                break;
            case NORTH:
                cubeBuilder.addCube(5, 5, 0, 11, 11, 4);
                break;
            case SOUTH:
                cubeBuilder.addCube(5, 5, 12, 11, 11, 16);
                break;
            case UP:
                cubeBuilder.addCube(5, 12, 5, 11, 16, 11);
                break;
            case WEST:
                cubeBuilder.addCube(0, 5, 5, 4, 11, 11);
                break;
        }
    }

    // Get all textures needed for building the actual cable quads
    public static List<RenderMaterial> getTextures() {
        List<RenderMaterial> locations = new ArrayList<>();

        for (CableCoreType coreType : CableCoreType.values()) {
            for (AEColor color : AEColor.values()) {
                locations.add(coreType.getTexture(color));
            }
        }

        for (AECableType cableType : AECableType.VALIDCABLES) {
            for (AEColor color : AEColor.values()) {
                locations.add(getConnectionTexture(cableType, color));
            }
        }

        Collections.addAll(locations, SmartCableTextures.SMART_CHANNELS_TEXTURES);

        return locations;
    }

    public TextureAtlasSprite getCoreTexture(CableCoreType coreType, AEColor color) {
        return this.coreTextures.get(coreType).get(color);
    }
}
