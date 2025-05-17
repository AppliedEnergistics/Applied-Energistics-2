package appeng.me.pathfinding;

import java.util.HashSet;
import java.util.Set;

import appeng.api.networking.IGridNode;
import appeng.me.GridConnection;

// This object is shared between all grid nodes that share a nearest controller.
// Additionally, the "members" field is further shared between ControllerOrigin objects as the grid is pathed.
/**
 * Represents a cluster of nodes relevant to multi-controller trunk pathing that share their nearest controller
 */
public class ControllerInfo {
    /** The calculation that owns this object */
    public final PathingCalculation owner;
    /** Closest controller to this node */
    public final IGridNode controllerNode;
    /** Defines a region that is properly interconnected */
    public Set<ControllerInfo> members;

    public enum TrunkSearchState {
        SEARCHING,
        CONNECTED,
        INVALID
    }

    public class SubtreeInfo {
        /** Root of this subtree */
        public final GridConnection root;
        public TrunkSearchState trunkState = TrunkSearchState.SEARCHING;

        public SubtreeInfo(GridConnection root) {
            this.root = root;
        }

        public ControllerInfo parent() {
            return ControllerInfo.this;
        }
    }

    public ControllerInfo(PathingCalculation owner, IGridNode controllerNode) {
        this.owner = owner;
        this.controllerNode = controllerNode;
        members = new HashSet<>();
        members.add(this);
    }

    public SubtreeInfo forSubtree(GridConnection root) {
        return new SubtreeInfo(root);
    }

    public static Set<ControllerInfo> mergeMembers(ControllerInfo a, ControllerInfo b) {
        Set<ControllerInfo> big, small;
        if (a.members.size() >= b.members.size()) {
            big = a.members;
            small = b.members;
        } else {
            big = b.members;
            small = a.members;
        }
        big.addAll(small);
        for (var entry : small) {
            entry.members = big;
        }
        return big;
    }
}
