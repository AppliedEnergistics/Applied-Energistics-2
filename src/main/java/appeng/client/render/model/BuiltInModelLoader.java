package appeng.client.render.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Supplier;

public final class BuiltInModelLoader<T extends IUnbakedGeometry<T>> implements IGeometryLoader<T> {
    private final Supplier<T> supplier;

    public BuiltInModelLoader(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return supplier.get();
    }
}
