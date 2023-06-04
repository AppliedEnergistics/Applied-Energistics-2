package appeng.client.guidebook.scene;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.parts.IPartHost;
import appeng.client.guidebook.color.SymbolicColor;
import appeng.client.guidebook.scene.annotation.InWorldBoxAnnotation;
import appeng.client.guidebook.scene.annotation.SceneAnnotation;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import appeng.parts.BusCollisionHelper;

public class PartAnnotationStrategy implements ImplicitAnnotationStrategy {
    @Override
    public @Nullable SceneAnnotation getAnnotation(GuidebookLevel level, BlockState blockState,
            BlockHitResult blockHitResult) {
        var pos = blockHitResult.getBlockPos();
        var be = level.getBlockEntity(pos);
        if (!(be instanceof IPartHost partHost)) {
            return null;
        }

        var partResult = partHost.selectPartWorld(blockHitResult.getLocation());
        if (partResult != null) {
            var part = partResult.part;
            if (part != null) {
                List<AABB> boxes = new ArrayList<>();
                var bch = new BusCollisionHelper(boxes, partResult.side, true);
                part.getBoxes(bch);
                if (boxes.isEmpty()) {
                    return null;
                }

                // Find the outer bounding box
                var minX = Double.MAX_VALUE;
                var minY = Double.MAX_VALUE;
                var minZ = Double.MAX_VALUE;
                var maxX = Double.MIN_VALUE;
                var maxY = Double.MIN_VALUE;
                var maxZ = Double.MIN_VALUE;
                for (var box : boxes) {
                    minX = Math.min(minX, box.minX);
                    minY = Math.min(minY, box.minY);
                    minZ = Math.min(minZ, box.minZ);
                    maxX = Math.max(maxX, box.maxX);
                    maxY = Math.max(maxY, box.maxY);
                    maxZ = Math.max(maxZ, box.maxZ);
                }
                var annotation = new InWorldBoxAnnotation(
                        new Vector3f(pos.getX() + (float) minX, pos.getY() + (float) minY, pos.getZ() + (float) minZ),
                        new Vector3f(pos.getX() + (float) maxX, pos.getY() + (float) maxY, pos.getZ() + (float) maxZ),
                        SymbolicColor.IN_WORLD_BLOCK_HIGHLIGHT);
                annotation.setTooltipContent(part.getPartItem().asItem().getDescription());
                return annotation;
            }
        }
        return null;
    }
}
