package appeng.client.guidebook.document.block.recipes;

import net.minecraft.world.item.crafting.RecipeHolder;

import appeng.client.guidebook.document.block.LytBox;
import appeng.siteexport.ExportableResourceProvider;
import appeng.siteexport.ResourceExporter;

public abstract class LytRecipeBox extends LytBox implements ExportableResourceProvider {
    private final RecipeHolder<?> recipe;

    public LytRecipeBox(RecipeHolder<?> recipe) {
        this.recipe = recipe;
    }

    @Override
    public void exportResources(ResourceExporter exporter) {
        exporter.referenceRecipe(this.recipe);
    }
}
