package appeng.api.hotkeys;

import appeng.hotkeys.LocatingServicesImpl;

/**
 * registers {@link LocatingService}
 */
public class LocatingServices {
    /**
     * register a new {@link LocatingService} under an id
     * <p/>
     * a Keybinding will be created automatically for every id
     */
    public static void register(LocatingService locatingService, String id) {
        LocatingServicesImpl.register(locatingService, id);
    }
}
