package appeng.api.features;

import appeng.core.worlddata.PlayerData;

public final class PlayerRegistryInternal {

    private PlayerRegistryInternal() {
    }

    public static void initialize() {
        IPlayerRegistry.Holder.lookup = PlayerData::get;
    }

}
