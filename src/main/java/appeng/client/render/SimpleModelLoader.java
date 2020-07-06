package appeng.client.render;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

/**
 * A quaint model provider that provides a single model with a single given resource identifier.
 */
public class SimpleModelLoader<T extends UnbakedModel> implements ModelResourceProvider {

    private final Identifier identifier;

    private final Supplier<T> factory;

    public SimpleModelLoader(Identifier identifier, Supplier<T> factory) {
        this.factory = factory;
        this.identifier = identifier;
    }

    @Override
    public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) {
        if (identifier.equals(this.identifier)) {
            return factory.get();
        } else {
            return null;
        }
    }

}
