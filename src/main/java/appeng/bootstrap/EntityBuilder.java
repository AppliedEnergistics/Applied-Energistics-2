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
        this.builder = EntityType.Builder.of(entityFactory, classification);
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
