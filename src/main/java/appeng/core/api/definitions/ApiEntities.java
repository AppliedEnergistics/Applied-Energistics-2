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

package appeng.core.api.definitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

import appeng.entity.ChargedQuartzEntity;
import appeng.entity.GrowingCrystalEntity;
import appeng.entity.SingularityEntity;
import appeng.entity.TinyTNTPrimedEntity;

public final class ApiEntities {

    private static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>();

    public static List<EntityType<?>> getEntityTypes() {
        return Collections.unmodifiableList(ENTITY_TYPES);
    }

    public static final EntityType<SingularityEntity> SINGULARITY = create(
            "singularity",
            SingularityEntity::new,
            EntityClassification.MISC,
            builder -> builder.size(0.2f, 0.2f).setTrackingRange(16).setUpdateInterval(4)
                    .setShouldReceiveVelocityUpdates(true));

    public static final EntityType<ChargedQuartzEntity> CHARGED_QUARTZ = create(
            "charged_quartz",
            ChargedQuartzEntity::new,
            EntityClassification.MISC,
            builder -> builder.size(0.2f, 0.2f).setTrackingRange(16).setUpdateInterval(4)
                    .setShouldReceiveVelocityUpdates(true));

    public static final EntityType<TinyTNTPrimedEntity> TINY_TNT_PRIMED = create(
            "tiny_tnt_primed",
            TinyTNTPrimedEntity::new,
            EntityClassification.MISC,
            builder -> builder.setTrackingRange(16).setUpdateInterval(4).setShouldReceiveVelocityUpdates(true));

    public static EntityType<GrowingCrystalEntity> GROWING_CRYSTAL = create(
            "growing_crystal",
            GrowingCrystalEntity::new,
            EntityClassification.MISC,
            builder -> builder.size(0.25F, 0.4F));

    private static <T extends Entity> EntityType<T> create(String id,
            EntityType.IFactory<T> entityFactory,
            EntityClassification classification,
            Consumer<EntityType.Builder<T>> customizer) {
        String registryLoc = "appliedenergistics2:" + id;
        EntityType.Builder<T> builder = EntityType.Builder.create(entityFactory, classification);
        customizer.accept(builder);
        EntityType<T> result = builder.build(registryLoc);
        result.setRegistryName(registryLoc);
        ENTITY_TYPES.add(result);
        return result;
    }

}
