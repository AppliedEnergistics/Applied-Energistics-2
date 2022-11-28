package appeng.client.guidebook.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import com.google.common.collect.Streams;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.style.ResolvedTextStyle;

public class LayoutContext implements FontMetrics {
    private final FontMetrics fontMetrics;

    private final LytRect viewport;

    private final List<LytRect> leftFloats = new ArrayList<>();
    private final List<LytRect> rightFloats = new ArrayList<>();

    public LayoutContext(FontMetrics fontMetrics, LytRect viewport) {
        this.fontMetrics = fontMetrics;
        this.viewport = viewport;
    }

    public LytRect viewport() {
        return this.viewport;
    }

    public int viewportWidth() {
        return viewport().width();
    }

    public int viewportHeight() {
        return viewport().height();
    }

    public void addLeftFloat(LytRect bounds) {
        leftFloats.add(bounds);
    }

    public void addRightFloat(LytRect bounds) {
        rightFloats.add(bounds);
    }

    public OptionalInt getLeftFloatRightEdge() {
        return leftFloats.stream()
                .mapToInt(LytRect::right)
                .max();
    }

    public OptionalInt getRightFloatLeftEdge() {
        return rightFloats.stream()
                .mapToInt(LytRect::x)
                .min();
    }

    // Clears all pending floats and returns the lowest y level below the cleared floats
    public OptionalInt clearFloats(boolean left, boolean right) {
        if (left && right) {
            var result = Streams.concat(leftFloats.stream(), rightFloats.stream())
                    .mapToInt(LytRect::bottom).max();
            leftFloats.clear();
            rightFloats.clear();
            return result;
        } else if (left) {
            var result = leftFloats.stream().mapToInt(LytRect::bottom).max();
            leftFloats.clear();
            return result;
        } else if (right) {
            var result = rightFloats.stream().mapToInt(LytRect::bottom).max();
            rightFloats.clear();
            return result;
        } else {
            return OptionalInt.empty();
        }
    }

    // Close out all floats above the given y position
    public void clearFloatsAbove(int y) {
        leftFloats.removeIf(f -> f.bottom() <= y);
        rightFloats.removeIf(f -> f.bottom() <= y);
    }

    @Override
    public float getAdvance(int codePoint, ResolvedTextStyle style) {
        return fontMetrics.getAdvance(codePoint, style);
    }

    @Override
    public int getLineHeight(ResolvedTextStyle style) {
        return fontMetrics.getLineHeight(style);
    }

    /**
     * If there's a float whose bottom edge is below the given y coordinate, return that bottom edge.
     */
    public OptionalInt getNextFloatBottomEdge(int y) {
        return Streams.concat(leftFloats.stream(), rightFloats.stream())
                .mapToInt(LytRect::bottom)
                .filter(bottom -> bottom > y)
                .min();
    }
}
