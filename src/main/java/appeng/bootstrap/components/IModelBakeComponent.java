package appeng.bootstrap.components;

import java.util.Map;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.Identifier;

import appeng.bootstrap.IBootstrapComponent;

public interface IModelBakeComponent extends IBootstrapComponent {

    void onModelsReloaded(Map<Identifier, BakedModel> loadedModels);

}
