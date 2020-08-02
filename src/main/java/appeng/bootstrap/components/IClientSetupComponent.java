package appeng.bootstrap.components;

import appeng.bootstrap.IBootstrapComponent;

/**
 * Will be run on the client-side only.
 */
public interface IClientSetupComponent extends IBootstrapComponent {

    void setup();

}
