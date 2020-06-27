package appeng.bootstrap;

import appeng.api.features.AEFeature;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * Helper to register a custom Entity with Minecraft.
 */
public class EntityBuilder<T extends Entity> {

    private final FeatureFactory factory;

    private final String id;

    private final EntityType.Builder<T> builder;

    private final EnumSet<AEFeature> features = EnumSet.noneOf(AEFeature.class);

    public EntityBuilder(FeatureFactory factory, String id, EntityType.EntityFactory<T> entityFactory,
            SpawnGroup classification) {
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
        String fullId = "appliedenergistics2:" + this.id;
        EntityType<T> entityType = builder.build(fullId);
        Registry.register(Registry.ENTITY_TYPE, fullId, entityType);
        return entityType;
    }
}
