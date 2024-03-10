package appeng.client.render.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class ColorApplicatorLoader implements IGeometryLoader<ColorApplicatorModel> {
    @Override
    public ColorApplicatorModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        return null;
    }
}
