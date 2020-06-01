package appeng.client.render.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public class GlassModelLoader implements IModelLoader<GlassModel> {

    public static final GlassModelLoader INSTANCE = new GlassModelLoader();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    @Override
    public GlassModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new GlassModel();
    }

}
