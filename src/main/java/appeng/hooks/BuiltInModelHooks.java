package appeng.hooks;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

/**
 * Replicates how Fabric allows custom built-in models to be registered on Forge.
 */
public class BuiltInModelHooks {
    private static final Map<ResourceLocation, UnbakedModel> builtInModels = new HashMap<>();

    private BuiltInModelHooks() {
    }

    public static void addBuiltInModel(ResourceLocation id, UnbakedModel model) {
        if (builtInModels.put(id, model) != null) {
            throw new IllegalStateException("Duplicate built-in model ID: " + id);
        }
    }

    @Nullable
    public static UnbakedModel getBuiltInModel(ResourceLocation variantId) {
        if (!AppEng.MOD_ID.equals(variantId.getNamespace())) {
            return null;
        }

        // Vanilla loads item models as <id>#inventory, which we replicate here
        if (variantId instanceof ModelResourceLocation modelId) {
            if ("inventory".equals(modelId.getVariant())) {
                var itemModelId = new ResourceLocation(modelId.getNamespace(), "item/" + modelId.getPath());
                return builtInModels.get(itemModelId);
            }

            return null;
        } else {
            return builtInModels.get(variantId);
        }
    }
}
