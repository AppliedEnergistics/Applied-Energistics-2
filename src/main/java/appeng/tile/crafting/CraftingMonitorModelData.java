package appeng.tile.crafting;

import appeng.api.util.AEColor;
import com.google.common.base.Preconditions;
import net.minecraft.util.Direction;

import java.util.EnumSet;
import java.util.Objects;

public class CraftingMonitorModelData extends CraftingCubeModelData {

    private final AEColor color;

    public CraftingMonitorModelData(Direction up, Direction forward, EnumSet<Direction> connections, AEColor color) {
        super(up, forward, connections);
        this.color = Preconditions.checkNotNull(color);
    }

    public AEColor getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CraftingMonitorModelData that = (CraftingMonitorModelData) o;
        return color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), color);
    }

}
