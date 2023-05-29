package appeng.client.guidebook.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
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

import appeng.client.guidebook.document.LytPoint;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.scene.annotation.InWorldAnnotation;
import appeng.client.guidebook.scene.annotation.OverlayAnnotation;
import appeng.client.guidebook.scene.annotation.SceneAnnotation;
import appeng.client.guidebook.scene.level.GuidebookLevel;

public class GuidebookScene {

    private final GuidebookLevel level;

    private final CameraSettings cameraSettings;

    private final List<InWorldAnnotation> inWorldAnnotations = new ArrayList<>();
    private final List<OverlayAnnotation> overlayAnnotations = new ArrayList<>();

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

        // Account for highlights
        for (var highlight : inWorldAnnotations) {
            var bounds = highlight.getScreenBounds(viewMatrix);
            min.x = Math.min(min.x, bounds.getLeft().x);
            min.y = Math.min(min.y, bounds.getLeft().y);
            max.x = Math.max(max.x, bounds.getRight().x);
            max.y = Math.max(max.y, bounds.getRight().y);
        }

        return new Vector4f(
                min.x,
                min.y,
                max.x,
                max.y);
    }

    public Vector2f worldToScreen(float x, float y, float z) {
        var viewMatrix = cameraSettings.getViewMatrix();
        var projectionMatrix = cameraSettings.getProjectionMatrix();

        Vector3f screenPos = new Vector3f();
        viewMatrix.transformPosition(x, y, z, screenPos);
        projectionMatrix.transformProject(screenPos);
        return new Vector2f(screenPos.x, screenPos.y);
    }

    private static Vector2f worldToScreen(Matrix4f viewMatrix, Matrix4f projectionMatrix, float x, float y, float z) {
        Vector3f screenPos = new Vector3f();
        viewMatrix.transformPosition(x, y, z, screenPos);
        projectionMatrix.transformProject(screenPos);
        /*
         * var screenX = this.bounds.x() + (screenPos.x + 1) * this.bounds.width() / 2; var screenY =
         * this.bounds.bottom() - (screenPos.y + 1) * this.bounds.height() / 2; return new Vector2f(screenX, screenY);/*
         */
        return new Vector2f();
    }

    private void buildPickRay(float screenX, float screenY, Vector3f rayOrigin, Vector3f rayDir) {
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

    }

    /**
     * Picks a scene annotation given the document point and viewport. Some annotations render as overlays and don't
     * actually have dimensions in the 3d scene, making it necessary to know the viewport.
     */
    @Nullable
    public SceneAnnotation pickAnnotation(LytPoint point, LytRect viewport) {
        var screenPos = documentToScreen(viewport, point);

        SceneAnnotation annotation = pickOverlayAnnotation(point, viewport);

        if (annotation == null) {
            annotation = pickInWorldAnnotation(screenPos.x, screenPos.y);
        }

        return annotation;
    }

    @Nullable
    public OverlayAnnotation pickOverlayAnnotation(LytPoint point, LytRect viewport) {
        for (int i = overlayAnnotations.size() - 1; i >= 0; i--) {
            var annotation = overlayAnnotations.get(i);
            var bounds = annotation.getBoundingRect(this, viewport);
            if (bounds.contains(point)) {
                return annotation;
            }
        }
        return null;
    }

    @Nullable
    public InWorldAnnotation pickInWorldAnnotation(float screenX, float screenY) {
        // Check overlay annotations first

        var rayOrigin = new Vector3f();
        var rayDir = new Vector3f();
        buildPickRay(screenX, screenY, rayOrigin, rayDir);

        float pickDistance = Float.POSITIVE_INFINITY;
        InWorldAnnotation pickedBox = null;
        for (var highlight : inWorldAnnotations) {
            var intersectionDist = highlight.intersect(rayOrigin, rayDir);
            if (intersectionDist.isPresent()) {
                if (intersectionDist.getAsDouble() < pickDistance) {
                    pickDistance = (float) intersectionDist.getAsDouble();
                    pickedBox = highlight;
                }
            }
        }

        return pickedBox;
    }

    public BlockHitResult pickBlock(LytPoint point, LytRect viewport) {
        var screenPos = documentToScreen(viewport, point);

        var rayOrigin = new Vector3f();
        var rayDir = new Vector3f();
        buildPickRay(screenPos.x, screenPos.y, rayOrigin, rayDir);

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

    /**
     * The camera settings affect layout so this should be called before layout is done (or relayout should be
     * triggered).
     */
    public CameraSettings getCameraSettings() {
        return cameraSettings;
    }

    public void clearAnnotations() {
        inWorldAnnotations.clear();
        overlayAnnotations.clear();
    }

    public void addAnnotation(SceneAnnotation annotation) {
        if (annotation instanceof InWorldAnnotation inWorldAnnotation) {
            inWorldAnnotations.add(inWorldAnnotation);
        }
        if (annotation instanceof OverlayAnnotation overlayAnnotation) {
            overlayAnnotations.add(overlayAnnotation);
        }
    }

    public void removeAnnotation(SceneAnnotation annotation) {
        if (annotation instanceof InWorldAnnotation) {
            inWorldAnnotations.remove(annotation);
        }
        if (annotation instanceof OverlayAnnotation) {
            overlayAnnotations.remove(annotation);
        }
    }

    public Collection<InWorldAnnotation> getInWorldAnnotations() {
        return inWorldAnnotations;
    }

    public Collection<OverlayAnnotation> getOverlayAnnotations() {
        return overlayAnnotations;
    }

    /**
     * Transforms from document coordinates (layout coordinate system) to coordinates in the screen space used by the
     * scene.
     */
    public Vector2f documentToScreen(LytRect viewport, LytPoint documentPoint) {
        var localX = (documentPoint.x() - viewport.x()) / viewport.width() * 2 - 1;
        var localY = -((documentPoint.y() - viewport.y()) / viewport.height() * 2 - 1);
        return new Vector2f(localX, localY);
    }

    /**
     * Transforms from document coordinates (layout coordinate system) to coordinates in the screen space used by the
     * scene.
     */
    public LytPoint screenToDocument(LytRect viewport, Vector2f screen) {
        var x = viewport.x() + (screen.x + 1) / 2f * viewport.width();
        var y = viewport.y() + (-screen.y + 1) / 2f * viewport.height();
        return new LytPoint(x, y);
    }
}
