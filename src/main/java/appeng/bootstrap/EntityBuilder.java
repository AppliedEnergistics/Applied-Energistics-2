package appeng.bootstrap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import appeng.api.features.AEFeature;
import appeng.bootstrap.components.IClientSetupComponent;
import appeng.bootstrap.components.IEntityRegistrationComponent;
import appeng.core.AppEng;
import appeng.entity.EntityFloatingItem;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.entity.RenderFloatingItem;
import appeng.entity.RenderTinyTNTPrimed;

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
