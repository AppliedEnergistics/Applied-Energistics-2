package appeng.core.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import appeng.client.Hotkey;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.core.network.ServerboundPacket;
import appeng.hotkeys.HotkeyActions;

public record HotkeyPacket(String hotkey) implements ServerboundPacket {
    public HotkeyPacket(Hotkey hotkey) {
        this(hotkey.name());
    }

    public static HotkeyPacket decode(FriendlyByteBuf stream) {
        var hotkey = stream.readUtf();
        return new HotkeyPacket(hotkey);
    }

    @Override
    public void write(FriendlyByteBuf data) {
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
