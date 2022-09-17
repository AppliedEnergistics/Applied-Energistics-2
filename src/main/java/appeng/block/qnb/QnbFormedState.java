package appeng.block.qnb;


import net.minecraft.util.EnumFacing;

import java.util.Set;


public class QnbFormedState {

    private final Set<EnumFacing> adjacentQuantumBridges;

    private final boolean corner;

    private final boolean powered;

    public QnbFormedState(Set<EnumFacing> adjacentQuantumBridges, boolean corner, boolean powered) {
        this.adjacentQuantumBridges = adjacentQuantumBridges;
        this.corner = corner;
        this.powered = powered;
    }

    public Set<EnumFacing> getAdjacentQuantumBridges() {
        return this.adjacentQuantumBridges;
    }

    public boolean isCorner() {
        return this.corner;
    }

    public boolean isPowered() {
        return this.powered;
    }

}
