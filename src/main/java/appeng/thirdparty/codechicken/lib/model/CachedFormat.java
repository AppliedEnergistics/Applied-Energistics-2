/*
 * This file is part of CodeChickenLib.
 * Copyright (c) 2018, covers1624, All rights reserved.
 *
 * CodeChickenLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * CodeChickenLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CodeChickenLib. If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.thirdparty.codechicken.lib.model;


import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A simple VertexFormat cache.
 * This caches the existence of attributes and their indexes.
 *
 * @author covers1624
 */
public class CachedFormat {

    public static final Map<VertexFormat, CachedFormat> formatCache = new ConcurrentHashMap<>();

    /**
     * Lookup or create the CachedFormat for a given VertexFormat.
     *
     * @param format The format to lookup.
     * @return The CachedFormat.
     */
    public static CachedFormat lookup(VertexFormat format) {
        return formatCache.computeIfAbsent(format, CachedFormat::new);
    }

    public VertexFormat format;

    public boolean hasPosition;
    public boolean hasNormal;
    public boolean hasColor;
    public boolean hasUV;
    public boolean hasLightMap;

    public int positionIndex = -1;
    public int normalIndex = -1;
    public int colorIndex = -1;
    public int uvIndex = -1;
    public int lightMapIndex = -1;

    public int elementCount;

    /**
     * Caches the vertex format element indexes for efficiency.
     *
     * @param format The format.
     */
    public CachedFormat(VertexFormat format) {
        this.format = format;
        this.elementCount = format.getElementCount();
        for (int i = 0; i < this.elementCount; i++) {
            VertexFormatElement element = format.getElement(i);
            switch (element.getUsage()) {
                case POSITION:
                    if (this.hasPosition) {
                        throw new IllegalStateException("Found 2 position elements..");
                    }
                    this.hasPosition = true;
                    this.positionIndex = i;
                    break;
                case NORMAL:
                    if (this.hasNormal) {
                        throw new IllegalStateException("Found 2 normal elements..");
                    }
                    this.hasNormal = true;
                    this.normalIndex = i;
                    break;
                case COLOR:
                    if (this.hasColor) {
                        throw new IllegalStateException("Found 2 color elements..");
                    }
                    this.hasColor = true;
                    this.colorIndex = i;
                    break;
                case UV:
                    if (element.getIndex() == 0) {
                        if (this.hasUV) {
                            throw new IllegalStateException("Found 2 UV elements..");
                        }
                        this.hasUV = true;
                        this.uvIndex = i;
                        break;
                    } else if (element.getIndex() == 1) {
                        if (this.hasLightMap) {
                            throw new IllegalStateException("Found 2 LightMap elements..");
                        }
                        this.hasLightMap = true;
                        this.lightMapIndex = i;
                        break;
                    }
                    break;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CachedFormat)) {
            return false;
        }
        CachedFormat other = (CachedFormat) obj;
        return other.elementCount == this.elementCount && //
                other.positionIndex == this.positionIndex && //
                other.normalIndex == this.normalIndex && //
                other.colorIndex == this.colorIndex && //
                other.uvIndex == this.uvIndex && //
                other.lightMapIndex == this.lightMapIndex;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + this.elementCount;
        result = 31 * result + this.positionIndex;
        result = 31 * result + this.normalIndex;
        result = 31 * result + this.colorIndex;
        result = 31 * result + this.uvIndex;
        result = 31 * result + this.lightMapIndex;
        return result;
    }
}
