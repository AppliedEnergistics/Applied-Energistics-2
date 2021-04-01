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

package appeng.bootstrap;

import java.util.function.Function;

import appeng.tile.AEBaseTileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class TileEntityRendering<T extends AEBaseTileEntity> {

    @Environment(EnvType.CLIENT)
    Function<TileEntityRendererDispatcher, TileEntityRenderer<T>> tileEntityRenderer;

    @Environment(EnvType.CLIENT)
    public TileEntityRendering<T> tileEntityRenderer(
            Function<TileEntityRendererDispatcher, TileEntityRenderer<T>> tileEntityRenderer) {
        this.tileEntityRenderer = tileEntityRenderer;
        return this;
    }

}
