package appeng.bootstrap.components;

import appeng.bootstrap.IBootstrapComponent;
import net.minecraftforge.client.event.ModelBakeEvent;

public interface IModelBakeComponent extends IBootstrapComponent {

    void onModelBakeEvent(ModelBakeEvent event);

}
