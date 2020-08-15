package appeng.bootstrap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;

import appeng.api.features.AEFeature;

/**
 * Helper to register a custom Entity with Minecraft.
 */
public class EntityBuilder<T extends Entity> {

    private final FeatureFactory factory;

    private final String id;

    private final FabricEntityTypeBuilder<T> builder;

    private final EnumSet<AEFeature> features = EnumSet.noneOf(AEFeature.class);

    public EntityBuilder(FeatureFactory factory, String id, EntityType.EntityFactory<T> entityFactory,
            SpawnGroup classification) {
        this.factory = factory;
        this.id = id;
        this.builder = FabricEntityTypeBuilder.create(classification, entityFactory);
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

    public EntityBuilder<T> customize(Consumer<FabricEntityTypeBuilder<T>> function) {
        function.accept(builder);
        return this;
    }

    public EntityType<T> build() {
        EntityType<T> entityType = builder.build();
        String fullId = "appliedenergistics2:" + this.id;
        Registry.register(Registry.ENTITY_TYPE, fullId, entityType);
        return entityType;
    }
}
