package appeng.container.me.common;

import appeng.helpers.InventoryAction;

public interface IMEInteractionHandler {
    void handleInteraction(long serial, InventoryAction action);
}
