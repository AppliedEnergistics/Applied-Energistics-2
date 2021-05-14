package appeng.server;

import java.util.Random;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.core.AppEngBase;
import appeng.core.sync.network.ServerNetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.hooks.ticking.TickHandler;

public final class AppEngServer extends AppEngBase {

    private MinecraftServer server;

    private final ServerNetworkHandler networkHandler;

    private final TickHandler tickHandler;

    public AppEngServer() {
        this.networkHandler = new ServerNetworkHandler();
        this.tickHandler = new TickHandler();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(WorldData::onServerStoppped);
    }

    private void onServerStarting(MinecraftServer server) {
        // For a dedicated server, the lifecycle of WorldData is much simpler
        WorldData.onServerStarting(server);

        this.server = server;
    }

    @Override
    public Stream<? extends PlayerEntity> getPlayers() {
        return PlayerStream.all(server);
    }

    @Override
    public void spawnEffect(EffectType effect, World world, double posX, double posY, double posZ, Object extra) {

    }

    @Override
    public boolean shouldAddParticles(Random r) {
        return false;
    }

    @Override
    public RayTraceResult getRTR() {
        return null;
    }

    @Override
    public void postInit() {

    }

    @Override
    public boolean isActionKey(@Nonnull ActionKey key, InputMappings.Input input) {
        return false;
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public boolean isOnServerThread() {
        return server != null && server.isOnExecutionThread();
    }

    @Override
    public World getClientWorld() {
        throw new UnsupportedOperationException("Cannot call getClientWorld() on the server.");
    }
}
