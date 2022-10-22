package appeng.siteexport;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Path;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;

public class OffScreenRenderer implements AutoCloseable {
    private final NativeImage nativeImage;
    private final TextureTarget fb;
    private final int width;
    private final int height;

    public OffScreenRenderer(int width, int height) {
        this.width = width;
        this.height = height;
        nativeImage = new NativeImage(width, height, true);
        fb = new TextureTarget(width, height, true /* with depth */, true /* check error */);
        fb.setClearColor(0, 0, 0, 0);
        fb.clear(true /* check error */);
    }

    @Override
    public void close() {
        nativeImage.close();
        fb.destroyBuffers();
    }

    public void captureAsPng(Runnable r, Path path) throws IOException {
        fb.bindWrite(true);
        GlStateManager._clear(GL12.GL_COLOR_BUFFER_BIT | GL12.GL_DEPTH_BUFFER_BIT, false);
        r.run();
        fb.unbindWrite();

        // Load the framebuffer back into CPU memory
        fb.bindRead();
        nativeImage.downloadTexture(0, false);
        nativeImage.flipY();
        fb.unbindRead();

        nativeImage.writeToFile(path);
    }

    public void setupItemRendering() {
        // Set up GL state for GUI rendering where the 16x16 item will fill the entire framebuffer
        RenderSystem.setProjectionMatrix(
                new Matrix4f().ortho(0, 16, 0, 16, 1000, 3000));

        var poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();
        poseStack.translate(0.0F, 0.0F, -2000.0F);
        Lighting.setupForFlatItems();

        RenderSystem.applyModelViewMatrix();
        FogRenderer.setupNoFog();
    }

    public void setupOrtographicRendering() {
        float angle = 36;
        float renderHeight = 0;
        float renderScale = 100;
        float rotation = 45;

        // Set up GL state for GUI rendering where the 16x16 item will fill the entire framebuffer
        RenderSystem.setProjectionMatrix(
                new Matrix4f().ortho(-1, 1, 1, -1, 1000, 3000));

        var poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();
        poseStack.translate(0.0F, 0.0F, -2000.0F);

        FogRenderer.setupNoFog();

        poseStack.scale(1, -1, -1);
        poseStack.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * -180));

        Quaternionf flip = new Quaternionf().rotationZ(Mth.DEG_TO_RAD * 180);
        flip.mul(new Quaternionf().rotationX(Mth.DEG_TO_RAD * angle));

        poseStack.translate(0, (renderHeight / -300d), 0);
        poseStack.scale(renderScale * 0.004f, renderScale * 0.004f, 1f);

        Quaternionf rotate = new Quaternionf().rotationY(Mth.DEG_TO_RAD * rotation);
        poseStack.mulPose(flip);
        poseStack.mulPose(rotate);

        RenderSystem.applyModelViewMatrix();
        Lighting.setupLevel(poseStack.last().pose());
    }

    public void setupPerspectiveRendering(float zoom, float fov, Vector3f eyePos, Vector3f lookAt) {
        float aspectRatio = (float) width / height;

        PoseStack projMat = new PoseStack();
        if (zoom != 1.0F) {
            projMat.scale(zoom, zoom, 1.0F);
        }

        projMat.mulPoseMatrix(new Matrix4f().perspective(fov, aspectRatio, 0.05F, 16));
        RenderSystem.setProjectionMatrix(projMat.last().pose());

        var poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();
        var vm = createViewMatrix(eyePos, lookAt);
        poseStack.mulPoseMatrix(vm);

        RenderSystem.applyModelViewMatrix();
        Lighting.setupLevel(poseStack.last().pose());
    }

    /**
     * This is in essence the same code as in gluLookAt, but it returns the resulting transformation matrix instead of
     * applying it to the deprecated OpenGL transformation stack.
     */
    private static Matrix4f createViewMatrix(Vector3f eyePos, Vector3f lookAt) {
        Vector3f dir = new Vector3f(lookAt);
        dir.sub(eyePos);

        Vector3f up = new Vector3f(0, 1f, 0);
        dir.normalize();

        var right = new Vector3f(dir);
        right.cross(up);
        right.normalize();

        up = new Vector3f(right);
        up.cross(dir);
        up.normalize();

        var viewMatrix = new Matrix4f();
        viewMatrix.setTransposed(FloatBuffer.wrap(new float[] {
                right.x(),
                right.y(),
                right.z(),
                0.0f,

                up.x(),
                up.y(),
                up.z(),
                0.0f,

                -dir.x(),
                -dir.y(),
                -dir.z(),
                0.0f,

                0.0f,
                0.0f,
                0.0f,
                1.0f,
        }));

        viewMatrix.translate(-eyePos.x(), -eyePos.y(), -eyePos.z());
        return viewMatrix;
    }

}
