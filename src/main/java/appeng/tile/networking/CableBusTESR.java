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

package appeng.tile.networking;


import appeng.api.parts.IPart;
import appeng.tile.AEBaseTile;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;


public class CableBusTESR extends TileEntitySpecialRenderer<AEBaseTile> {

    @Override
    public void render(AEBaseTile te, double x, double y, double z, float partialTicks, int destroyStage, float p_render_10_) {

        if (!(te instanceof TileCableBusTESR)) {
            return;
        }

        TileCableBusTESR realTe = (TileCableBusTESR) te;

        for (EnumFacing facing : EnumFacing.values()) {
            IPart part = realTe.getPart(facing);
            if (part != null && part.requireDynamicRender()) {
                part.renderDynamic(x, y, z, partialTicks, destroyStage);
            }
        }
    }
}
