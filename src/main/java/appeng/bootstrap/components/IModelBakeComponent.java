package appeng.bootstrap.components;

import net.minecraftforge.client.event.ModelBakeEvent;

import appeng.bootstrap.IBootstrapComponent;

public interface IModelBakeComponent extends IBootstrapComponent {

    void onModelBakeEvent(ModelBakeEvent event);

}
