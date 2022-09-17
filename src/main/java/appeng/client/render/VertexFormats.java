/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.client.render;


import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.client.FMLClientHandler;


/**
 * Utility for managing extended Vertex Formats without having to re-clone existing vertex formats over and over again.
 */
public final class VertexFormats {

    // Standard item format extended with lightmap coordinates
    private static final VertexFormat itemFormatWithLightMap = new VertexFormat(DefaultVertexFormats.ITEM).addElement(DefaultVertexFormats.TEX_2S);

    private VertexFormats() {
    }

    public static VertexFormat getFormatWithLightMap(VertexFormat format) {
        // Do not use this when Optifine is present or if the vanilla lighting pipeline is used
        if (FMLClientHandler.instance().hasOptifine() || !ForgeModContainer.forgeLightPipelineEnabled) {
            return format;
        }

        VertexFormat result;
        if (format == DefaultVertexFormats.BLOCK) {
            result = DefaultVertexFormats.BLOCK;
        } else if (format == DefaultVertexFormats.ITEM) {
            result = itemFormatWithLightMap;
        } else if (!format.hasUvOffset(1)) {
            result = new VertexFormat(format);
            result.addElement(DefaultVertexFormats.TEX_2S);
        } else {
            result = format; // Already has the needed UV, so keep it
        }
        return result;
    }
}
