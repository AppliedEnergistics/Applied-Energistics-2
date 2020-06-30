package appeng.client.render.crafting;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraftforge.client.model.IModelLoader;

/**
 * Provides our custom {@link EncodedPatternBakedModel encoded pattern item
 * model}.
 */
public class EncodedPatternModelLoader implements IModelLoader<EncodedPatternModel> {

    public static final EncodedPatternModelLoader INSTANCE = new EncodedPatternModelLoader();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    @Override
    public EncodedPatternModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        modelContents.remove("loader"); // Avoid recursion
        Identifier baseModel = new Identifier(JsonHelper.getString(modelContents, "baseModel"));
        return new EncodedPatternModel(baseModel);
    }

}
