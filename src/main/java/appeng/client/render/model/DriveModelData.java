package appeng.client.render.model;

import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.block.storage.DriveSlotsState;

public class DriveModelData extends AEModelData {

    public final static ModelProperty<DriveSlotsState> STATE = new ModelProperty<>();

    private final DriveSlotsState slotsState;

    public DriveModelData(Direction up, Direction forward, DriveSlotsState slotsState) {
        super(up, forward);
        this.slotsState = slotsState;
    }

    @Override
    protected boolean isCacheable() {
        return false; // Too many combinations
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
        DriveModelData that = (DriveModelData) o;
        return slotsState.equals(that.slotsState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), slotsState);
    }

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return prop == STATE || super.hasProperty(prop);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Nullable
    public <T> T getData(ModelProperty<T> prop) {
        if (prop == STATE) {
            return (T) this.slotsState;
        }
        return super.getData(prop);
    }

}
