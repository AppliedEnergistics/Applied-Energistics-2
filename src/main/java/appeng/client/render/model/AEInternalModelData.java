package appeng.client.render.model;

import javax.annotation.Nullable;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public abstract class AEInternalModelData implements IModelData {

    @Override
    public final boolean hasProperty(ModelProperty<?> prop) {
        throw new IllegalStateException("This model data must be used by internal models only.");
    }

    @Nullable
    @Override
    public final <T> T getData(ModelProperty<T> prop) {
        throw new IllegalStateException("This model data must be used by internal models only.");
    }

    @Nullable
    @Override
    public final <T> T setData(ModelProperty<T> prop, T data) {
        throw new IllegalStateException("This model data must be used by internal models only.");
    }

}
