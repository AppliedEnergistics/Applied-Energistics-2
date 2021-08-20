package appeng.hooks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Event that is triggered on the client-side when the player uses the mouse-wheel while no UI is open. Returning true
 * will prevent default behavior (which would be switching the selected hotbar slot).
 */
public interface MouseWheelScrolled {
    Event<MouseWheelScrolled> EVENT = EventFactory.createArrayBacked(MouseWheelScrolled.class,
            (listeners) -> (scrollDelta) -> {
                for (MouseWheelScrolled listener : listeners) {
                    if (listener.onWheelScrolled(scrollDelta)) {
                        return true;
                    }
                }
                return false;
            });

    boolean onWheelScrolled(double scrollDelta);

}
