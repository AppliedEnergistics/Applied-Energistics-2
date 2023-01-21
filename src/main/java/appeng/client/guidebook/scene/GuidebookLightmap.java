package appeng.client.guidebook.scene;

import appeng.core.AppEng;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * A version of {@link LightTexture} which is independent of the current client level.
 */
public class GuidebookLightmap implements AutoCloseable {
    private final DynamicTexture lightmapTexture;
    private final NativeImage lightmapPixels;

    public GuidebookLightmap() {
        lightmapTexture = new DynamicTexture(16, 16, false);
        lightmapPixels = Objects.requireNonNull(lightmapTexture.getPixels());
        lightmapPixels.fillRect(0, 0, 16, 16, -1);
        lightmapTexture.upload();
    }

    public float getSkyDarken(Level level, float partialTick) {
        var f = level.getTimeOfDay(partialTick);
        var g = 1.0F - (Mth.cos(f * (float) (Math.PI * 2)) * 2.0F + 0.2F);
        g = Mth.clamp(g, 0.0F, 1.0F);
        g = 1.0F - g;
        return 0.2f + g * 0.8f;
    }

    public void update(Level level) {
        float f = getSkyDarken(level, 1.0f);
        Vector3f vector3f = new Vector3f(f, f, 1.0F).lerp(new Vector3f(1.0F, 1.0F, 1.0F), 0.35F);
        float g = f * 0.95F + 0.05F;
        float m = 1.5F;

        var minecraft = Minecraft.getInstance();

        Vector3f vector3f2 = new Vector3f();

        for (int skyLightLvl = 0; skyLightLvl < 16; ++skyLightLvl) {
            for (int blockLightLvl = 0; blockLightLvl < 16; ++blockLightLvl) {
                float p = LightTexture.getBrightness(level.dimensionType(), skyLightLvl) * g;
                float q = LightTexture.getBrightness(level.dimensionType(), blockLightLvl) * m;

                float s = q * ((q * 0.6F + 0.4F) * 0.6F + 0.4F);
                float t = q * (q * q * 0.6F + 0.4F);
                vector3f2.set(q, s, t);

                Vector3f vector3f3 = new Vector3f(vector3f).mul(p);
                vector3f2.add(vector3f3);
                vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);

                float v = minecraft.options.gamma().get().floatValue();
                Vector3f vector3f5 = new Vector3f(notGamma(vector3f2.x), notGamma(vector3f2.y), notGamma(vector3f2.z));
                vector3f2.lerp(vector3f5, Math.max(0.0F, v));
                vector3f2.lerp(new Vector3f(0.75F, 0.75F, 0.75F), 0.04F);
                clampColor(vector3f2);
                vector3f2.mul(255.0F);

                int x = (int) vector3f2.x();
                int y = (int) vector3f2.y();
                int z = (int) vector3f2.z();
                lightmapPixels.setPixelRGBA(blockLightLvl, skyLightLvl, 0xFF000000 | z << 16 | y << 8 | x);
            }
        }

        lightmapTexture.upload();
    }

    private static void clampColor(Vector3f vector3f) {
        vector3f.set(Mth.clamp(vector3f.x, 0.0F, 1.0F), Mth.clamp(vector3f.y, 0.0F, 1.0F), Mth.clamp(vector3f.z, 0.0F, 1.0F));
    }

    private float notGamma(float value) {
        float f = 1.0F - value;
        return 1.0F - f * f * f * f;
    }

    public void bind() {
        RenderSystem.setShaderTexture(2, lightmapTexture.getId());
        lightmapTexture.bind();
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
        RenderSystem.texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public void close() throws Exception {
        lightmapTexture.close();
    }
}
