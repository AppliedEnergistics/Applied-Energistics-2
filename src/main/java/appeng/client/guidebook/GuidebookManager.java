package appeng.client.guidebook;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.navigation.NavigationTree;
import appeng.core.AppEng;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class GuidebookManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuidebookManager.class);

    public static final GuidebookManager INSTANCE = new GuidebookManager();
    private final Map<ResourceLocation, ParsedGuidePage> developmentPages = new HashMap<>();
    private NavigationTree navigationTree = new NavigationTree();
    private Map<ResourceLocation, ParsedGuidePage> pages;

    private GuidebookManager() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ReloadListener());

        var sourceFolder = System.getProperty("appeng.guide.sources");
        if (sourceFolder != null) {
            // Allow overriding which Mod-ID is used for the sources in the given folder
            var namespace = System.getProperty("appeng.guide.sources.namespace", AppEng.MOD_ID);
            watchDevelopmentSources(sourceFolder, namespace);
        }
    }

    public static void init() {
        // Guaranteed init order
    }

    @Nullable
    public ParsedGuidePage getPage(ResourceLocation id) {
        if (pages == null) {
            LOGGER.warn("Can't get page {}. Pages not loaded yet.", id);
            return null;
        }

        var devPage = developmentPages.get(id);
        if (devPage != null) {
            return devPage;
        }

        return pages.get(id);
    }

    public NavigationTree getNavigationTree() {
        return navigationTree;
    }

    class ReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, ParsedGuidePage>> implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return AppEng.makeId("guidebook");
        }

        @Override
        protected Map<ResourceLocation, ParsedGuidePage> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            profiler.startTick();
            Map<ResourceLocation, ParsedGuidePage> pages = new HashMap<>();

            var resources = resourceManager.listResources("ae2guide", location -> location.getPath().endsWith(".md"));

            for (var entry : resources.entrySet()) {
                var pageId = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        entry.getKey().getPath().substring("ae2guide/".length())
                );

                String sourcePackId = entry.getValue().sourcePackId();
                try (var in = entry.getValue().open()) {
                    pages.put(pageId, PageCompiler.parse(sourcePackId, pageId, in));
                } catch (IOException e) {
                    LOGGER.error("Failed to load guidebook page {} from pack {}", pageId, sourcePackId, e);
                }
            }


            profiler.endTick();
            return pages;
        }

        @Override
        protected void apply(Map<ResourceLocation, ParsedGuidePage> pages, ResourceManager resourceManager, ProfilerFiller profiler) {
            profiler.startTick();
            GuidebookManager.this.pages = pages;
            profiler.push("navigation");
            navigationTree = buildNavigation();
            profiler.pop();
            profiler.endTick();
        }

        @Override
        public String getName() {
            return "AE2 Guidebook";
        }
    }

    private void watchDevelopmentSources(String developmentSources, String namespace) {
        var watcher = new GuideSourceWatcher(developmentPages, namespace, Paths.get(developmentSources));
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (watcher.applyChanges()) {
                this.navigationTree = buildNavigation();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> watcher.close());
    }

    private NavigationTree buildNavigation() {
        if (developmentPages.isEmpty()) {
            return NavigationTree.build(pages.values());
        } else {
            var allPages = new HashMap<>(pages);
            allPages.putAll(developmentPages);
            return NavigationTree.build(allPages.values());
        }
    }

}

