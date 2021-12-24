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

import net.minecraft.world.level.block.entity.BlockEntity;

import mcjty.theoneprobe.api.IProbeHitData;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;

public final class PartAccessor {

    public Optional<IPart> getMaybePart(final BlockEntity te, final IProbeHitData data) {
        if (te instanceof IPartHost host) {
            var sp = host.selectPartWorld(data.getHitVec());

            if (sp.part != null) {
                return Optional.of(sp.part);
            }
        }

        return Optional.empty();
    }
}
