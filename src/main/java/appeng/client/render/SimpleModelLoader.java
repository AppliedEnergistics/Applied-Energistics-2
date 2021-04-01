package appeng.client.render;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.util.ResourceLocation;

/**
 * A quaint model provider that provides a single model with a single given resource identifier.
 */
public class SimpleModelLoader<T extends IUnbakedModel> implements ModelResourceProvider {

    private final ResourceLocation identifier;

    private final Supplier<T> factory;

    public SimpleModelLoader(ResourceLocation identifier, Supplier<T> factory) {
        this.factory = factory;
        this.identifier = identifier;
    }

    @Override
    public IUnbakedModel loadModelResource(ResourceLocation identifier, ModelProviderContext modelProviderContext) {
        if (identifier.equals(this.identifier)) {
            return factory.get();
        } else {
            return null;
        }
    }

}
