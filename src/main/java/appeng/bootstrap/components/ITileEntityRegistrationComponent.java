package appeng.bootstrap.components;

import appeng.bootstrap.IBootstrapComponent;

@FunctionalInterface
public interface ITileEntityRegistrationComponent extends IBootstrapComponent {
    void register();
}
