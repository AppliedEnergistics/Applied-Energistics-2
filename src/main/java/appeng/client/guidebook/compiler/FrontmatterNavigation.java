package appeng.client.guidebook.compiler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Inserts a page into the navigation tree.
 * Null parent means top-level category.
 */
public record FrontmatterNavigation(
        String title,
        @Nullable
        ResourceLocation parent,
        int position,
        @Nullable
        ResourceLocation iconItemId,
        @Nullable
        CompoundTag iconNbt
) {
}
