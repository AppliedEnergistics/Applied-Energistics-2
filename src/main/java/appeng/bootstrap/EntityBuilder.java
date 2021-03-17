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

package appeng.bootstrap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

import appeng.api.features.AEFeature;
import appeng.bootstrap.components.IEntityRegistrationComponent;
import appeng.core.AppEng;

/**
 * Helper to register a custom Entity with Minecraft.
 */
public class EntityBuilder<T extends Entity> {

    private final FeatureFactory factory;

    private final String id;

    private final EntityType.Builder<T> builder;

    private final EnumSet<AEFeature> features = EnumSet.noneOf(AEFeature.class);

    public EntityBuilder(FeatureFactory factory, String id, EntityType.IFactory<T> entityFactory,
            EntityClassification classification) {
        this.factory = factory;
        this.id = id;
        this.builder = EntityType.Builder.create(entityFactory, classification);
    }

    public EntityBuilder<T> features(AEFeature... features) {
        this.features.clear();
        this.addFeatures(features);
        return this;
    }

    public EntityBuilder<T> addFeatures(AEFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    public EntityBuilder<T> customize(Consumer<EntityType.Builder<T>> function) {
        function.accept(builder);
        return this;
    }

    public EntityType<T> build() {
        EntityType<T> entityType = builder.build("appliedenergistics2:" + id);
        entityType.setRegistryName(AppEng.MOD_ID, id);
        factory.addBootstrapComponent((IEntityRegistrationComponent) r -> {
            r.register(entityType);
        });
        return entityType;
    }
}
