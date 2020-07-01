package appeng.bootstrap.components;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

import appeng.bootstrap.IBootstrapComponent;

import java.util.Map;

public interface IModelBakeComponent extends IBootstrapComponent {

    void onModelsReloaded(Map<Identifier, BakedModel> loadedModels);

}
