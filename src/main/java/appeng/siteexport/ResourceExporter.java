package appeng.siteexport;

import java.nio.file.Path;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public interface ResourceExporter {
    default void referenceItem(ItemLike item) {
        referenceItem(new ItemStack(item.asItem()));
    }

    void referenceItem(ItemStack stack);

    void copyResource(ResourceLocation id);

    Path getPathForWriting(ResourceLocation assetId);

    Path getOutputFolder();

    /**
     * Generates a resource location for a page specific resource.
     */
    ResourceLocation getPageSpecificResourceLocation(String suffix);

    void referenceRecipe(Recipe<?> recipe);
}
