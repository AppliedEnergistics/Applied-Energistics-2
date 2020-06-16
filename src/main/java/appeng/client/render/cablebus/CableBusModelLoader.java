package appeng.client.render.cablebus;

import appeng.core.features.registries.PartModels;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public class CableBusModelLoader implements IModelLoader<CableBusModel> {

    private final PartModels partModels;

    public CableBusModelLoader(PartModels partModels) {
        this.partModels = partModels;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        CableBusBakedModel.clearCache();
    }

    @Override
    public CableBusModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new CableBusModel(partModels);
    }

}
