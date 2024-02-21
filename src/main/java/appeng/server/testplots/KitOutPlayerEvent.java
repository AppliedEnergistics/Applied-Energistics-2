package appeng.server.testplots;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class KitOutPlayerEvent extends PlayerEvent {
    public KitOutPlayerEvent(ServerPlayer player) {
        super(player);
    }

    public ServerPlayer getPlayer() {
        return (ServerPlayer) getEntity();
    }
}
