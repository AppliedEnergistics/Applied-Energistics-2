package appeng.client.renderer.spatialstorage;

import com.mojang.renderpearl.api.commands.RenderPass;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;

public class SpatialStorageWeatherEffectsRenderer implements CustomWeatherEffectRenderer {
    @Override
    public boolean renderSnowAndRain(LevelRenderState levelRenderState, WeatherRenderState weatherRenderState,
            Vec3 camPos, RenderPass renderPass) {
        return true; // Skip rendering Vanilla rain
    }

    @Override
    public boolean renderSnowAndRainOit(LevelRenderState levelRenderState, WeatherRenderState weatherRenderState,
            Vec3 camPos, OitStage stage, RenderPass renderPass) {
        return true; // Skip rendering Vanilla rain
    }

    @Override
    public boolean tickRain(ClientLevel level, long ticks, Camera camera) {
        return true; // Skip ticking Vanilla rain
    }
}
