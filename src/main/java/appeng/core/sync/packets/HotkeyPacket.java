package appeng.core.sync.packets;

import io.netty.buffer.Unpooled;

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.hotkeys.LocatingService;
import appeng.client.Hotkey;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.BasePacket;
import appeng.hotkeys.LocatingServicesImpl;

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
        var locatingServices = LocatingServicesImpl.REGISTRY.get(hotkey);
        if (locatingServices == null) {
            player.sendMessage(
                    PlayerMessages.UnknownHotkey.text().copy().append(new TranslatableComponent("key.ae2." + hotkey)),
                    Util.NIL_UUID);
            AELog.warn("Player %s tried using unknown hotkey \"%s\"", player, hotkey);
            return;
        }
        for (LocatingService locatingService : locatingServices) {
            if (locatingService.findAndOpen(player))
                break;
        }
    }
}
