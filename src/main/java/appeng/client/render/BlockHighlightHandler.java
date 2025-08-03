package appeng.client.render;

import com.glodblock.github.glodium.client.render.ColorData;
import com.glodblock.github.glodium.client.render.highlight.HighlightHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class BlockHighlightHandler {
    private static final ColorData colorData = new ColorData(1f, 0f, 0f);

    public static void highlight(BlockPos pos, ResourceKey<Level> level, long time) {
        highlight(pos, null, level, time, new AABB(pos));
    }

    public static void highlight(BlockPos pos, Direction face, ResourceKey<Level> level, long time, AABB box) {
        HighlightHandler.highlight(pos, face, level, time, box, colorData, BlockHighlightHandler::blink);
    }

    private static boolean blink() {
        return ((System.currentTimeMillis() / 500) & 1) != 0;
    }

    public static long getTime(BlockPos be, BlockPos player) {
        return System.currentTimeMillis() + (long) (600 * be.distSqr(player));
    }
}
