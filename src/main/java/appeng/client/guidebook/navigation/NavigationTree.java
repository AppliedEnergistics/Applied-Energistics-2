package appeng.client.guidebook.navigation;

import appeng.client.guidebook.compiler.ParsedGuidePage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NavigationTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationTree.class);

    private final Map<ResourceLocation, Node> nodeIndex;

    private final List<Node> rootNodes;

    public NavigationTree(Map<ResourceLocation, Node> nodeIndex, List<Node> rootNodes) {
        this.nodeIndex = nodeIndex;
        this.rootNodes = rootNodes;
    }

    public NavigationTree() {
        this.nodeIndex = Map.of();
        this.rootNodes = List.of();
    }

    public List<Node> getRootNodes() {
        return rootNodes;
    }

    @Nullable
    public Node getNodeById(ResourceLocation pageId) {
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
                        return previousPair != null ?
                                Pair.of(page, previousPair.getRight())
                                : Pair.of(page, new ArrayList<>());
                    }
            );

            // Add this page to the colleted children of the parent page (if any)
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
                        }
                );
            }
        }

        var nodeIndex = new HashMap<ResourceLocation, Node>(pages.size());
        var rootNodes = new ArrayList<Node>();

        for (var entry : pagesWithChildren.entrySet()) {
            createNode(nodeIndex, rootNodes, pagesWithChildren, entry.getKey(), entry.getValue());
        }

        // Sort root nodes
        rootNodes.sort(NODE_COMPARATOR);

        return new NavigationTree(Map.copyOf(nodeIndex), List.copyOf(rootNodes));
    }

    private static Node createNode(HashMap<ResourceLocation, Node> nodeIndex,
                                   ArrayList<Node> rootNodes,
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
            var iconItem = Registry.ITEM.get(navigationEntry.iconItemId());
            icon = new ItemStack(iconItem);
            icon.setTag(navigationEntry.iconNbt());
            if (icon.isEmpty()) {
                LOGGER.error("Couldn't find icon {} for icon of page {}", navigationEntry.iconItemId(), page);
            }
        }

        var childNodes = new ArrayList<Node>(children.size());
        for (var childPage : children) {
            var childPageEntry = pagesWithChildren.get(childPage.getId());

            childNodes.add(createNode(nodeIndex, rootNodes, pagesWithChildren, childPage.getId(), childPageEntry));
        }
        childNodes.sort(NODE_COMPARATOR);

        var node = new Node(
                page.getId(),
                navigationEntry.title(),
                icon,
                childNodes,
                navigationEntry.position(),
                true
        );
        nodeIndex.put(page.getId(), node);
        if (navigationEntry.parent() == null) {
            rootNodes.add(node);
        }
        return node;
    }

    private static final Comparator<Node> NODE_COMPARATOR = Comparator.comparingInt(Node::position)
            .thenComparing(Node::title);

    record Node(ResourceLocation pageId, String title, ItemStack icon, List<Node> children, int position,
                boolean hasPage) {
    }

}
