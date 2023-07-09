package appeng.siteexport.model;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import appeng.client.guidebook.navigation.NavigationNode;

public class NavigationNodeJson {
    public String pageId;
    public String title;
    public ItemStack icon;
    public List<NavigationNodeJson> children;
    public int position;
    public boolean hasPage;

    public static NavigationNodeJson of(NavigationNode node) {
        var jsonNode = new NavigationNodeJson();
        if (node.pageId() != null) {
            jsonNode.pageId = node.pageId().toString();
        }
        jsonNode.title = node.title();
        if (!node.icon().isEmpty()) {
            jsonNode.icon = node.icon();
        }
        jsonNode.children = node.children().stream().map(NavigationNodeJson::of).toList();
        jsonNode.position = node.position();
        jsonNode.hasPage = node.hasPage();
        return jsonNode;
    }
}
