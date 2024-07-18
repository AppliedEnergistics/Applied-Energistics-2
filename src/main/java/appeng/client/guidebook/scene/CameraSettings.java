package appeng.client.guidebook.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import net.minecraft.util.Mth;

import appeng.client.guidebook.document.LytSize;

public class CameraSettings {

    private float zoom = 1;

    private final Vector4f viewport = new Vector4f();

    private Mode mode = Mode.ORTOGRAPHIC;

    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private final Vector3f rotationCenter = new Vector3f();
    private float offsetX;
    private float offsetY;
    private LytSize viewportSize = LytSize.empty();

    public void setViewportSize(LytSize size) {
        this.viewportSize = size;
        var halfWidth = size.width() / 2f;
        var halfHeight = size.height() / 2f;
        var renderViewport = new Vector4f(
                -halfWidth, -halfHeight, halfWidth, halfHeight);
        this.viewport.set(renderViewport);
    }

    public LytSize getViewportSize() {
        return viewportSize;
    }

    public CameraSettings() {
        setPerspectivePreset(PerspectivePreset.ISOMETRIC_NORTH_EAST);
    }

    public void setPerspectivePreset(PerspectivePreset preset) {
        switch (preset) {
            case ISOMETRIC_NORTH_EAST -> {
                setIsometricYawPitchRoll(225, 30, 0);
            }
            case ISOMETRIC_NORTH_WEST -> {
                setIsometricYawPitchRoll(135, 30, 0);
            }
            case UP -> {
                setIsometricYawPitchRoll(120, 0, 45);
            }
        }
    }

    public void setIsometricYawPitchRoll(float yawDeg, float pitchDeg, float rollDeg) {
        mode = Mode.ORTOGRAPHIC;
        rotationY = yawDeg;
        rotationX = pitchDeg;
        rotationZ = rollDeg;
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

        // 0.625f comes from the default block model json GUI transform
        result.scale(0.625f * 16 * zoom, 0.625f * 16 * zoom, 0.625f * 16 * zoom);

        if (mode == Mode.ORTOGRAPHIC) {
            result.translate(rotationCenter.x, rotationCenter.y, rotationCenter.z);
            result.rotateZ(Mth.DEG_TO_RAD * rotationZ);
            result.rotateX(Mth.DEG_TO_RAD * rotationX);
            result.rotateY(Mth.DEG_TO_RAD * rotationY);
            result.translate(-rotationCenter.x, -rotationCenter.y, -rotationCenter.z);
        }

        return result;
    }

    public Matrix4f getProjectionMatrix() {
        var projectionMatrix = new Matrix4f();
        projectionMatrix.setOrtho(
                viewport.x(),
                viewport.z(),
                viewport.y(),
                viewport.w(),
                -1000,
                3000);
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

    private enum Mode {
        ORTOGRAPHIC,
        PERSPECTIVE
    }

    public SavedCameraSettings save() {
        return new SavedCameraSettings(rotationX, rotationY, rotationZ, offsetX, offsetY, zoom);
    }

    public void restore(SavedCameraSettings settings) {
        rotationX = settings.rotationX();
        rotationY = settings.rotationY();
        rotationZ = settings.rotationZ();
        offsetX = settings.offsetX();
        offsetY = settings.offsetY();
        zoom = settings.zoom();
    }
}
