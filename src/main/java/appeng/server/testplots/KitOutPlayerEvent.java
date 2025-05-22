package appeng.server.testplots;

import java.util.function.Consumer;

import net.minecraft.server.level.ServerPlayer;

import appeng.api.events.Event;
import appeng.api.events.EventFactory;

public class KitOutPlayerEvent {
    public static final Event<Consumer<ServerPlayer>> EVENT = EventFactory.createArrayBacked(
            Consumer.class,
            (events) -> (player) -> {
                for (var event : events) {
                    event.accept(player);
                }
            });
}
