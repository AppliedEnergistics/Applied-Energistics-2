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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Created by covers1624 on 1/6/20.
 */
public class TargetPoint {

    public final ServerPlayer excluded;
    public final double x;
    public final double y;
    public final double z;
    public final double r2;
    public final Level world;

    public TargetPoint(double x, double y, double z, double r2, Level world) {
        this(null, x, y, z, r2, world);
    }

    public TargetPoint(ServerPlayer excluded, double x, double y, double z, double r2, Level world) {
        this.excluded = excluded;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r2 = r2;
        this.world = world;
    }

    public static TargetPoint at(double x, double y, double z, double r2, Level world) {
        return new TargetPoint(x, y, z, r2, world);
    }

    public static TargetPoint at(ServerPlayer excluded, double x, double y, double z, double r2, Level world) {
        return new TargetPoint(excluded, x, y, z, r2, world);
    }
}
