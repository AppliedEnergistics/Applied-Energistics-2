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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.features.AEFeature;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IInitComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.core.AEItemGroup;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.features.ItemDefinition;
import appeng.util.Platform;

class ItemDefinitionBuilder implements IItemBuilder {

    private final FeatureFactory factory;

    private final String registryName;

    private final Function<Item.Properties, Item> itemFactory;

    private final EnumSet<AEFeature> features = EnumSet.noneOf(AEFeature.class);

    private final List<Function<Item, IBootstrapComponent>> boostrapComponents = new ArrayList<>();

    private final Item.Properties props = new Item.Properties();

    private Supplier<IDispenseItemBehavior> dispenserBehaviorSupplier;

    @OnlyIn(Dist.CLIENT)
    private ItemRendering itemRendering;

    private ItemGroup itemGroup = CreativeTab.INSTANCE;

    ItemDefinitionBuilder(FeatureFactory factory, String registryName, Function<Item.Properties, Item> itemFactory) {
        this.factory = factory;
        this.registryName = registryName;
        this.itemFactory = itemFactory;
        if (Platform.hasClientClasses()) {
            this.itemRendering = new ItemRendering();
        }
    }

    @Override
    public IItemBuilder bootstrap(Function<Item, IBootstrapComponent> component) {
        this.boostrapComponents.add(component);
        return this;
    }

    @Override
    public IItemBuilder features(AEFeature... features) {
        this.features.clear();
        this.addFeatures(features);
        return this;
    }

    @Override
    public IItemBuilder addFeatures(AEFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    @Override
    public IItemBuilder itemGroup(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
        return this;
    }

    @Override
    public IItemBuilder props(Consumer<Item.Properties> consumer) {
        consumer.accept(props);
        return this;
    }

    @Override
    public IItemBuilder rendering(ItemRenderingCustomizer callback) {
        if (Platform.hasClientClasses()) {
            this.customizeForClient(callback);
        }

        return this;
    }

    @Override
    public IItemBuilder dispenserBehavior(Supplier<IDispenseItemBehavior> behavior) {
        this.dispenserBehaviorSupplier = behavior;
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    private void customizeForClient(ItemRenderingCustomizer callback) {
        callback.customize(this.itemRendering);
    }

    @Override
    public ItemDefinition build() {
        props.group(itemGroup);

        Item item = this.itemFactory.apply(props);
        item.setRegistryName(AppEng.MOD_ID, this.registryName);

        ItemDefinition definition = new ItemDefinition(this.registryName, item, features);

        // Register all extra handlers
        this.boostrapComponents.forEach(component -> this.factory.addBootstrapComponent(component.apply(item)));

        // Register custom dispenser behavior if requested
        if (this.dispenserBehaviorSupplier != null) {
            this.factory.addBootstrapComponent((IInitComponent) () -> {
                IDispenseItemBehavior behavior = this.dispenserBehaviorSupplier.get();
                DispenserBlock.registerDispenseBehavior(item, behavior);
            });
        }

        this.factory.addBootstrapComponent((IItemRegistrationComponent) (side, reg) -> reg.register(item));

        if (Platform.hasClientClasses()) {
            this.itemRendering.apply(this.factory, item);
        }

        if (itemGroup instanceof AEItemGroup) {
            ((AEItemGroup) itemGroup).add(definition);
        }

        return definition;
    }

}
