package appeng.client.render;

import appeng.client.render.tesr.CellLedRenderer;
import appeng.parts.IndicatorState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;

public final class PartIndicatorLightRenderer {
    private PartIndicatorLightRenderer() {
    }

    public static void renderIndicatorLights(float[] positions,
                                             IndicatorState state,
                                             double animationTicks,
                                             PoseStack stack,
                                             MultiBufferSource buffers) {
        var consumer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);

        var r = state.getRed();
        var g = state.getGreen();
        var b = state.getBlue();
        var a = 255;

        if (state == IndicatorState.BOOTING) {
            r = 127;
            g = 127;
            b = 127;
        }

        if (state == IndicatorState.BOOTING) {
            if ((((long) animationTicks / 10) % 2) != 0) {
                r /= 2;
                g /= 2;
                b /= 2;
            }

        } else if (state == IndicatorState.MISSING_CHANNEL || state.isBlinking()) {
//            var tick = (gameTime + Math.abs((short) seed)) + partialTick;
            var t = Mth.abs((float) (animationTicks % 120 - 60) / 60.0f);
            var f = Mth.lerp(easeInOutCubic(t), 0.45f, 1.0f);
            r *= f;
            g *= f;
            b *= f;
        }

        var v = new Vector4f();
        var translation = stack.last().pose();
        for (int i = 0; i < positions.length; i += 3) {
            var x = positions[i];
            var y = positions[i + 1];
            var z = positions[i + 2];
            v.set(x, y, z, 1);
            v.transform(translation);
            consumer.vertex(translation, x, y, z)
                    .color(r, g, b, a)
                    .endVertex();
        }
    }

    private static float easeInOutCubic(float x) {
        return x < 0.5f ? 4 * x * x * x : 1 - (float) Math.pow(-2 * x + 2, 3) / 2;
    }
}
