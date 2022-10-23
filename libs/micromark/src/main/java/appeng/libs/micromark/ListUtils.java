package appeng.libs.micromark;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {
    private ListUtils() {
    }

    public static <T> void splice(List<T> list, int start, int remove) {
        var removeEnd = Math.min(list.size(), start + remove);
        var sublist = list.subList(start, removeEnd);
        sublist.clear();
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

    public static <T> List<T> slice(List<T> list) {
        return new ArrayList<>(list);
    }

    public static <T> List<T> slice(List<T> list, int start) {
        return slice(list, start, list.size());
    }

    public static <T> List<T> slice(List<T> list, int start, int end) {
        if (start > list.size() || end <= start) {
            return new ArrayList<>();
        }
        end = Math.min(list.size(), end);
        return new ArrayList<>(list.subList(start, end));
    }

    public static <T> void setLength(List<T> list, int newLength) {
        if (newLength <= 0) {
            list.clear();
        } else if (newLength < list.size()) {
            list.subList(newLength, list.size()).clear();
        }
    }

}
