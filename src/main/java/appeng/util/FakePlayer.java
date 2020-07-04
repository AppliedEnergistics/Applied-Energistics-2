package appeng.util;

import com.mojang.authlib.GameProfile;
import io.netty.util.concurrent.CompleteFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.SucceededFuture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

public class FakePlayer extends ServerPlayerEntity {
    private static final WeakHashMap<World, FakePlayer> FAKE_PLAYERS = new WeakHashMap<>();

    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("60C173A5-E1E6-4B87-85B1-272CE424521D"), "[AppEng2]");

    private FakePlayer(ServerWorld world) {
        super(world.getServer(), world, PROFILE, new ServerPlayerInteractionManager(world));
    }

    /**
     * DO NOT COPY THE PLAYER ANYWHERE!
     * It will keep the world alive, always call this method if you need it.
     */
    public static FakePlayer getOrCreate(ServerWorld world) {
        Objects.requireNonNull(world);

        final FakePlayer wrp = FAKE_PLAYERS.get(world);
        if (wrp != null) {
            return wrp;
        }

        FakePlayer p = new FakePlayer(world);
        FAKE_PLAYERS.put(world, p);
        return p;
    }

    public static boolean isFakePlayer(PlayerEntity player) {
        // FIXME FABRIC: Is there a reliable way of detecting fake players of other mods???
        return player instanceof FakePlayer;
    }

    @Override
    public void tick() {
    }

    // FIXME: We should probably find and override all methods that access the networkHandler

}