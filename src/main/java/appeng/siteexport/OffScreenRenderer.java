package appeng.siteexport;

import java.io.IOException;
import java.nio.file.Path;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import org.lwjgl.opengl.GL12;

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
                Matrix4f.orthographic(0, 16, 0, 16, 1000, 3000));

        var poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();
        poseStack.translate(0.0F, 0.0F, -2000.0F);
        Lighting.setupFor3DItems();

        RenderSystem.applyModelViewMatrix();
    }

    public void setupOrtographicRendering() {
        float angle = 36;
        float renderHeight = 0;
        float renderScale = 48;
        float rotation = 45;

        var poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();

        poseStack.scale(1, -1, -1);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-180));

        Quaternion flip = Vector3f.ZP.rotationDegrees(180);
        flip.mul(Vector3f.XP.rotationDegrees(angle));

        poseStack.translate(0, (renderHeight / -300d), 0);
        poseStack.scale(renderScale * 0.004f, renderScale * 0.004f, 1f);

        Quaternion rotate = Vector3f.YP.rotationDegrees(rotation);
        poseStack.mulPose(flip);
        poseStack.mulPose(rotate);
    }

    public void setupPerspectiveRendering(float zoom, float fov, Vector3f eyePos, Vector3f lookAt) {
        float aspectRatio = (float) width / height;

        PoseStack projMat = new PoseStack();
        if (zoom != 1.0F) {
            projMat.scale(zoom, zoom, 1.0F);
        }

        projMat.mulPoseMatrix(Matrix4f.perspective(fov, aspectRatio, 0.05F, 16));
        RenderSystem.setProjectionMatrix(projMat.last().pose());

        var poseStack = RenderSystem.getModelViewStack();
        poseStack.setIdentity();
        var vm = setCamera(eyePos.x(), eyePos.y(), eyePos.z(), lookAt.x(), lookAt.y(), lookAt.z());
        poseStack.mulPoseMatrix(vm);

        RenderSystem.applyModelViewMatrix();
        Lighting.setupLevel(poseStack.last().pose());
    }

    public Matrix4f gluLookAt(Vector3f eye, Vector3f center, Vector3f up) {
        Vector3f forward = center.copy();
        forward.sub(eye);
        forward.normalize();
        Vector3f side = forward.copy();
        side.cross(up);
        side.normalize();
        up = side.copy();
        up.cross(forward);

        Matrix4f result = new Matrix4f(new float[] {
                side.x(),
                side.y(),
                side.z(),
                0,
                up.x(),
                up.y(),
                up.z(),
                0,
                -forward.x(),
                -forward.y(),
                -forward.z(),
                0,
                0,
                0,
                0,
                1
        });
        return result;
    }

    private static Matrix4f setCamera(float posX, float posY, float posZ,
            float lookAtX, float lookAtY, float lookAtZ) {
        Vector3f dir = new Vector3f(lookAtX - posX, lookAtY - posY, lookAtZ - posZ);
        Vector3f up = new Vector3f(0, 1f, 0);
        dir.normalize();

        var right = dir.copy();
        right.cross(up);
        right.normalize();

        up = right.copy();
        up.cross(dir);
        up.normalize();

        var viewMatrix = new Matrix4f(new float[] {
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
        });

        // setup aux as a translation matrix by placing positions in the last column
//        var aux = Matrix4f.createTranslateMatrix(-posX, -posY, -posZ);

        // multiplication(in fact translation) viewMatrix with aux
        viewMatrix.multiplyWithTranslation(-posX, -posY, -posZ);
        return viewMatrix;
    }

}
