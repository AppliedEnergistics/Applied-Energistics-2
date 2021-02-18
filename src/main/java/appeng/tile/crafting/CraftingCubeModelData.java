package appeng.tile.crafting;

import java.util.EnumSet;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.client.render.model.AEModelData;

public class CraftingCubeModelData extends AEModelData {

    public static final ModelProperty<EnumSet<Direction>> CONNECTIONS = new ModelProperty<>();

    // Contains information on which sides of the block are connected to other parts
    // of a formed crafting cube
    private final EnumSet<Direction> connections;

    public CraftingCubeModelData(Direction up, Direction forward, EnumSet<Direction> connections) {
        super(up, forward);
        this.connections = Preconditions.checkNotNull(connections);
    }

    @Override
    protected boolean isCacheable() {
        return false; // Too many variants
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

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == CONNECTIONS || super.hasProperty(prop);
    }

    @Override
    @Nullable
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == CONNECTIONS) {
            return (T) this.connections;
        }
        return super.getData(prop);
    }

}
