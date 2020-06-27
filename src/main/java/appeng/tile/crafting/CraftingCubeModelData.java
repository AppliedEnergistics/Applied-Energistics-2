package appeng.tile.crafting;

import java.util.EnumSet;
import java.util.Objects;

import com.google.common.base.Preconditions;

import net.minecraft.util.math.Direction;

import appeng.client.render.model.AEModelData;

public class CraftingCubeModelData extends AEModelData {

    // Contains information on which sides of the block are connected to other parts
    // of a formed crafting cube
    private final EnumSet<Direction> connections;

    public CraftingCubeModelData(Direction up, Direction forward, EnumSet<Direction> connections) {
        super(up, forward);
        this.connections = Preconditions.checkNotNull(connections);
    }

    @Override
    public boolean isCacheable() {
        return false; // Too many variants
    }

    public EnumSet<Direction> getConnections() {
        return connections;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CraftingCubeModelData that = (CraftingCubeModelData) o;
        return connections.equals(that.connections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), connections);
    }

}
