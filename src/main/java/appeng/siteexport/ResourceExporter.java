package appeng.siteexport;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

public interface ResourceExporter {
    default void referenceItem(ItemLike item) {
        referenceItem(new ItemStack(item.asItem()));
    }

    void referenceItem(ItemStack stack);

    void referenceFluid(Fluid fluid);

    Path renderAndWrite(OffScreenRenderer renderer,
            String baseName,
            Runnable renderRunnable,
            Collection<TextureAtlasSprite> sprites,
            boolean withAlpha) throws IOException;

    String exportTexture(ResourceLocation texture);

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
        return "/" + getOutputFolder().relativize(p).toString().replace('\\', '/');
    }

    /**
     * Generates a resource location for a page specific resource.
     */
    ResourceLocation getPageSpecificResourceLocation(String suffix);

    void referenceRecipe(Recipe<?> recipe);
}
