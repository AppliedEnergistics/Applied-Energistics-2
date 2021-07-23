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

package appeng.helpers;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;

public class AEMaterials {

    public static final Material GLASS = make(MaterialColor.NONE, false, false, true, false, false, false,
            PushReaction.NORMAL);

    public static final Material FIXTURE = make(MaterialColor.METAL, false, false, false, false, false, false,
            PushReaction.DESTROY);

    /**
     * Small factory helper with named parameters.
     *
     * @param color
     * @param isLiquid
     * @param isSolid
     * @param blocksMovement
     * @param isOpaque
     * @param requiresNoTool
     * @param flammable
     * @param replaceable
     * @param pushReaction
     * @return
     */
    private static Material make(MaterialColor color, boolean isLiquid, boolean isSolid, boolean blocksMovement,
            boolean isOpaque, boolean flammable, boolean replaceable, PushReaction pushReaction) {
        return new Material(color, isLiquid, isSolid, blocksMovement, isOpaque, flammable, replaceable, pushReaction);
    }

}
