/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.client.render.tesr;

import java.util.function.Function;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

import appeng.client.render.renderable.ItemRenderable;
import appeng.tile.misc.ChargerTileEntity;

public final class ChargerTESR {

    private ChargerTESR() {
    }

    public static Function<TileEntityRendererDispatcher, TileEntityRenderer<ChargerTileEntity>> FACTORY = dispatcher -> new ModularTESR<>(
            dispatcher, new ItemRenderable<>(ChargerTESR::getRenderedItem));

    private static Pair<ItemStack, ItemTransformVec3f> getRenderedItem(ChargerTileEntity tile) {
        ItemTransformVec3f transform = new ItemTransformVec3f(new Vector3f(), new Vector3f(0.5f, 0.375f, 0.5f),
                new Vector3f(1f, 1f, 1f));
        return new ImmutablePair<>(tile.getInternalInventory().getInvStack(0), transform);
    }

}
