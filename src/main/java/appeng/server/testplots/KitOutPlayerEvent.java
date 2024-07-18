package appeng.server.testplots;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public class KitOutPlayerEvent {
    public static final Event<Consumer<ServerPlayer>> EVENT = EventFactory.createArrayBacked(
            Consumer.class,
            (events) -> (player) -> {
                for (var event : events) {
                    event.accept(player);
                }
            });
}
