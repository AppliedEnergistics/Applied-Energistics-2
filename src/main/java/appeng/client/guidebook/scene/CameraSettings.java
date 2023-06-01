package appeng.client.guidebook.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import net.minecraft.util.Mth;

public class CameraSettings {

    private float zoom = 1;

    private final Vector3f center = new Vector3f();

    private final Vector4f viewport = new Vector4f();

    private final Matrix4f baseViewMatrix = new Matrix4f();

    private final Vector3f rotationCenter = new Vector3f();
    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float offsetX;
    private float offsetY;

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
            case UP -> {
                baseViewMatrix.rotate(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 120));
                baseViewMatrix.rotate(new Quaternionf().rotationZ(Mth.DEG_TO_RAD * 45));
            }
        }
    }

    public void setIsometricYawPitchRoll(float yawDeg, float pitchDeg, float rollDeg) {
        baseViewMatrix.identity();

        if (Math.abs(rollDeg) >= 0.1f) {
            baseViewMatrix.rotate(new Quaternionf().rotationZ(Mth.DEG_TO_RAD * rollDeg));
        }
        if (Math.abs(pitchDeg) >= 0.1f) {
            baseViewMatrix.rotate(new Quaternionf().rotationX(Mth.DEG_TO_RAD * pitchDeg));
        }
        if (Math.abs(yawDeg) >= 0.1f) {
            baseViewMatrix.rotate(new Quaternionf().rotationY(Mth.DEG_TO_RAD * yawDeg));
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

        result.translate(offsetX, offsetY, 0);

        var rotation = new Quaternionf().rotationYXZ(Mth.DEG_TO_RAD * rotationX, Mth.DEG_TO_RAD * rotationY,
                Mth.DEG_TO_RAD * rotationZ);
        result.rotateAround(rotation, rotationCenter.x, rotationCenter.y, rotationCenter.z);

        // 0.625f comes from the default block model json GUI transform
        result.scale(0.625f * 16 * zoom, 0.625f * 16 * zoom, 0.625f * 16 * zoom);

        result.mul(baseViewMatrix);

        result.translate(-center.x, -center.y, -center.z);

        return result;
    }

    public Matrix4f getProjectionMatrix() {
        var projectionMatrix = new Matrix4f();
        projectionMatrix.ortho(viewport.x, viewport.z, viewport.y, viewport.w, -1000, 3000);
        return projectionMatrix;
    }

    public float getRotationX() {
        return rotationX;
    }

    public void setRotationX(float rotationX) {
        this.rotationX = rotationX;
    }

    public float getRotationY() {
        return rotationY;
    }

    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public void setRotationZ(float rotationZ) {
        this.rotationZ = rotationZ;
    }

    public void setRotationCenter(Vector3fc rotationCenter) {
        this.rotationCenter.set(rotationCenter);
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }
}
