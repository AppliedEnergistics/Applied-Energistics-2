package appeng.core.network.bidirectional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.api.config.Setting;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.ServerboundPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ConfigValuePacket(String name, String value) implements ClientboundPacket, ServerboundPacket {
    public static ConfigValuePacket decode(FriendlyByteBuf stream) {
        var name = stream.readUtf();
        var value = stream.readUtf();
        return new ConfigValuePacket(name, value);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeUtf(name);
        data.writeUtf(value);
    }

    public <T extends Enum<T>> ConfigValuePacket(Setting<T> setting, T value) {
        this(setting.getName(), value.name());
        if (!setting.getValues().contains(value)) {
            throw new IllegalStateException(value + " not a valid value for " + setting);
        }
    }

    public <T extends Enum<T>> ConfigValuePacket(Setting<T> setting, IConfigManager configManager) {
        this(setting, setting.getValue(configManager));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleOnClient(Player player) {
        if (player.containerMenu instanceof IConfigurableObject configurableObject) {
            loadSetting(configurableObject);
        }
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof IConfigurableObject configurableObject) {
            loadSetting(configurableObject);
        }
    }

    private void loadSetting(IConfigurableObject configurableObject) {
        var cm = configurableObject.getConfigManager();

        for (var setting : cm.getSettings()) {
            if (setting.getName().equals(this.name)) {
                setting.setFromString(cm, value);
                break;
            }
        }
    }

}
