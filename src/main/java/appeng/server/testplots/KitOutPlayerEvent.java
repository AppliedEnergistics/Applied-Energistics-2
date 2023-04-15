package appeng.server.testplots;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class KitOutPlayerEvent extends Event {
    private final ServerPlayer player;

    public KitOutPlayerEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
