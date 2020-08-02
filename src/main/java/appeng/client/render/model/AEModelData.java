package appeng.client.render.model;

import java.util.Objects;

import com.google.common.base.Preconditions;

import net.minecraft.util.math.Direction;

/**
 * This implementation of IModelData allows us to know precisely which data is
 * part of the model data. This is relevant for {@link AutoRotatingBakedModel}
 * and {@link AutoRotatingCacheKey}.
 */
public class AEModelData {

    private final Direction up;
    private final Direction forward;

    public AEModelData(Direction up, Direction forward) {
        this.up = Preconditions.checkNotNull(up);
        this.forward = Preconditions.checkNotNull(forward);
    }

    public Direction getUp() {
        return up;
    }

    public Direction getForward() {
        return forward;
    }

    public boolean isCacheable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AEModelData that = (AEModelData) o;
        return up == that.up && forward == that.forward;
    }

    @Override
    public int hashCode() {
        return Objects.hash(up, forward);
    }

}
