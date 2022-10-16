package appeng.libs.micromark;

import java.util.List;

public final class ChunkUtils {
    private  ChunkUtils() {
    }

    /**
     * This does not return the removed items, and takes `items` as an
     * array instead of rest parameters.
     */
    public static <T> void splice(List<T> list, int start, int remove, List<T> items) {
       var end = list.size();

        // Make start between zero and `end` (included).
        if (start < 0) {
            start = -start > end ? 0 : end + start;
        } else {
            start = Math.min(start, end);
        }

        remove = Math.max(remove, 0);

        if (remove > 0) {
            list.subList(start, start + remove).clear();
        }
        list.addAll(start, items);
    }

    /**
     * Append `items` (an array) at the end of `list` (another array).
     * When `list` was empty, returns `items` instead.
     * <p>
     * This prevents a potentially expensive operation when `list` is empty,
     * and adds items in batches to prevent V8 from hanging.
     */
    public static <T> List<T> push(List<T> list, List<T> items) {
        if (!list.isEmpty()) {
            list.addAll(items);
            return list;
        }

        return items;
    }

}
