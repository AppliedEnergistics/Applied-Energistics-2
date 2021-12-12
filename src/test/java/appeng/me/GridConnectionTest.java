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

import appeng.api.exceptions.ExistingConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.networking.IGridNode;
import appeng.util.Platform;

class GridConnectionTest extends AbstractGridNodeTest {

    @Test
    void testConnectionsToSelfArePrevented() {
        var a = makeReadyNode();

        assertThrows(Exception.class, () -> GridConnection.create(a, a, null));
    }

    @Test
    void testDuplicateConnectionsArePrevented() throws Exception {
        var a = makeReadyNode();
        var b = makeReadyNode();
        GridConnection.create(a, b, null);

        assertThrows(ExistingConnectionException.class, () -> GridConnection.create(a, b, null));
        assertThrows(ExistingConnectionException.class, () -> GridConnection.create(b, a, null));
    }

    @Test
    void testConnectionsBetweenDifferentlySecuredGridsAreForbidden() throws Exception {
        var a = makeReadyNode();
        var b = makeReadyNode();

        platform.when(() -> Platform.securityCheck(a, b)).thenReturn(false);
        assertThrows(SecurityConnectionException.class, () -> GridConnection.create(a, b, null));
    }

    /**
     * When disconnecting nodes while they're being destroyed, listeners on such nodes should not be called.
     */
    @Test
    void destroyConnectionDoesNotNotifyDestroyedNodes() throws Exception {
        var a = makeReadyNode();
        var b = makeNode();
        var con = GridConnection.create(a, b, null);
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
        void testGridlessToGridless() throws Exception {
            var a = makeNode();
            var b = makeNode();
            GridConnection.create(a, b, null);
            assertOnlyConnection(a, b);
        }

        @Test
        void testGridAToGridless() throws Exception {
            var a = makeNode();
            var g = Grid.create(a);
            var b = makeNode();
            GridConnection.create(a, b, null);
            assertSame(g, a.getGrid());
            assertSame(g, b.getGrid());
            assertOnlyConnection(a, b);
        }

        @Test
        void testGridBToGridless() throws Exception {
            var a = makeNode();
            var b = makeNode();
            var g = Grid.create(b);
            GridConnection.create(a, b, null);
            assertSame(g, a.getGrid());
            assertSame(g, b.getGrid());
            assertOnlyConnection(a, b);
        }

        @Test
        void testAlreadyOnSameGrid() throws Exception {
            // Create a triangle connection
            var a = makeNode();
            var b = makeNode();
            var c = makeNode();
            var g = Grid.create(a);
            GridConnection.create(a, b, null);
            GridConnection.create(b, c, null);

            // Complete the triangle. The grid should not change
            GridConnection.create(a, c, null);
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
        void testMergeGrids() throws Exception {
            // Create a triangle connection
            var a = makeNode();
            var b = makeNode();
            var c = makeNode();
            var d = makeNode();
            GridConnection.create(a, b, null);
            GridConnection.create(c, d, null);

            var leftGrid = a.getGrid();

            // Complete the triangle. The grid should not change
            GridConnection.create(a, c, null);
            assertSame(leftGrid, a.getGrid());
            assertSame(leftGrid, b.getGrid());
            assertSame(leftGrid, c.getGrid());
            assertSame(leftGrid, d.getGrid());
        }

        /**
         * Test that when two grids are merged, the one that is powered is preferred.
         */
        @Test
        void testMergeGridsPreferPoweredGridA() throws Exception {
            var a = makePoweredNode();
            var gridA = a.getInternalGrid();
            var b = makePoweredNode();
            b.getInternalGrid();
            assertNotNull(b.getGrid());

            GridConnection.create(a, b, null);
            assertSame(gridA, a.getGrid());
            assertSame(gridA, b.getGrid());
        }

        /**
         * Test that when two grids are merged, the one that is powered is preferred.
         */
        @Test
        void testMergeGridsPreferPoweredGridB() throws Exception {
            var a = makeNode();
            a.getInternalGrid();
            assertNotNull(a.getGrid());
            var b = makePoweredNode();
            var gridB = b.getInternalGrid();

            GridConnection.create(a, b, null);
            assertSame(gridB, a.getGrid());
            assertSame(gridB, b.getGrid());
        }

        /**
         * Test that when merging two unpowered or powered grids, the grid size is used as a tie breaker to decide which
         * one propagates to the other nodes.
         */
        @Test
        void testMergeGridsUsesSizeAsTieBreaker() throws Exception {
            var a = makeReadyNode();
            var b = makeReadyNode();
            var c = makeReadyNode();
            GridConnection.create(a, b, null);
            var largerGrid = b.getInternalGrid();
            assertEquals(2, largerGrid.size());
            var smallerGrid = c.getInternalGrid();
            assertEquals(1, smallerGrid.size());

            var con = GridConnection.create(b, c, null);
            assertSame(largerGrid, c.getGrid());

            // Now try the other way
            con.destroy();
            GridConnection.create(c, b, null);
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
