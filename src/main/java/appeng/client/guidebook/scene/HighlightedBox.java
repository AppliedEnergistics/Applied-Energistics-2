package appeng.client.guidebook.scene;

import net.minecraft.core.BlockPos;
import org.joml.Vector3f;

public record HighlightedBox(Vector3f min, Vector3f max, int color) {

    public static HighlightedBox forBlock(BlockPos pos, int color) {
        return new HighlightedBox(
                new Vector3f(pos.getX(), pos.getY(), pos.getZ()),
                new Vector3f(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1),
                color
        );
    }

}
