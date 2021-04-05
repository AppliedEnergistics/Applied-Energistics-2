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

package appeng.core.sync.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

/**
 * Created by covers1624 on 1/6/20.
 */
public class TargetPoint {

    public final ServerPlayerEntity excluded;
    public final double x;
    public final double y;
    public final double z;
    public final double radius;
    public final World world;

    public TargetPoint(double x, double y, double z, double radius, World world) {
        this(null, x, y, z, radius, world);
    }

    public TargetPoint(ServerPlayerEntity excluded, double x, double y, double z, double radius, World world) {
        this.excluded = excluded;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.world = world;
    }

}
