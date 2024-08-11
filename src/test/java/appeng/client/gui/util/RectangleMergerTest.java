package appeng.client.gui.util;

import appeng.client.guidebook.document.LytRect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RectangleMergerTest {
    @Test
    public void testEmpty() {
        assertThat(RectangleMerger.merge(List.of())).isEmpty();
    }

    @Test
    public void testSingle() {
        var r = new LytRect(5, 10, 20, 50);
        assertThat(RectangleMerger.merge(List.of(r)))
                .containsOnly(r);
    }

    @Test
    public void testMergeIdentical() {
        var r1 = new LytRect(5, 10, 20, 50);
        var r2 = new LytRect(5, 10, 20, 50);
        assertThat(RectangleMerger.merge(List.of(r1, r2)))
                .containsOnly(r1);
    }

    /**
     * The algorithm doesn't "look ahead" to merge adjacent rectangles. It only handles overlap.
     */
    @Test
    public void testDoesNotMergeAdjacentOnPrimaryAxis() {
        var r1 = new LytRect(5, 10, 20, 50);
        var r2 = new LytRect(25, 10, 20, 50);
        assertThat(RectangleMerger.merge(List.of(r1, r2)))
                .containsOnly(r1, r2);
    }

    /**
     * The algorithm does indeed look ahead on the secondary axis to merge adjacent rectangles.
     */
    @Test
    public void testDoesMergeAdjacentOnSecondaryAxis() {
        var r1 = new LytRect(0, 0, 10, 10);
        var r2 = new LytRect(0, 10, 10, 10);
        assertThat(RectangleMerger.merge(List.of(r1, r2)))
                .containsOnly(new LytRect(0, 0, 10, 20));
    }

    /**
     * The algorithm doesn't "look ahead" to merge adjacent rectangles. It only handles overlap.
     */
    @Test
    public void testMergeOverlapping() {
        var r1 = new LytRect(5, 10, 20, 50);
        var r2 = new LytRect(24, 10, 21, 50);
        assertThat(RectangleMerger.merge(List.of(r1, r2)))
                .containsOnly(
                        new LytRect(5, 10, 19, 50),
                        new LytRect(24, 10, 1, 50),
                        new LytRect(25, 10, 20, 50)
                );
    }

    @Test
    public void testCornerOverlaps() {
        var centerRect = new LytRect(-10, -10, 20, 20);
        var topLeft = new LytRect(-15, -15, 10, 10);
        var topRight = new LytRect(5, -15, 10, 10);
        var bottomRight = new LytRect(5, 5, 10, 10);
        var bottomLeft = new LytRect(-15, 5, 10, 10);

        assertThat(RectangleMerger.merge(List.of(centerRect, topLeft, topRight, bottomRight, bottomLeft)))
                .containsOnly(
                        new LytRect(-15, -15, 5, 10),
                        new LytRect(-15, 5, 5, 10),
                        new LytRect(-10, -15, 5, 30),
                        new LytRect(-5, -10, 10, 20),
                        new LytRect(5, -15, 5, 30),
                        new LytRect(10, -15, 5, 10),
                        new LytRect(10, 5, 5, 10)
                );
    }
}
