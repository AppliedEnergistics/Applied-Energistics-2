package appeng.client.guidebook.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.util.Mth;

public class CameraSettings {

    private float zoom;

    private final Vector3f center = new Vector3f();

    private final Vector4f viewport = new Vector4f();

    private final Matrix4f baseViewMatrix = new Matrix4f();

    public Vector4f getViewport() {
        return viewport;
    }

    public void setViewport(Vector4f viewport) {
        this.viewport.set(viewport);
    }

    public CameraSettings() {
        setPerspectivePreset(PerspectivePreset.ISOMETRIC_NORTH_EAST);
    }

    public void setPerspectivePreset(PerspectivePreset preset) {
        baseViewMatrix.identity();
        switch (preset) {
            case ISOMETRIC_NORTH_EAST -> {
                baseViewMatrix.rotate(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 30));
                baseViewMatrix.rotate(new Quaternionf().rotationY(Mth.DEG_TO_RAD * 225));
            }
            case ISOMETRIC_NORTH_WEST -> {
                baseViewMatrix.rotate(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 30));
                baseViewMatrix.rotate(new Quaternionf().rotationY(Mth.DEG_TO_RAD * 135));
            }
        }
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getZoom() {
        return zoom;
    }

    public Matrix4f getViewMatrix() {
        var result = new Matrix4f();

        // 0.625f comes from the default block model json GUI transform
        result.scale(0.625f * 16 * zoom, 0.625f * 16 * zoom, 0.625f * 16 * zoom);

        result.mul(baseViewMatrix);

//        // Get the center and move the origin there
//        var bounds = level.getBounds();
//        var centerX = (bounds.max().getX() + bounds.min().getX()) / 2f;
//        var centerY = (bounds.max().getY() + bounds.min().getY()) / 2f;
//        var centerZ = (bounds.max().getZ() + bounds.min().getZ()) / 2f;
////        modelViewStack.mulPose(new Quaternionf().rotationY(
////                ((System.currentTimeMillis() % 6000) - 3000) / 3000f * Mth.PI
//        ));
        result.translate(-center.x, -center.y, -center.z);

        return result;
    }

    public Matrix4f getProjectionMatrix() {
        var projectionMatrix = new Matrix4f();
        projectionMatrix.ortho(viewport.x, viewport.z, viewport.y, viewport.w, -1000, 3000);
        return projectionMatrix;
    }
}
