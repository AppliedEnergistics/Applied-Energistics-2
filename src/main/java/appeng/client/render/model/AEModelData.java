package appeng.client.render.model;

import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * This implementation of IModelData allows us to know precisely which data is part of the model data. This is relevant
 * for {@link AutoRotatingBakedModel} and {@link AutoRotatingCacheKey}.
 */
public class AEModelData implements IModelData {

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

    @Override
    public boolean hasProperty(ModelProperty<?> prop) {
        return false;
    }

    @Nullable
    @Override
    public <T> T getData(ModelProperty<T> prop) {
        throw new IllegalStateException();
    }

    @Nullable
    @Override
    public <T> T setData(ModelProperty<T> prop, T data) {
        throw new IllegalStateException();
    }
}
