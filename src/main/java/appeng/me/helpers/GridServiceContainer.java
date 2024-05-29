package appeng.me.helpers;

import java.util.Map;

import appeng.api.networking.IGridServiceProvider;

public record GridServiceContainer(
        Map<Class<?>, IGridServiceProvider> services,
        IGridServiceProvider[] serverStartTickServices,
        IGridServiceProvider[] levelStartTickServices,
        IGridServiceProvider[] levelEndtickServices,
        IGridServiceProvider[] serverEndTickServices) {
}
