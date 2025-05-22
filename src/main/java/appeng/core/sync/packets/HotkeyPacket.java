package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import appeng.client.Hotkey;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.BasePacket;
import appeng.hotkeys.HotkeyActions;

public class HotkeyPacket extends BasePacket {
    private final String hotkey;

    public HotkeyPacket(FriendlyByteBuf stream) {
        hotkey = stream.readUtf();
    }

    public HotkeyPacket(Hotkey hotkey) {
        this.hotkey = hotkey.name();

        var data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeUtf(this.hotkey);

        this.configureWrite(data);
    }

    public void serverPacketData(ServerPlayer player) {
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
