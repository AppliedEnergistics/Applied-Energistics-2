package appeng.client.guidebook.navigation;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record NavigationNode(
        @Nullable ResourceLocation pageId,
        String title,
        ItemStack icon,
        List<NavigationNode> children,
        int position,
        boolean hasPage) {
}
