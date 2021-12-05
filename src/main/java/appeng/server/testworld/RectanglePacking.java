/*
 * ISC License
 *
 * Copyright (c) 2018, Mapbox
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package appeng.server.testworld;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * See https://observablehq.com/@mourner/simple-rectangle-packing
 */
final class RectanglePacking {
    private RectanglePacking() {
    }

    public static <T> PositionedArea<T> pack(List<T> objects, Function<T, Size> measurer) {
        var rectangles = new ArrayList<Rectangle<T>>(objects.size());
        for (T object : objects) {
            var size = measurer.apply(object);
            rectangles.add(new Rectangle<>(
                    0, 0, size.w, size.h, object));
        }

        // calculate total box area and maximum box width
        var area = 0;
        var maxWidth = 0;
        for (var box : rectangles) {
            area += box.w * box.h;
            maxWidth = Math.max(maxWidth, box.w);
        }

        // sort the rectangles for insertion by height, descending
        rectangles.sort((a, b) -> b.h - a.h);

        // aim for a squarish resulting container,
        // slightly adjusted for sub-100% space utilization
        var startWidth = (int) Math.max(Math.ceil(Math.sqrt(area / 0.95)), maxWidth);

        // start with a single empty space, unbounded at the bottom
        var spaces = new ArrayList<Rectangle<T>>();
        spaces.add(new Rectangle<>(0, 0, startWidth, Integer.MAX_VALUE, null));

        for (var box : rectangles) {
            // look through spaces backwards so that we check smaller spaces first
            for (var i = spaces.size() - 1; i >= 0; i--) {
                var space = spaces.get(i);

                // look for empty spaces that can accommodate the current box
                if (box.w > space.w || box.h > space.h)
                    continue;

                // found the space; add the box to its top-left corner
                // |-------|-------|
                // | box | |
                // |_______| |
                // | space |
                // |_______________|
                box.x = space.x;
                box.y = space.y;

                if (box.w == space.w && box.h == space.h) {
                    // space matches the box exactly; remove it
                    var last = spaces.remove(spaces.size() - 1);
                    if (i < spaces.size()) {
                        spaces.set(i, last);
                    }

                } else if (box.h == space.h) {
                    // space matches the box height; update it accordingly
                    // |-------|---------------|
                    // | box | updated space |
                    // |_______|_______________|
                    space.x += box.w;
                    space.w -= box.w;

                } else if (box.w == space.w) {
                    // space matches the box width; update it accordingly
                    // |---------------|
                    // | box |
                    // |_______________|
                    // | updated space |
                    // |_______________|
                    space.y += box.h;
                    space.h -= box.h;

                } else {
                    // otherwise the box splits the space into two spaces
                    // |-------|-----------|
                    // | box | new space |
                    // |_______|___________|
                    // | updated space |
                    // |___________________|
                    spaces.add(new Rectangle<>(
                            space.x + box.w,
                            space.y,
                            space.w - box.w,
                            box.h,
                            null));
                    space.y += box.h;
                    space.h -= box.h;
                }
                break;
            }
        }

        var positioned = rectangles.stream().map(Rectangle::toPositioned).toList();
        var width = rectangles.stream().mapToInt(r -> r.x + r.w).max().orElse(0);
        var height = rectangles.stream().mapToInt(r -> r.y + r.h).max().orElse(0);
        return new PositionedArea<>(width, height, positioned);
    }

    private static class Rectangle<T> {
        private int x;
        private int y;
        private int w;
        private int h;
        private final T wrapped;

        public Rectangle(int x, int y, int w, int h, T wrapped) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.wrapped = wrapped;
        }

        public Rectangle<T> copy() {
            return new Rectangle<>(x, y, w, h, wrapped);
        }

        public Positioned<T> toPositioned() {
            return new Positioned<>(x, y, w, h, wrapped);
        }
    }

    public record Size(int w, int h) {
    }

    public record PositionedArea<T> (int w, int h, List<Positioned<T>> rectangles) {
    }

    public record Positioned<T> (int x, int y, int w, int h, T what) {
    }
}
