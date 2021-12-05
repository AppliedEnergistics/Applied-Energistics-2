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

package appeng.core.definitions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;

import appeng.entity.ChargedQuartzEntity;
import appeng.entity.GrowingCrystalEntity;
import appeng.entity.SingularityEntity;
import appeng.entity.TinyTNTPrimedEntity;

public final class AEEntities {

    private static final Map<ResourceLocation, EntityType<?>> ENTITY_TYPES = new HashMap<>();

    public static Map<ResourceLocation, EntityType<?>> getEntityTypes() {
        return Collections.unmodifiableMap(ENTITY_TYPES);
    }

    public static final EntityType<SingularityEntity> SINGULARITY = create(
            "singularity",
            SingularityEntity::new,
            MobCategory.MISC,
            builder -> builder.sized(0.2f, 0.2f).setTrackingRange(16).setUpdateInterval(4)
                    .setShouldReceiveVelocityUpdates(true));

    public static final EntityType<ChargedQuartzEntity> CHARGED_QUARTZ = create(
            "charged_quartz",
            ChargedQuartzEntity::new,
            MobCategory.MISC,
            builder -> builder.sized(0.2f, 0.2f).setTrackingRange(16).setUpdateInterval(4)
                    .setShouldReceiveVelocityUpdates(true));

    public static final EntityType<TinyTNTPrimedEntity> TINY_TNT_PRIMED = create(
            "tiny_tnt_primed",
            TinyTNTPrimedEntity::new,
            MobCategory.MISC,
            builder -> builder.setTrackingRange(16).setUpdateInterval(4).setShouldReceiveVelocityUpdates(true));

    public static EntityType<GrowingCrystalEntity> GROWING_CRYSTAL = create(
            "growing_crystal",
            GrowingCrystalEntity::new,
            MobCategory.MISC,
            builder -> builder.sized(0.25F, 0.4F));

    private static <T extends Entity> EntityType<T> create(String id,
            EntityFactory<T> entityFactory,
            MobCategory classification,
            Consumer<Builder<T>> customizer) {
        String registryLoc = "ae2:" + id;
        Builder<T> builder = Builder.of(entityFactory, classification);
        customizer.accept(builder);
        // Temporarily disable the data fixer check to avoid the annoying "no data fixer registered for ae2:xxx".
        boolean prev = SharedConstants.CHECK_DATA_FIXER_SCHEMA;
        SharedConstants.CHECK_DATA_FIXER_SCHEMA = false;
        EntityType<T> result = builder.build(registryLoc);
        SharedConstants.CHECK_DATA_FIXER_SCHEMA = prev;
        ENTITY_TYPES.put(new ResourceLocation(registryLoc), result);
        return result;
    }

}
