
package appeng.core.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.AELog;
import appeng.core.network.ServerboundPacket;
import appeng.menu.AEBaseMenu;
import appeng.util.EnumCycler;

public record ConfigButtonPacket(Setting<?> option, boolean rotationDirection) implements ServerboundPacket {
    public static ConfigButtonPacket decode(FriendlyByteBuf stream) {
        var option = Settings.getOrThrow(stream.readUtf());
        var rotationDirection = stream.readBoolean();
        return new ConfigButtonPacket(option, rotationDirection);
    }

    @Override
    public void write(FriendlyByteBuf data) {
        data.writeUtf(option.getName());
        data.writeBoolean(rotationDirection);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof AEBaseMenu baseMenu) {
            if (baseMenu.getTarget() instanceof IConfigurableObject configurableObject) {
                var cm = configurableObject.getConfigManager();
                if (cm.hasSetting(option)) {
                    cycleSetting(cm, option);
                } else {
                    AELog.info("Ignoring unsupported setting %s sent by client on %s", option,
                            baseMenu.getTarget());
                }
            }
        }
    }

    private <T extends Enum<T>> void cycleSetting(IConfigManager cm, Setting<T> setting) {
        var currentValue = cm.getSetting(setting);
        var nextValue = EnumCycler.rotateEnum(currentValue, rotationDirection, setting.getValues());
        cm.putSetting(setting, nextValue);
    }
}
