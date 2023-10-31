package appeng.hooks;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class HighlightHandler {

    private static final PriorityQueue<HighlightData> BLOCK_QUEUE = new ObjectHeapPriorityQueue<>(
            Comparator.comparingLong(o -> o.time));
    private static final ObjectSet<HighlightData> BLOCKS = new ObjectOpenCustomHashSet<>(
            new Hash.Strategy<>() {
                @Override
                public int hashCode(HighlightData o) {
                    return o.dim.hashCode() ^ o.pos.hashCode();
                }

                @Override
                public boolean equals(HighlightData a, HighlightData b) {
                    return (a == b)
                            || (a != null && b != null && Objects.equals(a.dim, b.dim) && Objects.equals(a.pos, b.pos));
                }
            });

    public static void highlight(BlockPos pos, ResourceKey<Level> dim, long time) {
        var r = new HighlightData(pos, time, dim);
        if (!BLOCKS.contains(r)) {
            BLOCK_QUEUE.enqueue(r);
            BLOCKS.add(r);
        }
    }

    public static void expire() {
        if (BLOCK_QUEUE.isEmpty()) {
            return;
        }
        BLOCKS.remove(BLOCK_QUEUE.first());
        BLOCK_QUEUE.dequeue();
    }

    public static HighlightData getFirst() {
        if (BLOCK_QUEUE.isEmpty()) {
            return null;
        }
        return BLOCK_QUEUE.first();
    }

    public static Collection<HighlightData> getBlockData() {
        return BLOCKS;
    }

    public record HighlightData(BlockPos pos, long time, ResourceKey<Level> dim) {

        public boolean checkDim(ResourceKey<Level> dim) {
            if (dim == null || this.dim == null) {
                return false;
            }
            return dim.equals(this.dim);
        }

    }

}
