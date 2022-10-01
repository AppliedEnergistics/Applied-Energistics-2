package appeng.util;

import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class FakePlayer {
    private static final WeakHashMap<Level, net.minecraftforge.common.util.FakePlayer> FAKE_PLAYERS = new WeakHashMap<>();

    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("60C173A5-E1E6-4B87-85B1-272CE424521D"),
            "[AE2]");

    private FakePlayer() {
    }

    /**
     * DO NOT COPY THE PLAYER ANYWHERE! It will keep the world alive, always call this method if you need it.
     */
    static Player getOrCreate(ServerLevel level) {
        Objects.requireNonNull(level);

        var wrp = FAKE_PLAYERS.get(level);
        if (wrp != null) {
            return wrp;
        }

        var p = new net.minecraftforge.common.util.FakePlayer(level, PROFILE);
        FAKE_PLAYERS.put(level, p);
        return p;
    }
}
