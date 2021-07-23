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

package appeng.integration.modules.theoneprobe.part;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import mcjty.theoneprobe.api.IProbeHitData;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;

public final class PartAccessor {

    public Optional<IPart> getMaybePart(final BlockEntity te, final IProbeHitData data) {
        if (te instanceof IPartHost) {
            net.minecraft.core.BlockPos pos = data.getPos();
            final Vec3 position = data.getHitVec().add(-pos.getX(), -pos.getY(), -pos.getZ());
            final IPartHost host = (IPartHost) te;
            final SelectedPart sp = host.selectPart(position);

            if (sp.part != null) {
                return Optional.of(sp.part);
            }
        }

        return Optional.empty();
    }
}
