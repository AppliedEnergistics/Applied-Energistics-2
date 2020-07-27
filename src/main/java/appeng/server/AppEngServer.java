package appeng.server;

import java.util.Random;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import appeng.api.parts.CableRenderMode;
import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.core.AppEngBase;
import appeng.core.sync.network.ServerNetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.hooks.TickHandler;

public final class AppEngServer extends AppEngBase {

    private final MinecraftServer server;

    private final ServerNetworkHandler networkHandler;

    private final TickHandler tickHandler;

    public AppEngServer(MinecraftServer server) {
        this.server = server;
        this.networkHandler = new ServerNetworkHandler();
        this.tickHandler = new TickHandler();

        // For a dedicated server, the lifecycle of WorldData is much simpler
        WorldData.onServerStarting(server);
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> onServerStopping());
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> onServerStopped());
    }

    private void onServerStopping() {
        WorldData.instance().onServerStopping();
    }

    private void onServerStopped() {
        WorldData.instance().onServerStoppped();
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
    public HitResult getRTR() {
        return null;
    }

    @Override
    public void postInit() {

    }

    @Override
    public CableRenderMode getRenderMode() {
        return null;
    }

    @Override
    public void updateRenderMode(PlayerEntity player) {

    }

    @Override
    public boolean isActionKey(@Nonnull ActionKey key, int keyCode, int scanCode) {
        return false;
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

}
