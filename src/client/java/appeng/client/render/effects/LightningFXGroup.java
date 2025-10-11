package appeng.client.render.effects;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;

public class LightningFXGroup extends ParticleGroup<LightningFX> {
    public static ParticleRenderType RENDER_TYPE = new ParticleRenderType("AE2_LIGHTNING");

    public LightningFXGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
        return null;
    }

    public static class RenderState implements ParticleGroupRenderState {
        @Override
        public void submit(SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {

        }

        @Override
        public void clear() {
        }
    }
}
