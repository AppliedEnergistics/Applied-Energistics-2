package appeng.client.renderer.spatialstorage;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;

public class SpatialStorageWeatherEffectsRenderer implements CustomWeatherEffectRenderer {
    @Override
    public boolean renderSnowAndRain(LevelRenderState levelRenderState, WeatherRenderState weatherRenderState,
            MultiBufferSource bufferSource, Vec3 camPos) {
        return true; // Skip rendering Vanilla rain
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        return true; // Skip ticking Vanilla rain
    }
}
