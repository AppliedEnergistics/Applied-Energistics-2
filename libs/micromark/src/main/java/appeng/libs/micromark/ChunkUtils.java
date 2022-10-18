package appeng.libs.micromark;

import java.util.List;

public final class ChunkUtils {
    private ChunkUtils() {
    }

    /**
     * This does not return the removed items, and takes `items` as an
     * array instead of rest parameters.
     */
    public static <T> void splice(List<T> list, int start, int remove, List<T> items) {
        var removeEnd = Math.min(list.size(), start + remove);
        var sublist = list.subList(start, removeEnd);
        sublist.clear();
        sublist.addAll(items);
    }

    /**
     * Append `items` (an array) at the end of `list` (another array).
     * When `list` was empty, returns `items` instead.
     * <p>
     * This prevents a potentially expensive operation when `list` is empty,
     * and adds items in batches to prevent V8 from hanging.
     */
    public static <T> List<T> push(List<T> list, List<T> items) {
        list.addAll(items);
        return list;
    }

}
