package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.client.Hotkey;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.hotkeys.HotkeyActions;

public record HotkeyPacket(String hotkey) implements ServerboundPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, HotkeyPacket> STREAM_CODEC = StreamCodec.ofMember(
            HotkeyPacket::write,
            HotkeyPacket::decode);

    public static final Type<HotkeyPacket> TYPE = CustomAppEngPayload.createType("hotkey");

    @Override
    public Type<HotkeyPacket> type() {
        return TYPE;
    }

    public HotkeyPacket(Hotkey hotkey) {
        this(hotkey.name());
    }

    public static HotkeyPacket decode(RegistryFriendlyByteBuf stream) {
        var hotkey = stream.readUtf();
        return new HotkeyPacket(hotkey);
    }

    public void write(RegistryFriendlyByteBuf data) {
        data.writeUtf(this.hotkey);
    }

    public void handleOnServer(ServerPlayer player) {
        var actions = HotkeyActions.REGISTRY.get(hotkey);
        if (actions == null) {
            player.sendSystemMessage(
                    PlayerMessages.UnknownHotkey.text().copy().append(Component.translatable("key.ae2." + hotkey)));
            AELog.warn("Player %s tried using unknown hotkey \"%s\"", player, hotkey);
            return;
        }

        for (var action : actions) {
            if (action.run(player)) {
                break;
            }
        }
    }
}
