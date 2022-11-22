package appeng.client.guidebook.navigation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record NavigationNode(
        ResourceLocation pageId,
        String title,
        ItemStack icon,
        List<NavigationNode> children,
        int position,
        boolean hasPage
) {
}
