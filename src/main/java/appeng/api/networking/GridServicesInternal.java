package appeng.api.networking;

import java.util.Map;

/**
 * Allows access to non-public features of {@link GridServices}.
 */
public class GridServicesInternal {

    public static Map<Class<?>, IGridServiceProvider> createServices(IGrid g) {
        return GridServices.createServices(g);
    }

}
