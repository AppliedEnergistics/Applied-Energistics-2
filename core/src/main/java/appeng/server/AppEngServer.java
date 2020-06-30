package appeng.server;

import appeng.api.parts.CableRenderMode;
import appeng.block.AEBaseBlock;
import appeng.client.ActionKey;
import appeng.client.EffectType;
import appeng.core.AppEngBase;
import appeng.core.sync.network.ServerNetworkHandler;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public final class AppEngServer extends AppEngBase {

    private final MinecraftServer server;

    private final ServerNetworkHandler networkHandler;

    public AppEngServer(MinecraftServer server) {
        this.server = server;
        this.networkHandler = new ServerNetworkHandler();
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
    public void triggerUpdates() {

    }

    @Override
    public void updateRenderMode(PlayerEntity player) {

    }

    @Override
    public boolean isActionKey(@Nonnull ActionKey key, InputUtil.Key input) {
        return false;
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

}
