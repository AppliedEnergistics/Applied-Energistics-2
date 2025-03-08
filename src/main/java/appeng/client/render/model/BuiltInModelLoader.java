package appeng.client.render.model;

import appeng.core.AppEng;
import appeng.hooks.BuiltInModelHooks;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public class BuiltInModelLoader implements UnbakedModelLoader<UnbakedModel> {
    public static final ResourceLocation ID = AppEng.makeId("builtin");

    @Override
    public UnbakedModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        var builtInId = ResourceLocation.parse(jsonObject.get("ae2:model").getAsString());
        var model = BuiltInModelHooks.getBuiltInModel(builtInId);
        if (model == null) {
            throw new JsonParseException("Unknown built-in AE2 model: " + model);
        }
        return model;
    }
}
