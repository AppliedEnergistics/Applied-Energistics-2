package appeng.bootstrap.components;

import appeng.bootstrap.IBootstrapComponent;

/**
 * Will be run during {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent}.
 */
public interface IClientSetupComponent extends IBootstrapComponent {

    void setup();

}
