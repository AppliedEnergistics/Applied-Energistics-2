package appeng.hooks;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import appeng.api.parts.CableRenderMode;
import appeng.client.AppEngClient;
import appeng.core.Api;
import appeng.hooks.ticking.PlayerColor;
import appeng.hooks.ticking.TickHandler;
import appeng.util.Platform;

@Environment(EnvType.CLIENT)
public class ClientTickHandler extends TickHandler {

    private final HashMap<Integer, PlayerColor> cliPlayerColors = new HashMap<>();
    private CableRenderMode crm = CableRenderMode.STANDARD;

    public ClientTickHandler() {
        ClientTickEvents.START_CLIENT_TICK.register(this::onBeforeClientTick);
    }

    @Override
    public Map<Integer, PlayerColor> getPlayerColors() {
        if (!Platform.isServer()) {
            return this.cliPlayerColors;
        }
        return super.getPlayerColors();
    }

    private void onBeforeClientTick(Minecraft client) {
        this.tickColors(this.cliPlayerColors);
        final CableRenderMode currentMode = Api.instance().partHelper().getCableRenderMode();
        if (currentMode != this.crm) {
            this.crm = currentMode;
            AppEngClient.instance().triggerUpdates();
        }
    }

}
