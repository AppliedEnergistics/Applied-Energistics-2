package appeng.client.render;

import net.minecraft.client.renderer.model.IBakedModel;

/**
 * Helper to get a specific baked model class out of a chain of delegating baked models.
 */
public final class BakedModelUnwrapper {

    private BakedModelUnwrapper() {
    }

    public static <T> T unwrap(IBakedModel model, Class<T> targetClass) {
        if (targetClass.isInstance(model)) {
            return targetClass.cast(model);
        }

        if (model instanceof DelegateBakedModel) {
            model = ((DelegateBakedModel) model).getBaseModel();
            if (targetClass.isInstance(model)) {
                return targetClass.cast(model);
            } else {
                return unwrap(model, targetClass);
            }
        }

        return null;
    }

}
