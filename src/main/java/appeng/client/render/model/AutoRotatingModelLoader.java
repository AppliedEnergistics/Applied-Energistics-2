package appeng.client.render.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public class AutoRotatingModelLoader implements IModelLoader<AutoRotatingModel> {

    public static final AutoRotatingModelLoader INSTANCE = new AutoRotatingModelLoader();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    @Override
    public AutoRotatingModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        modelContents.remove("loader");
        BlockModel blockModel = deserializationContext.deserialize(modelContents, BlockModel.class);
        return new AutoRotatingModel(blockModel);
    }

}
