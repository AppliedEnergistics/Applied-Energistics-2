package appeng.bootstrap.components;

import java.util.Map;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.ResourceLocation;

import appeng.bootstrap.IBootstrapComponent;

public interface IModelBakeComponent extends IBootstrapComponent {

    void onModelsReloaded(Map<ResourceLocation, IBakedModel> loadedModels);

}
