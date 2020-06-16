package appeng.parts.automation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

public class PlaneModelLoader implements IModelLoader<PlaneModel> {

    public static final PlaneModelLoader INSTANCE = new PlaneModelLoader();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    @Override
    public PlaneModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        String frontTexture = modelContents.get("front").getAsString();
        String sidesTexture = modelContents.get("sides").getAsString();
        String backTexture = modelContents.get("back").getAsString();

        return new PlaneModel(new ResourceLocation(frontTexture), new ResourceLocation(sidesTexture),
                new ResourceLocation(backTexture));
    }

}
