/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2026, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.gui.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import guideme.document.LytRect;

class RectangleMergerTest {
    @Test
    void testEmpty() {
        assertThat(RectangleMerger.merge(List.of())).isEmpty();
    }

    @Test
    void testSingle() {
        var r = new LytRect(5, 10, 20, 50);
        assertThat(RectangleMerger.merge(List.of(r))).containsOnly(r);
    }

    @Test
    void testMergeIdentical() {
        var r1 = new LytRect(5, 10, 20, 50);
        var r2 = new LytRect(5, 10, 20, 50);
        assertThat(RectangleMerger.merge(List.of(r1, r2))).containsOnly(r1);
    }

    /**
     * The algorithm doesn't "look ahead" to merge adjacent rectangles on the primary axis. It only handles overlap.
     */
    @Test
    void testDoesNotMergeAdjacentOnPrimaryAxis() {
        var r1 = new LytRect(5, 10, 20, 50);
        var r2 = new LytRect(25, 10, 20, 50);
        assertThat(RectangleMerger.merge(List.of(r1, r2))).containsOnly(r1, r2);
    }

    /**
     * The algorithm does look ahead on the secondary axis to merge adjacent rectangles.
     */
    @Test
    void testDoesMergeAdjacentOnSecondaryAxis() {
        var r1 = new LytRect(0, 0, 10, 10);
        var r2 = new LytRect(0, 10, 10, 10);
        assertThat(RectangleMerger.merge(List.of(r1, r2))).containsOnly(new LytRect(0, 0, 10, 20));
    }

    @Test
    void testMergeOverlapping() {
        var r1 = new LytRect(5, 10, 20, 50);
        var r2 = new LytRect(24, 10, 21, 50);
        assertThat(RectangleMerger.merge(List.of(r1, r2)))
                .containsOnly(
                        new LytRect(5, 10, 19, 50),
                        new LytRect(24, 10, 1, 50),
                        new LytRect(25, 10, 20, 50));
    }

    @Test
    void testCornerOverlaps() {
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
                        new LytRect(10, 5, 5, 10));
    }
}
