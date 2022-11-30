package appeng.client.guidebook;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.indices.CategoryIndex;
import appeng.client.guidebook.indices.ItemIndex;
import appeng.client.guidebook.indices.PageIndex;
import appeng.client.guidebook.navigation.NavigationTree;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.util.Platform;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GuideManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuideManager.class);

    public static final GuideManager INSTANCE = new GuideManager();
    private final Map<ResourceLocation, ParsedGuidePage> developmentPages = new HashMap<>();
    private final List<PageIndex> indices = new ArrayList<>();
    private NavigationTree navigationTree = new NavigationTree();
    private Map<ResourceLocation, ParsedGuidePage> pages;

    @Nullable
    private final Path developmentSourceFolder;
    @Nullable
    private final String developmentSourceNamespace;

    private GuideManager() {
        addIndex(ItemIndex.INSTANCE);
        addIndex(CategoryIndex.INSTANCE);

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ReloadListener());

        var sourceFolder = System.getProperty("appeng.guide.sources");
        if (sourceFolder != null) {
            developmentSourceFolder = Paths.get(sourceFolder);
            // Allow overriding which Mod-ID is used for the sources in the given folder
            developmentSourceNamespace = System.getProperty("appeng.guide.sources.namespace", AppEng.MOD_ID);
            watchDevelopmentSources(developmentSourceFolder, developmentSourceNamespace);
        } else {
            developmentSourceFolder = null;
            developmentSourceNamespace = null;
        }
    }

    public void addIndex(PageIndex index) {
        if (!indices.contains(index)) {
            indices.add(index);
        }
    }

    public static void init() {
        // Guaranteed init order

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (client.getOverlay() instanceof LoadingOverlay loadingOverlay) {
                ReloadInstance reloadInstance;
                try {
                    reloadInstance = (ReloadInstance) FieldUtils.readField(loadingOverlay, "reload", true);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                reloadInstance.done().thenRunAsync(() -> {
                            var page = GuideManager.INSTANCE.getPage(AppEng.makeId("index.md"));
                            var screen = new GuideScreen(page);

                            try {
                                var access = RegistryAccess.BUILTIN.get();

                                PackRepository packRepository = new PackRepository(
                                        PackType.SERVER_DATA,
                                        new ServerPacksSource(),
                                        new ModResourcePackCreator(PackType.SERVER_DATA)
                                );
                                packRepository.reload();
                                packRepository.setSelected(ModResourcePackUtil.createDefaultDataPackSettings().getEnabled());


                                var closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, packRepository.openAllSelected());
                                var stuff = ReloadableServerResources.loadResources(
                                        closeableResourceManager,
                                        access,
                                        Commands.CommandSelection.ALL,
                                        0,
                                        Util.backgroundExecutor(),
                                        Runnable::run
                                ).get();
                                stuff.updateRegistryTags(access);
                                Platform.fallbackClientRecipeManager = stuff.getRecipeManager();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            // Iterate and compile all pages
                            for (var entry : GuideManager.INSTANCE.developmentPages.entrySet()) {
                                LOGGER.info("Compiling {}", entry.getKey());
                                GuideManager.INSTANCE.getPage(entry.getKey());
                            }

                            client.setScreen(screen);
                        }, client)
                        .exceptionally(throwable -> {
                            AELog.error(throwable);
                            return null;
                        });
            } else {
                var page = GuideManager.INSTANCE.getPage(AppEng.makeId("index.md"));
                client.setScreen(new GuideScreen(page));
            }
        });
    }

    @Nullable
    public ParsedGuidePage getParsedPage(ResourceLocation id) {
        if (pages == null) {
            LOGGER.warn("Can't get page {}. Pages not loaded yet.", id);
            return null;
        }

        return developmentPages.getOrDefault(id, pages.get(id));
    }

    @Nullable
    public GuidePage getPage(ResourceLocation id) {
        var page = getParsedPage(id);

        return page != null ? PageCompiler.compile(this::getAsset, page) : null;
    }

    private byte[] getAsset(ResourceLocation id) {
        // Also load images from the development sources folder, if it exists and contains the asset namespace
        if (developmentSourceFolder != null && id.getNamespace().equals(developmentSourceNamespace)) {
            var path = developmentSourceFolder.resolve(id.getPath());
            try (var in = Files.newInputStream(path)) {
                return in.readAllBytes();
            } catch (FileNotFoundException ignored) {
            } catch (IOException e) {
                LOGGER.error("Failed to open guidebook asset {}", path);
                return null;
            }
        }

        var resource = Minecraft.getInstance().getResourceManager().getResource(id).orElse(null);
        if (resource == null) {
            return null;
        }
        try (var input = resource.open()) {
            return input.readAllBytes();
        } catch (IOException e) {
            LOGGER.error("Failed to open guidebook asset {}", id);
            return null;
        }
    }

    public NavigationTree getNavigationTree() {
        return navigationTree;
    }

    public boolean pageExists(ResourceLocation pageId) {
        return developmentPages.containsKey(pageId) || pages != null && pages.containsKey(pageId);
    }

    class ReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, ParsedGuidePage>>
            implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return AppEng.makeId("guidebook");
        }

        @Override
        protected Map<ResourceLocation, ParsedGuidePage> prepare(ResourceManager resourceManager,
                                                                 ProfilerFiller profiler) {
            profiler.startTick();
            Map<ResourceLocation, ParsedGuidePage> pages = new HashMap<>();

            var resources = resourceManager.listResources("ae2guide", location -> location.getPath().endsWith(".md"));

            for (var entry : resources.entrySet()) {
                var pageId = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        entry.getKey().getPath().substring("ae2guide/".length()));

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
        protected void apply(Map<ResourceLocation, ParsedGuidePage> pages, ResourceManager resourceManager,
                             ProfilerFiller profiler) {
            profiler.startTick();
            GuideManager.this.pages = pages;
            profiler.push("indices");
            var allPages = new ArrayList<ParsedGuidePage>();
            allPages.addAll(pages.values());
            allPages.addAll(developmentPages.values());
            for (PageIndex index : indices) {
                index.rebuild(allPages);
            }
            profiler.pop();
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

    private void watchDevelopmentSources(Path developmentSources, String namespace) {
        var watcher = new GuideSourceWatcher(namespace, developmentSources);
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            var changes = watcher.takeChanges();
            if (!changes.isEmpty()) {
                applyChanges(changes);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> watcher.close());
        for (var page : watcher.loadAll()) {
            developmentPages.put(page.getId(), page);
        }
    }

    private void applyChanges(List<GuidePageChange> changes) {
        // Enrich each change with the previous page data while we process them
        for (int i = 0; i < changes.size(); i++) {
            var change = changes.get(i);
            var pageId = change.pageId();

            var oldPage = change.newPage() != null ? developmentPages.put(pageId, change.newPage())
                    : developmentPages.remove(pageId);
            changes.set(i, new GuidePageChange(pageId, oldPage, change.newPage()));
        }

        // Allow indices to rebuild
        var allPages = new ArrayList<ParsedGuidePage>(pages.size() + developmentPages.size());
        allPages.addAll(pages.values());
        allPages.addAll(developmentPages.values());
        for (var index : indices) {
            if (index.supportsUpdate()) {
                index.update(allPages, changes);
            } else {
                index.rebuild(allPages);
            }
        }

        // Rebuild navigation
        this.navigationTree = buildNavigation();

        // Reload the current page if it has been changed
        if (Minecraft.getInstance().screen instanceof GuideScreen guideScreen) {
            var currentPageId = guideScreen.getCurrentPageId();
            if (changes.stream().anyMatch(c -> c.pageId().equals(currentPageId))) {
                guideScreen.reloadPage();
            }
        }
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
