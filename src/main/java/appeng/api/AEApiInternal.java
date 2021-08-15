package appeng.api;

import appeng.core.api.ApiClientHelper;
import appeng.core.api.ApiCrafting;
import appeng.core.api.ApiGrid;
import appeng.core.api.ApiPart;
import appeng.core.api.ApiStorage;

public final class AEApiInternal {

    private AEApiInternal() {
    }

    public static void init() {
        AEApi.initialize(
                new ApiStorage(),
                new ApiCrafting(),
                new ApiGrid(),
                new ApiPart(),
                new ApiClientHelper());
    }

}
