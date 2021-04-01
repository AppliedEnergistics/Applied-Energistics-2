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

package appeng.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import appeng.api.features.AEFeature;
import appeng.bootstrap.components.ModelOverrideComponent;
import appeng.tile.AEBaseBlockEntity;
import appeng.util.Platform;

public class FeatureFactory {

    private final AEFeature[] defaultFeatures;

    private final Map<Class<? extends IBootstrapComponent>, List<IBootstrapComponent>> bootstrapComponents;

    @Environment(EnvType.CLIENT)
    private ModelOverrideComponent modelOverrideComponent;

    public FeatureFactory() {
        this.defaultFeatures = new AEFeature[] { AEFeature.CORE };
        this.bootstrapComponents = new HashMap<>();

        if (Platform.hasClientClasses()) {
            this.modelOverrideComponent = new ModelOverrideComponent();
            this.addBootstrapComponent(this.modelOverrideComponent);
        }
    }

    private FeatureFactory(FeatureFactory parent, AEFeature... defaultFeatures) {
        this.defaultFeatures = defaultFeatures.clone();
        this.bootstrapComponents = parent.bootstrapComponents;
        if (Platform.hasClientClasses()) {
            this.modelOverrideComponent = parent.modelOverrideComponent;
        }
    }

    public IBlockBuilder block(String id, Supplier<Block> block) {
        return new BlockDefinitionBuilder(this, id, block).features(this.defaultFeatures);
    }

    public IItemBuilder item(String id, Function<Item.Properties, Item> itemFactory) {
        return new ItemDefinitionBuilder(this, id, itemFactory).features(this.defaultFeatures);
    }

    public <T extends Entity> EntityBuilder<T> entity(String id, EntityType.IFactory<T> factory,
            EntityClassification classification) {
        return new EntityBuilder<T>(this, id, factory, classification).features(this.defaultFeatures);
    }

    public <T extends AEBaseBlockEntity> BlockEntityBuilder<T> tileEntity(String id, Class<T> teClass,
            Function<TileEntityType<T>, T> factory) {
        return new BlockEntityBuilder<>(this, id, teClass, factory).features(this.defaultFeatures);
    }

    public FeatureFactory features(AEFeature... features) {
        return new FeatureFactory(this, features);
    }

    public void addBootstrapComponent(IBootstrapComponent component) {
        Arrays.stream(component.getClass().getInterfaces()).filter(i -> IBootstrapComponent.class.isAssignableFrom(i))
                .forEach(i -> this.addBootstrapComponent((Class<? extends IBootstrapComponent>) i, component));
    }

    private <T extends IBootstrapComponent> void addBootstrapComponent(Class<? extends IBootstrapComponent> eventType,
            T component) {
        this.bootstrapComponents.computeIfAbsent(eventType, c -> new ArrayList<IBootstrapComponent>()).add(component);
    }

    @Environment(EnvType.CLIENT)
    void addModelOverride(String resourcePath, BiFunction<ResourceLocation, IBakedModel, IBakedModel> customizer) {
        this.modelOverrideComponent.addOverride(resourcePath, customizer);
    }

    public <T extends IBootstrapComponent> Iterator<T> getBootstrapComponents(Class<T> eventType) {
        return (Iterator<T>) this.bootstrapComponents.getOrDefault(eventType, Collections.emptyList()).iterator();
    }
}
