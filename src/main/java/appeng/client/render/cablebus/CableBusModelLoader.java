package appeng.client.render.cablebus;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;
import appeng.core.features.registries.PartModels;

public class CableBusModelLoader implements ModelResourceProvider {

    private static final ResourceLocation CABLE_BUS_MODEL = AppEng.makeId("block/cable_bus");

    private final PartModels partModels;

    public CableBusModelLoader(PartModels partModels) {
        this.partModels = partModels;
    }

    @Override
    public IUnbakedModel loadModelResource(ResourceLocation resourceId, ModelProviderContext context)
            throws ModelProviderException {
        if (CABLE_BUS_MODEL.equals(resourceId)) {
            return new CableBusModel(partModels);
        } else {
            return null;
        }
    }

}
