package appeng.siteexport;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

public interface ResourceExporter {
    default void referenceItem(ItemLike item) {
        referenceItem(new ItemStack(item.asItem()));
    }

    void referenceItem(ItemStack stack);

    /**
     * @return The new resource id after applying cache busting.
     */
    Path copyResource(ResourceLocation id);

    Path getPathForWriting(ResourceLocation assetId);

    /**
     * Generates a resource location for a page specific resource.
     */
    Path getPageSpecificPathForWriting(String suffix);

    @Nullable
    ResourceLocation getCurrentPageId();

    Path getOutputFolder();

    default String getPathRelativeFromOutputFolder(Path p) {
        return getOutputFolder().relativize(p).toString().replace('\\', '/');
    }

    /**
     * Generates a resource location for a page specific resource.
     */
    ResourceLocation getPageSpecificResourceLocation(String suffix);

    void referenceRecipe(Recipe<?> recipe);
}
