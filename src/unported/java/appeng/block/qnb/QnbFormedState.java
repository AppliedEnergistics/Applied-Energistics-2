
package appeng.block.qnb;

import java.util.Set;

import net.minecraft.util.math.Direction;

public class QnbFormedState {

    private final Set<Direction> adjacentQuantumBridges;

    private final boolean corner;

    private final boolean powered;

    public QnbFormedState(Set<Direction> adjacentQuantumBridges, boolean corner, boolean powered) {
        this.adjacentQuantumBridges = adjacentQuantumBridges;
        this.corner = corner;
        this.powered = powered;
    }

    public Set<Direction> getAdjacentQuantumBridges() {
        return this.adjacentQuantumBridges;
    }

    public boolean isCorner() {
        return this.corner;
    }

    public boolean isPowered() {
        return this.powered;
    }

}
