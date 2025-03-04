package appeng.init.client;

import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import appeng.client.item.PortableCellColorTintSource;
import appeng.client.item.StorageCellStateTintSource;

public final class InitItemColors {
    private InitItemColors() {
    }

    public static void init(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(PortableCellColorTintSource.ID, PortableCellColorTintSource.MAP_CODEC);
        event.register(StorageCellStateTintSource.ID, StorageCellStateTintSource.MAP_CODEC);
    }
}
