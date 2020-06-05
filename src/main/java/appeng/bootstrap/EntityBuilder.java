package appeng.bootstrap;

import appeng.api.features.AEFeature;
import appeng.bootstrap.components.IEntityRegistrationComponent;
import appeng.core.AppEng;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

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

    public EntityBuilder(FeatureFactory factory, String id, EntityType.IFactory<T> entityFactory, EntityClassification classification) {
        this.factory = factory;
        this.id = id;
        this.builder = EntityType.Builder.<T>create(entityFactory, classification);
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

    public void build() {
        factory.addBootstrapComponent((IEntityRegistrationComponent) r -> {
            EntityType<T> entityType = builder.build("appliedenergistics2:" + id);
            r.register( entityType.setRegistryName(AppEng.MOD_ID, id) );
        });
    }
}
