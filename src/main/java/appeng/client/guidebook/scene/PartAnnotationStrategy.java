package appeng.client.guidebook.scene;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.client.guidebook.color.SymbolicColor;
import appeng.client.guidebook.scene.annotation.InWorldBoxAnnotation;
import appeng.client.guidebook.scene.annotation.SceneAnnotation;
import appeng.client.guidebook.scene.level.GuidebookLevel;
import appeng.core.localization.GuiText;
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
            var facade = partResult.facade;
            AABB aabb = null;
            Component description = Component.empty();
            if (part != null) {
                aabb = getAABB(partResult.side, partResult.part::getBoxes);
                description = partResult.part.getPartItem().asItem().getDescription();
            } else if (facade != null) {
                aabb = getAABB(partResult.side, bch -> partResult.facade.getBoxes(bch, false));
                description = GuiText.Facade.text(partResult.facade.getItem().getDescription());
            }

            if (aabb != null) {
                var annotation = new InWorldBoxAnnotation(
                        new Vector3f(pos.getX() + (float) aabb.minX, pos.getY() + (float) aabb.minY,
                                pos.getZ() + (float) aabb.minZ),
                        new Vector3f(pos.getX() + (float) aabb.maxX, pos.getY() + (float) aabb.maxY,
                                pos.getZ() + (float) aabb.maxZ),
                        SymbolicColor.IN_WORLD_BLOCK_HIGHLIGHT);
                annotation.setTooltipContent(description);
                return annotation;
            }
        }
        return null;
    }

    @Nullable
    private AABB getAABB(Direction side, Consumer<IPartCollisionHelper> collisionHelper) {
        var boxes = new ArrayList<AABB>();
        var bch = new BusCollisionHelper(boxes, side, true);
        collisionHelper.accept(bch);
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
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
