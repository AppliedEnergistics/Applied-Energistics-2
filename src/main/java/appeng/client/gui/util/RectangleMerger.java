package appeng.client.gui.util;

import appeng.client.guidebook.document.LytRect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Remove overlapping rectangles and produce a list of equivalent non-overlapping rectangles.
 */
public final class RectangleMerger {
    private RectangleMerger() {
    }

    /**
     * This implements a line-sweep algorithm that ensures all rectangles are non-overlapping
     * by merging or subdividing overlapping rectangles.
     * <p>
     * What this algorithm ends up doing is essentially cutting up the union of all rectangles into
     * vertical stripes by scanning from left-to-right and producing rectangles whenever a rectangle
     * ends on the horizontal axis.
     */
    public static List<LytRect> merge(List<LytRect> rects) {
        if (rects.isEmpty()) {
            return List.of();
        }

        List<LytRect> result = new ArrayList<>(rects.size());

        List<Event> xEvents = getSortedXEvents(rects);
        List<Event> yEvents = getSortedYEvents(rects);

        // For each rect-index, mark if it's currently open (that is we saw an opening event along
        // the primary axis, but not yet a closing)
        boolean[] currentSet = new boolean[rects.size()];

        for (int i = 0; i < xEvents.size() - 1; i++) {
            var xEvent = xEvents.get(i);
            currentSet[xEvent.ind] = xEvent.type == OPENING;

            var nextXEvent = xEvents.get(i + 1);

            // For stacked rectangles, only process the last (after adding all to the current set)
            if (nextXEvent.x == xEvent.x) {
                continue;
            }

            int left = xEvent.x;
            int right = nextXEvent.x;
            int top = 0;
            int opened = 0; // Used to find the end of rectangles that overlap on the y-axis

            yEvents:
            for (int j = 0; j < yEvents.size(); j++) {
                var yEvent = yEvents.get(j);

                // Only process rects for which we are between open/close on the primary axis
                if (currentSet[yEvent.ind]) {
                    if (yEvent.type == OPENING) {
                        if (++opened == 1) {
                            // Use the first open after a clear area as the top
                            // of the rectangle we'll produce
                            top = yEvent.y;
                        }
                    } else {
                        if (--opened == 0) {
                            // Try to find the next relevant open event.
                            // If it is directly adjacent, skip creating a rectangle now, and continue with it
                            for (int k = j + 1; k < yEvents.size(); k++) {
                                var nextEvent = yEvents.get(k);
                                if (currentSet[nextEvent.ind] && nextEvent.type == OPENING) {
                                    if (nextEvent.y == yEvent.y) {
                                        ++opened;
                                        j = k;
                                        // Note that we keep top intact to produce a joined rectangle
                                        continue yEvents;
                                    }
                                    break;
                                }
                            }

                            // Try to join the emitted rectangle if the next opening rectangle is directly adjacent
                            result.add(new LytRect(left, top, right - left, yEvent.y - top));
                        }
                    }
                }
            }
        }

        return result;
    }

    private static List<Event> getSortedXEvents(List<LytRect> rects) {
        List<Event> events = new ArrayList<>(rects.size() * 2);

        for (int i = 0; i < rects.size(); i++) {
            var rect = rects.get(i);
            events.add(new Event(i, OPENING, rect.x(), rect.y(), rect.width()));
            events.add(new Event(i, CLOSING, rect.x() + rect.width(), rect.y(), rect.width()));
        }

        events.sort(Comparator.comparingInt((Event e) -> e.x).thenComparingInt(e -> e.y));
        return events;
    }

    private static List<Event> getSortedYEvents(List<LytRect> rects) {
        List<Event> events = new ArrayList<>(rects.size() * 2);

        for (int i = 0; i < rects.size(); i++) {
            var rect = rects.get(i);
            events.add(new Event(i, OPENING, rect.x(), rect.y(), rect.height()));
            events.add(new Event(i, CLOSING, rect.x(), rect.y() + rect.height(), rect.height()));
        }

        events.sort(Comparator.comparingInt((Event e) -> e.y).thenComparingInt(e -> e.x));
        return events;
    }

    private static final int OPENING = 0;
    private static final int CLOSING = 1;

    public record Event(int ind, int type, int x, int y, int length) {
    }
}
