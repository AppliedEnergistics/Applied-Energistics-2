package appeng.me;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;

class GridConnectionTest extends AbstractGridNodeTest {

    @Test
    void testConnectionsToSelfArePrevented() {
        var a = makeReadyNode();

        assertThrows(Exception.class, () -> GridHelper.createConnection(a, a));
    }

    @Test
    void testDuplicateConnectionsArePrevented() {
        var a = makeReadyNode();
        var b = makeReadyNode();
        GridHelper.createConnection(a, b);

        assertThrows(IllegalStateException.class, () -> GridHelper.createConnection(a, b));
        assertThrows(IllegalStateException.class, () -> GridHelper.createConnection(b, a));
    }

    /**
     * When disconnecting nodes while they're being destroyed, listeners on such nodes should not be called.
     */
    @Test
    void destroyConnectionDoesNotNotifyDestroyedNodes() {
        var a = makeReadyNode();
        var b = makeNode();
        var con = GridHelper.createConnection(a, b);
        reset(listener);
        con.destroy();
        verify(listener, never()).onGridChanged(owner, a);
        verify(listener, never()).onGridChanged(owner, b);
    }

    /**
     * Tests how grids propagate between nodes when making a connection.
     */
    @Nested
    class GridPropagation {
        @Test
        void testGridlessToGridless() {
            var a = makeNode();
            var b = makeNode();
            GridHelper.createConnection(a, b);
            assertOnlyConnection(a, b);
        }

        @Test
        void testGridAToGridless() {
            var a = makeNode();
            var g = Grid.create(a);
            var b = makeNode();
            GridHelper.createConnection(a, b);
            assertSame(g, a.getGrid());
            assertSame(g, b.getGrid());
            assertOnlyConnection(a, b);
        }

        @Test
        void testGridBToGridless() {
            var a = makeNode();
            var b = makeNode();
            var g = Grid.create(b);
            GridHelper.createConnection(a, b);
            assertSame(g, a.getGrid());
            assertSame(g, b.getGrid());
            assertOnlyConnection(a, b);
        }

        @Test
        void testAlreadyOnSameGrid() {
            // Create a triangle connection
            var a = makeNode();
            var b = makeNode();
            var c = makeNode();
            var g = Grid.create(a);
            GridHelper.createConnection(a, b);
            GridHelper.createConnection(b, c);

            // Complete the triangle. The grid should not change
            GridHelper.createConnection(a, c);
            assertThat(a.getConnections()).hasSize(2);
            assertThat(b.getConnections()).hasSize(2);
            assertThat(c.getConnections()).hasSize(2);
            assertSame(g, a.getGrid());
            assertSame(g, b.getGrid());
            assertSame(g, c.getGrid());
        }

        /**
         * Test that two grids are merged correctly and form a single grid after being connected. When there's
         * <code>a-b c-d</code>, connecting b to c should form a single grid.
         */
        @Test
        void testMergeGrids() {
            // Create a triangle connection
            var a = makeNode();
            var b = makeNode();
            var c = makeNode();
            var d = makeNode();
            GridHelper.createConnection(a, b);
            GridHelper.createConnection(c, d);

            var leftGrid = a.getGrid();

            // Complete the triangle. The grid should not change
            GridHelper.createConnection(a, c);
            assertSame(leftGrid, a.getGrid());
            assertSame(leftGrid, b.getGrid());
            assertSame(leftGrid, c.getGrid());
            assertSame(leftGrid, d.getGrid());
        }

        /**
         * Test that when two grids are merged, the one that is powered is preferred.
         */
        @Test
        void testMergeGridsPreferPoweredGridA() {
            var a = makePoweredNode();
            var gridA = a.getInternalGrid();
            var b = makePoweredNode();
            b.getInternalGrid();
            assertNotNull(b.getGrid());

            GridHelper.createConnection(a, b);
            assertSame(gridA, a.getGrid());
            assertSame(gridA, b.getGrid());
        }

        /**
         * Test that when two grids are merged, the one that is powered is preferred.
         */
        @Test
        void testMergeGridsPreferPoweredGridB() {
            var a = makeNode();
            a.getInternalGrid();
            assertNotNull(a.getGrid());
            var b = makePoweredNode();
            var gridB = b.getInternalGrid();

            GridHelper.createConnection(a, b);
            assertSame(gridB, a.getGrid());
            assertSame(gridB, b.getGrid());
        }

        /**
         * Test that when merging two unpowered or powered grids, the grid size is used as a tie breaker to decide which
         * one propagates to the other nodes.
         */
        @Test
        void testMergeGridsUsesSizeAsTieBreaker() {
            var a = makeReadyNode();
            var b = makeReadyNode();
            var c = makeReadyNode();
            GridHelper.createConnection(a, b);
            var largerGrid = b.getInternalGrid();
            assertEquals(2, largerGrid.size());
            var smallerGrid = c.getInternalGrid();
            assertEquals(1, smallerGrid.size());

            var con = GridHelper.createConnection(b, c);
            assertSame(largerGrid, c.getGrid());

            // Now try the other way
            con.destroy();
            GridHelper.createConnection(c, b);
            assertSame(largerGrid, c.getGrid());
        }

        private void assertOnlyConnection(GridNode a, GridNode b) {
            assertSameGrid(a, b);
            assertThat(a.getConnections()).hasSize(1);
            // Check that the connection is the same on both sides
            var connection = a.getConnections().get(0);
            assertThat(b.getConnections()).containsExactly(connection);
            if (connection.a() == a) {
                assertThat(connection.a()).isSameAs(a);
                assertThat(connection.b()).isSameAs(b);
            } else {
                assertThat(connection.a()).isSameAs(b);
                assertThat(connection.b()).isSameAs(a);
            }
        }

        private void assertSameGrid(IGridNode a, IGridNode b) {
            assertNotNull(a.getGrid());
            assertNotNull(b.getGrid());
            assertSame(a.getGrid(), b.getGrid());
        }
    }

}
