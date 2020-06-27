package appeng.client.render.model;

import java.util.Objects;

import net.minecraft.util.math.Direction;

import appeng.block.storage.DriveSlotsState;

public class DriveModelData extends AEModelData {

    private final DriveSlotsState slotsState;

    public DriveModelData(Direction up, Direction forward, DriveSlotsState slotsState) {
        super(up, forward);
        this.slotsState = slotsState;
    }

    @Override
    public boolean isCacheable() {
        return false; // Too many combinations
    }

    public DriveSlotsState getSlotsState() {
        return slotsState;
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

}
