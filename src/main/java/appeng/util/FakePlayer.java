package appeng.util;

import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class FakePlayer extends ServerPlayer {
    private static final WeakHashMap<Level, FakePlayer> FAKE_PLAYERS = new WeakHashMap<>();

    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("60C173A5-E1E6-4B87-85B1-272CE424521D"),
            "[AppEng2]");

    private FakePlayer(ServerLevel level) {
        super(level.getServer(), level, PROFILE, null);
    }

    /**
     * DO NOT COPY THE PLAYER ANYWHERE! It will keep the world alive, always call this method if you need it.
     */
    static FakePlayer getOrCreate(ServerLevel level) {
        Objects.requireNonNull(level);

        final FakePlayer wrp = FAKE_PLAYERS.get(level);
        if (wrp != null) {
            return wrp;
        }

        FakePlayer p = new FakePlayer(level);
        FAKE_PLAYERS.put(level, p);
        return p;
    }

    public static boolean isFakePlayer(Player player) {
        return player instanceof ServerPlayer && player.getClass() != ServerPlayer.class;
    }

    @Override
    public void tick() {
    }

    @Override
    public void doTick() {
    }

    @Override
    public void sendSystemMessage(Component component, boolean bl) {
    }

    @Override
    public void sendChatMessage(OutgoingPlayerChatMessage outgoingPlayerChatMessage, boolean bl, ChatType.Bound bound) {
    }

    @Override
    public void sendChatHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
    }
}
