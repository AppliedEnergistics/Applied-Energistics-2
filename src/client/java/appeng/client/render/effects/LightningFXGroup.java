package appeng.client.render.effects;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;
import net.minecraft.util.Mth;

import appeng.client.render.AERenderTypes;

public class LightningFXGroup extends ParticleGroup<LightningFX> {
    public static ParticleRenderType GROUP = new ParticleRenderType("AE2_LIGHTNING");

    public LightningFXGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
        var states = new ArrayList<LightningFX.ParticleState>(particles.size());
        for (var particle : particles) {
            states.add(particle.extract(partialTick, camera.position()));
        }
        return new State(states);
    }

    private static class State implements ParticleGroupRenderState {
        private final List<LightningFX.ParticleState> particles;

        public State(List<LightningFX.ParticleState> particles) {
            this.particles = particles;
        }

        @Override
        public void submit(SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
            nodeCollector.submitCustomGeometry(new PoseStack(), AERenderTypes.LIGHTNING_FX, (pose, consumer) -> {
                for (var particle : particles) {
                    renderParticle(consumer, particle);
                }
            });
        }

        @Override
        public void clear() {
            particles.clear();
        }

        private void renderParticle(VertexConsumer consumer, LightningFX.ParticleState particle) {

            float u = particle.u();
            float v = particle.v();

            float ox = 0;
            float oy = 0;
            float oz = 0;

            // The lightning segments are filled with a lighter color on the inside,
            // and a darker color on the outside edge. This is done by tessellating
            // two differently sized/colored quads on top of each other.
            for (int layer = 0; layer < 2; layer++) {
                float scale;
                float red;
                float green;
                float blue;
                float alpha = 0.7f;
                if (layer == 0) {
                    scale = 0.04f;
                    red = 0.4f;
                    green = 0.25f;
                    blue = 0.45f;
                } else {
                    scale = 0.02f;
                    red = 0.9f;
                    green = 0.65f;
                    blue = 0.85f;
                }

                for (int cycle = 0; cycle < 3; cycle++) {
                    float x = particle.centerX();
                    float y = particle.centerY();
                    float z = particle.centerZ();

                    float prevOuterX = 0, prevOuterY = 0, prevOuterZ = 0;
                    float prevX = 0, prevY = 0, prevZ = 0;
                    for (int s = 0; s < LightningFX.SEGMENTS; s++) {
                        final float xN = x + particle.precomputedSteps()[s * 3 + 0];
                        final float yN = y + particle.precomputedSteps()[s * 3 + 1];
                        final float zN = z + particle.precomputedSteps()[s * 3 + 2];

                        final float xD = xN - x;
                        final float yD = yN - y;
                        final float zD = zN - z;

                        if (cycle == 0) {
                            ox = yD * 0 - 1 * zD;
                            oy = zD * 0 - 0 * xD;
                            oz = xD * 1 - 0 * yD;
                        } else if (cycle == 1) {
                            ox = yD * 1 - 0 * zD;
                            oy = zD * 0 - 1 * xD;
                            oz = xD * 0 - 0 * yD;
                        } else if (cycle == 2) {
                            ox = yD * 0 - 0 * zD;
                            oy = zD * 1 - 0 * xD;
                            oz = xD * 0 - 1 * yD;
                        }

                        float ss = Mth.sqrt(ox * ox + oy * oy + oz * oz)
                                / (((float) LightningFX.SEGMENTS - (float) s) / LightningFX.SEGMENTS * scale);
                        ox /= ss;
                        oy /= ss;
                        oz /= ss;

                        var outerX = x + ox;
                        var outerY = y + oy;
                        var outerZ = z + oz;

                        if (s > 0) {
                            consumer.addVertex(outerX, outerY, outerZ).setUv(u, v).setColor(red, green, blue, alpha)
                                    .setUv2(LightningFX.BRIGHTNESS, LightningFX.BRIGHTNESS);
                            consumer.addVertex(prevOuterX, prevOuterY, prevOuterZ).setUv(u, v)
                                    .setColor(red, green, blue, alpha)
                                    .setUv2(LightningFX.BRIGHTNESS, LightningFX.BRIGHTNESS);
                            consumer.addVertex(prevX, prevY, prevZ).setUv(u, v).setColor(red, green, blue, alpha)
                                    .setUv2(LightningFX.BRIGHTNESS, LightningFX.BRIGHTNESS);
                            consumer.addVertex(x, y, z).setUv(u, v).setColor(red, green, blue, alpha)
                                    .setUv2(LightningFX.BRIGHTNESS, LightningFX.BRIGHTNESS);
                        }

                        prevOuterX = outerX;
                        prevOuterY = outerY;
                        prevOuterZ = outerZ;
                        prevX = x;
                        prevY = y;
                        prevZ = z;

                        x = xN;
                        y = yN;
                        z = zN;
                    }
                }
            }
        }
    }
}
