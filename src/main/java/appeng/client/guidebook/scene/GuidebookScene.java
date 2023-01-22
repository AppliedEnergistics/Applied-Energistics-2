package appeng.client.guidebook.scene;

import appeng.client.guidebook.scene.level.GuidebookLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.stream.Stream;

public class GuidebookScene {

    private final GuidebookLevel level;

    private final CameraSettings cameraSettings;

    public GuidebookScene(GuidebookLevel level, CameraSettings cameraSettings) {
        this.level = level;
        this.cameraSettings = cameraSettings;
    }

    public Vector4f getScreenBounds() {
        var viewMatrix = cameraSettings.getViewMatrix();

        // This is doing more work than needed since touching blocks create unneeded corners
        var tmpPos = new Vector3f();
        var min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        var max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        level.getFilledBlocks().forEach(pos -> {
            for (var xCorner = 0; xCorner <= 1; xCorner++) {
                for (var yCorner = 0; yCorner <= 1; yCorner++) {
                    for (var zCorner = 0; zCorner <= 1; zCorner++) {
                        viewMatrix.transformPosition(
                                pos.getX() + xCorner,
                                pos.getY() + yCorner,
                                pos.getZ() + zCorner,
                                tmpPos
                        );
                        min.min(tmpPos);
                        max.max(tmpPos);
                    }
                }
            }
        });
        return new Vector4f(
                min.x,
                min.y,
                max.x,
                max.y
        );
    }

    public Vector2f worldToScreen(Matrix4f viewMatrix, Matrix4f projectionMatrix, float x, float y, float z) {
        Vector3f screenPos = new Vector3f();
        viewMatrix.transformPosition(x, y, z, screenPos);
        projectionMatrix.transformProject(screenPos);
        /*var screenX = this.bounds.x() + (screenPos.x + 1) * this.bounds.width() / 2;
        var screenY = this.bounds.bottom() - (screenPos.y + 1) * this.bounds.height() / 2;
        return new Vector2f(screenX, screenY);/*
        */
        return new Vector2f();
    }

    private BlockHitResult pickBlock(Matrix4f viewMatrix, Matrix4f projectionMatrix, float screenX, float screenY) {
       /* var rayOrigin = new Vector3f();
        var rayDir = new Vector3f();

        var viewProj = new Matrix4f(projectionMatrix);
        viewProj.mul(viewMatrix);
        viewProj.unprojectRay(
                screenX - bounds.x(), bounds.bottom() - screenY,
                new int[]{
                        0, 0,
                        bounds.width(), bounds.height()
                },
                rayOrigin,
                rayDir
        );

        var levelBounds = level.getBounds();
        var intersection = new Vector2f();
        if (!Intersectionf.intersectRayAab(
                rayOrigin,
                rayDir,
                new Vector3f(levelBounds.min().getX(), levelBounds.min().getY(), levelBounds.min().getZ()),
                new Vector3f(levelBounds.max().getX(), levelBounds.max().getY(), levelBounds.max().getZ()),
                intersection
        )) {
            return BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
        }

        // Move the ray such that the start and end are on the bounding box of the content
        var start = new Vector3f(rayDir).mulAdd(intersection.x, rayOrigin);
        var end = new Vector3f(rayDir).mulAdd(intersection.y, rayOrigin);

        return this.level.clip(
                new ClipContext(new Vec3(start), new Vec3(end), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, Minecraft.getInstance().player)
        );*/
        return BlockHitResult.miss(null, null, null);
    }


    public Stream<BlockPos> getFilledBlocks() {
        return level.getFilledBlocks();
    }

    public GuidebookLevel getLevel() {
        return level;
    }

    public CameraSettings getCameraSettings() {
        return cameraSettings;
    }
}
