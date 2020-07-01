package appeng.client.render;

import java.util.function.Supplier;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

/**
 * A quaint model loader that does not accept any additional parameters in JSON.
 */
public class SimpleModelLoader<T extends IModelGeometry<T>> implements IModelLoader<T> {

    private final Supplier<T> factory;

    public SimpleModelLoader(Supplier<T> factory) {
        this.factory = factory;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public T read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return factory.get();
    }

}
