package appeng.tile.crafting;

import java.util.EnumSet;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.api.util.AEColor;

public class CraftingMonitorModelData extends CraftingCubeModelData {

    public static final ModelProperty<AEColor> COLOR = new ModelProperty<>();

    private final AEColor color;

    public CraftingMonitorModelData(Direction up, Direction forward, EnumSet<Direction> connections, AEColor color) {
        super(up, forward, connections);
        this.color = Preconditions.checkNotNull(color);
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
        CraftingMonitorModelData that = (CraftingMonitorModelData) o;
        return color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), color);
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == COLOR || super.hasProperty(prop);
    }

    @Override
    @Nullable
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == COLOR) {
            return (T) this.color;
        }
        return super.getData(prop);
    }

}
