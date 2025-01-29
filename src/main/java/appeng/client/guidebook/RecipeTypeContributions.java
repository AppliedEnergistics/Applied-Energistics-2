package appeng.client.guidebook;

import guideme.compiler.tags.RecipeTypeMappingSupplier;

import appeng.recipes.AERecipeTypes;

public class RecipeTypeContributions implements RecipeTypeMappingSupplier {
    @Override
    public void collect(RecipeTypeMappings mappings) {
        mappings.add(AERecipeTypes.INSCRIBER, LytInscriberRecipe::new);
        mappings.add(AERecipeTypes.CHARGER, LytChargerRecipe::new);
        mappings.add(AERecipeTypes.TRANSFORM, LytTransformRecipe::new);
    }
}
