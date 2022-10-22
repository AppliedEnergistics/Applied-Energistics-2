package appeng.client.guidebook.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.client.guidebook.compiler.ParsedGuidePage;

public class NavigationTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationTree.class);

    private final Map<ResourceLocation, NavigationNode> nodeIndex;

    private final List<NavigationNode> rootNodes;

    public NavigationTree(Map<ResourceLocation, NavigationNode> nodeIndex, List<NavigationNode> rootNodes) {
        this.nodeIndex = nodeIndex;
        this.rootNodes = rootNodes;
    }

    public NavigationTree() {
        this.nodeIndex = Map.of();
        this.rootNodes = List.of();
    }

    public List<NavigationNode> getRootNodes() {
        return rootNodes;
    }

    @Nullable
    public NavigationNode getNodeById(ResourceLocation pageId) {
        return nodeIndex.get(pageId);
    }

    public static NavigationTree build(Collection<ParsedGuidePage> pages) {
        var pagesWithChildren = new HashMap<ResourceLocation, Pair<ParsedGuidePage, List<ParsedGuidePage>>>();

        // First pass, build a map of pages and their children
        for (var page : pages) {
            var navigationEntry = page.getFrontmatter().navigationEntry();
            if (navigationEntry == null) {
                continue;
            }

            // Create an entry for this page to collect any children it might have
            pagesWithChildren.compute(
                    page.getId(),
                    (resourceLocation, previousPair) -> {
                        return previousPair != null ? Pair.of(page, previousPair.getRight())
                                : Pair.of(page, new ArrayList<>());
                    });

            // Add this page to the collected children of the parent page (if any)
            var parentId = navigationEntry.parent();
            if (parentId != null) {
                pagesWithChildren.compute(
                        parentId, (resourceLocation, prevPage) -> {
                            if (prevPage != null) {
                                prevPage.getRight().add(page);
                                return prevPage;
                            } else {
                                var children = new ArrayList<ParsedGuidePage>();
                                children.add(page);
                                return Pair.of(null, children);
                            }
                        });
            }
        }

        var nodeIndex = new HashMap<ResourceLocation, NavigationNode>(pages.size());
        var rootNodes = new ArrayList<NavigationNode>();

        for (var entry : pagesWithChildren.entrySet()) {
            createNode(nodeIndex, rootNodes, pagesWithChildren, entry.getKey(), entry.getValue());
        }

        // Sort root nodes
        rootNodes.sort(NODE_COMPARATOR);

        return new NavigationTree(Map.copyOf(nodeIndex), List.copyOf(rootNodes));
    }

    private static NavigationNode createNode(HashMap<ResourceLocation, NavigationNode> nodeIndex,
            ArrayList<NavigationNode> rootNodes,
            Map<ResourceLocation, Pair<ParsedGuidePage, List<ParsedGuidePage>>> pagesWithChildren,
            ResourceLocation pageId,
            Pair<ParsedGuidePage, List<ParsedGuidePage>> entry) {
        var page = entry.getKey();
        var children = entry.getRight();

        if (page == null) {
            // These children had a parent that doesn't exist
            LOGGER.error("Pages {} had unknown navigation parent {}", children, pageId);
            return null;
        }

        var navigationEntry = Objects.requireNonNull(page.getFrontmatter().navigationEntry(), "navigation frontmatter");

        // Construct the icon if set
        var icon = ItemStack.EMPTY;
        if (navigationEntry.iconItemId() != null) {
            var iconItem = BuiltInRegistries.ITEM.get(navigationEntry.iconItemId());
            icon = new ItemStack(iconItem);
            icon.setTag(navigationEntry.iconNbt());
            if (icon.isEmpty()) {
                LOGGER.error("Couldn't find icon {} for icon of page {}", navigationEntry.iconItemId(), page);
            }
        }

        var childNodes = new ArrayList<NavigationNode>(children.size());
        for (var childPage : children) {
            var childPageEntry = pagesWithChildren.get(childPage.getId());

            childNodes.add(createNode(nodeIndex, rootNodes, pagesWithChildren, childPage.getId(), childPageEntry));
        }
        childNodes.sort(NODE_COMPARATOR);

        var node = new NavigationNode(
                page.getId(),
                navigationEntry.title(),
                icon,
                childNodes,
                navigationEntry.position(),
                true);
        nodeIndex.put(page.getId(), node);
        if (navigationEntry.parent() == null) {
            rootNodes.add(node);
        }
        return node;
    }

    private static final Comparator<NavigationNode> NODE_COMPARATOR = Comparator.comparingInt(NavigationNode::position)
            .thenComparing(NavigationNode::title);

}
