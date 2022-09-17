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

package appeng.entity;


import net.minecraft.entity.Entity;


public final class EntityIds {
    private static final int TINY_TNT = 10;
    private static final int SINGULARITY = 11;
    private static final int CHARGED_QUARTZ = 12;
    private static final int GROWING_CRYSTAL = 13;

    private EntityIds() {
    }

    public static int get(final Class<? extends Entity> droppedEntity) {
        if (droppedEntity == EntityTinyTNTPrimed.class) {
            return TINY_TNT;
        }
        if (droppedEntity == EntitySingularity.class) {
            return SINGULARITY;
        }
        if (droppedEntity == EntityChargedQuartz.class) {
            return CHARGED_QUARTZ;
        }
        if (droppedEntity == EntityGrowingCrystal.class) {
            return GROWING_CRYSTAL;
        }

        throw new IllegalStateException("Missing entity id: " + droppedEntity.getName());
    }
}
