package appeng.client.guidebook.scene;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

import appeng.client.guidebook.scene.level.GuidebookLevel;

public class GuidebookScene {

    private final GuidebookLevel level;

    private final CameraSettings cameraSettings;

    private final Map<BlockPos, BlockHighlight> highlights = new HashMap<>();

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
                                tmpPos);
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
                max.y);
    }

    public Vector2f worldToScreen(Matrix4f viewMatrix, Matrix4f projectionMatrix, float x, float y, float z) {
        Vector3f screenPos = new Vector3f();
        viewMatrix.transformPosition(x, y, z, screenPos);
        projectionMatrix.transformProject(screenPos);
        /*
         * var screenX = this.bounds.x() + (screenPos.x + 1) * this.bounds.width() / 2; var screenY =
         * this.bounds.bottom() - (screenPos.y + 1) * this.bounds.height() / 2; return new Vector2f(screenX, screenY);/*
         */
        return new Vector2f();
    }

    public BlockHitResult pickBlock(float screenX, float screenY) {
        var rayOrigin = new Vector3f();
        var rayDir = new Vector3f();

        var viewProj = new Matrix4f(cameraSettings.getProjectionMatrix());
        viewProj.mul(cameraSettings.getViewMatrix());
        viewProj.unprojectRay(
                screenX, screenY,
                // We already expect normalized device coordinates,
                // so the viewport is set in such a way as to leave the coordinates alone
                new int[] {
                        -1, -1,
                        2, 2
                },
                rayOrigin,
                rayDir);

        var levelBounds = level.getBounds();
        var intersection = new Vector2f();
        if (!Intersectionf.intersectRayAab(
                rayOrigin,
                rayDir,
                new Vector3f(levelBounds.min().getX(), levelBounds.min().getY(), levelBounds.min().getZ()),
                new Vector3f(levelBounds.max().getX(), levelBounds.max().getY(), levelBounds.max().getZ()),
                intersection)) {
            return BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
        }

        // Move the ray such that the start and end are on the bounding box of the content
        var start = new Vector3f(rayDir).mulAdd(intersection.x, rayOrigin);
        var end = new Vector3f(rayDir).mulAdd(intersection.y, rayOrigin);

        var fromVec3 = new Vec3(start);
        var toVec3 = new Vec3(end);
        var blockClipContext = ClipContext.Block.OUTLINE;
        var fluidClipContext = ClipContext.Fluid.ANY;
        return BlockGetter.traverseBlocks(fromVec3, toVec3, null, (ignored, blockPos) -> {
            BlockState blockState = level.getBlockState(blockPos);
            FluidState fluidState = level.getFluidState(blockPos);

            var blockShape = blockClipContext.get(blockState, level, blockPos, CollisionContext.empty());
            var blockHit = level.clipWithInteractionOverride(fromVec3, toVec3, blockPos, blockShape, blockState);

            var fluidShape = fluidClipContext.canPick(fluidState) ? fluidState.getShape(level, blockPos)
                    : Shapes.empty();
            var fluidHit = fluidShape.clip(fromVec3, toVec3, blockPos);

            double blockDist = blockHit == null ? Double.MAX_VALUE : fromVec3.distanceToSqr(blockHit.getLocation());
            double fluidDist = fluidHit == null ? Double.MAX_VALUE : fromVec3.distanceToSqr(fluidHit.getLocation());
            return blockDist <= fluidDist ? blockHit : fluidHit;
        }, ignored -> {
            Vec3 vec3 = fromVec3.subtract(toVec3);
            return BlockHitResult.miss(toVec3, Direction.getNearest(vec3.x, vec3.y, vec3.z),
                    BlockPos.containing(toVec3));
        });
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

    public void clearHighlights() {
        highlights.clear();
    }

    public void addHighlight(BlockHighlight highlight) {
        highlights.put(highlight.pos(), highlight);
    }

    public void removeHighlight(BlockPos pos) {
        highlights.remove(pos);
    }

    public Collection<BlockHighlight> getHighlights() {
        return highlights.values();
    }
}
