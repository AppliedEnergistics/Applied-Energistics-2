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

package appeng.integration.modules.waila.part;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

/**
 * Accessor to access specific parts for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class PartAccessor {
    /**
     * Hits a {@link IPartHost} with {@link BlockPos}.
     * <p/>
     * You can derive the looked at {@link IPart} by doing that. If a facade is
     * being looked at, it is defined as being absent.
     *
     * @param te  being looked at {@link BlockEntity}
     * @param rtr type of ray-trace
     *
     * @return maybe the looked at {@link IPart}
     */
    public Optional<IPart> getMaybePart(final BlockEntity te, final HitResult rtr) {
        if (te instanceof IPartHost && rtr instanceof BlockHitResult) {
            BlockPos pos = ((BlockHitResult) rtr).getBlockPos();
            final Vec3d position = rtr.getPos().add(-pos.getX(), -pos.getY(), -pos.getZ());
            final IPartHost host = (IPartHost) te;
            final SelectedPart sp = host.selectPart(position);

            if (sp.part != null) {
                return Optional.of(sp.part);
            }
        }

        return Optional.empty();
    }
}
