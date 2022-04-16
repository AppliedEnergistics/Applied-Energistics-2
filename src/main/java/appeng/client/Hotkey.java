package appeng.client;

import net.minecraft.client.KeyMapping;

import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.HotkeyPacket;

public record Hotkey(String name, KeyMapping mapping) {
    public void check() {
        while (mapping().consumeClick()) {
            NetworkHandler.instance().sendToServer(new HotkeyPacket(this));
        }
    }
}
