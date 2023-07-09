package appeng.siteexport.model;

import java.util.List;

import appeng.client.guidebook.navigation.NavigationNode;
import appeng.siteexport.SiteExportWriter;

public class NavigationNodeJson {
    public String pageId;
    public String title;
    public String icon;
    public List<NavigationNodeJson> children;
    public int position;
    public boolean hasPage;

    public static NavigationNodeJson of(SiteExportWriter writer, NavigationNode node) {
        var jsonNode = new NavigationNodeJson();
        if (node.pageId() != null) {
            jsonNode.pageId = node.pageId().toString();
        }
        jsonNode.title = node.title();
        if (!node.icon().isEmpty()) {
            jsonNode.icon = writer.addItem(node.icon());
        }
        jsonNode.children = node.children().stream().map(childNode -> of(writer, childNode)).toList();
        jsonNode.position = node.position();
        jsonNode.hasPage = node.hasPage();
        return jsonNode;
    }
}
