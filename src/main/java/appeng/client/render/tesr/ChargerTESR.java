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

import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;

import appeng.client.render.renderable.ItemRenderable;
import appeng.tile.misc.ChargerTileEntity;

public final class ChargerTESR {

    private ChargerTESR() {
    }

    public static BlockEntityRendererProvider<ChargerTileEntity> FACTORY = context -> new ModularTESR<>(
            new ItemRenderable<>(ChargerTESR::getRenderedItem)
    );

    private static Pair<ItemStack, Transformation> getRenderedItem(ChargerTileEntity tile) {
        Transformation transform = new Transformation(new Vector3f(0.5f, 0.375f, 0.5f), null, null, null);
        return new ImmutablePair<>(tile.getInternalInventory().getStackInSlot(0), transform);
    }

}
