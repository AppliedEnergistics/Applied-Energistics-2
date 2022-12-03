package appeng.client.guidebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.indices.CategoryIndex;
import appeng.client.guidebook.indices.ItemIndex;
import appeng.client.guidebook.indices.PageIndex;
import appeng.client.guidebook.navigation.NavigationTree;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.util.Platform;

/**
 * Encapsulates a Guide, which consists of a collection of Markdown pages and associated content, loaded from a
 * guide-specific subdirectory of resource packs.
 */
public final class Guide implements PageCollection {
    private static final Logger LOGGER = LoggerFactory.getLogger(Guide.class);

    private final String defaultNamespace;
    private final String folder;

    private final Map<ResourceLocation, ParsedGuidePage> developmentPages = new HashMap<>();
    private final Map<Class<?>, PageIndex> indices;
    private NavigationTree navigationTree = new NavigationTree();
    private Map<ResourceLocation, ParsedGuidePage> pages;

    @Nullable
    private final Path developmentSourceFolder;
    @Nullable
    private final String developmentSourceNamespace;

    private Guide(String defaultNamespace,
            String folder,
            @Nullable Path developmentSourceFolder,
            @Nullable String developmentSourceNamespace,
            Map<Class<?>, PageIndex> indices) {
        this.defaultNamespace = defaultNamespace;
        this.folder = folder;
        this.developmentSourceFolder = developmentSourceFolder;
        this.developmentSourceNamespace = developmentSourceNamespace;
        this.indices = indices;
    }

    @Override
    public <T extends PageIndex> T getIndex(Class<T> indexClass) {
        var index = indices.get(indexClass);
        if (index == null) {
            throw new IllegalArgumentException("No index of type " + indexClass + " is registered with this guide.");
        }
        return indexClass.cast(index);
    }

    public static Builder builder(String defaultNamespace, String folder) {
        return new Builder(defaultNamespace, folder);
    }

    private static CompletableFuture<Minecraft> afterClientStart() {
        var future = new CompletableFuture<Minecraft>();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            CompletableFuture<?> reload;

            if (client.getOverlay() instanceof LoadingOverlay loadingOverlay) {
                reload = loadingOverlay.reload.done();
            } else {
                reload = CompletableFuture.completedFuture(null);
            }

            reload.whenCompleteAsync((o, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                } else {
                    future.complete(client);
                }
            }, client);
        });

        return future;
    }

    // Run a fake datapack reload to properly compile the page (Recipes, Tags, etc.)
    // Only used when we try to compile pages before entering a world (validation, show on startup)
    private static void runDatapackReload() {
        try {
            var access = RegistryAccess.BUILTIN.get();

            PackRepository packRepository = new PackRepository(
                    PackType.SERVER_DATA,
                    new ServerPacksSource(),
                    new ModResourcePackCreator(PackType.SERVER_DATA));
            packRepository.reload();
            packRepository.setSelected(ModResourcePackUtil.createDefaultDataPackSettings().getEnabled());

            var closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA,
                    packRepository.openAllSelected());
            var stuff = ReloadableServerResources.loadResources(
                    closeableResourceManager,
                    access,
                    Commands.CommandSelection.ALL,
                    0,
                    Util.backgroundExecutor(),
                    Runnable::run).get();
            stuff.updateRegistryTags(access);
            Platform.fallbackClientRecipeManager = stuff.getRecipeManager();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    public ParsedGuidePage getParsedPage(ResourceLocation id) {
        if (pages == null) {
            LOGGER.warn("Can't get page {}. Pages not loaded yet.", id);
            return null;
        }

        return developmentPages.getOrDefault(id, pages.get(id));
    }

    @Override
    @Nullable
    public GuidePage getPage(ResourceLocation id) {
        var page = getParsedPage(id);

        return page != null ? PageCompiler.compile(this, page) : null;
    }

    @Override
    public byte[] loadAsset(ResourceLocation id) {
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

        // Transform id such that the path is prefixed with "ae2assets", the source folder for the guidebook assets
        id = new ResourceLocation(id.getNamespace(), folder + "/" + id.getPath());

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

    @Override
    public NavigationTree getNavigationTree() {
        return navigationTree;
    }

    @Override
    public boolean pageExists(ResourceLocation pageId) {
        return developmentPages.containsKey(pageId) || pages != null && pages.containsKey(pageId);
    }

    /**
     * Returns the on-disk path for a given guidebook resource (i.e. page, asset) if development mode is enabled and the
     * resource exists in the development source folder.
     *
     * @return null if development mode is not enabled or the resource doesn't exist in the development sources.
     */
    @Nullable
    public Path getDevelopmentSourcePath(ResourceLocation id) {
        if (developmentSourceFolder != null && id.getNamespace().equals(developmentSourceNamespace)) {
            var path = developmentSourceFolder.resolve(id.getPath());
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    private class ReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, ParsedGuidePage>>
            implements IdentifiableResourceReloadListener {
        private final ResourceLocation id;

        public ReloadListener(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getFabricId() {
            return id;
        }

        @Override
        protected Map<ResourceLocation, ParsedGuidePage> prepare(ResourceManager resourceManager,
                ProfilerFiller profiler) {
            profiler.startTick();
            Map<ResourceLocation, ParsedGuidePage> pages = new HashMap<>();

            var resources = resourceManager.listResources(folder,
                    location -> location.getPath().endsWith(".md"));

            for (var entry : resources.entrySet()) {
                var pageId = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        entry.getKey().getPath().substring((folder + "/").length()));

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
            Guide.this.pages = pages;
            profiler.push("indices");
            var allPages = new ArrayList<ParsedGuidePage>();
            allPages.addAll(pages.values());
            allPages.addAll(developmentPages.values());
            for (var index : indices.values()) {
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
            return id.toString();
        }
    }

    private void watchDevelopmentSources() {
        var watcher = new GuideSourceWatcher(developmentSourceNamespace, developmentSourceFolder);
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
        for (var index : indices.values()) {
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

    public static class Builder {
        private final String defaultNamespace;
        private final String folder;
        private final Map<Class<?>, PageIndex> indices = new IdentityHashMap<>();
        private boolean registerReloadListener = true;
        @Nullable
        private ResourceLocation startupPage;
        private boolean validateAtStartup;
        private Path developmentSourceFolder;
        private String developmentSourceNamespace;
        private boolean watchDevelopmentSources = true;

        private Builder(String defaultNamespace, String folder) {
            this.defaultNamespace = Objects.requireNonNull(defaultNamespace, "defaultNamespace");
            this.folder = Objects.requireNonNull(folder, folder);

            // Both folder and default namespace need to be valid resource paths
            if (!ResourceLocation.isValidResourceLocation(defaultNamespace + ":dummy")) {
                throw new IllegalArgumentException("The default namespace for a guide needs to be a valid namespace");
            }
            if (!ResourceLocation.isValidResourceLocation("dummy:" + folder)) {
                throw new IllegalArgumentException("The folder for a guide needs to be a valid resource location");
            }

            var startupPageProperty = String.format(Locale.ROOT, "guideDev.%s.startupPage", folder);
            try {
                this.startupPage = new ResourceLocation(System.getProperty(startupPageProperty));
            } catch (Exception e) {
                LOGGER.error("Specified invalid page id in system property {}", startupPageProperty);
            }

            // Development sources folder
            var devSourcesFolderProperty = String.format(Locale.ROOT, "guideDev.%s.sources", folder);
            var devSourcesNamespaceProperty = String.format(Locale.ROOT, "guideDev.%s.sourcesNamespace",
                    defaultNamespace);
            var sourceFolder = System.getProperty(devSourcesFolderProperty);
            if (sourceFolder != null) {
                developmentSourceFolder = Paths.get(sourceFolder);
                // Allow overriding which Mod-ID is used for the sources in the given folder
                developmentSourceNamespace = System.getProperty(devSourcesNamespaceProperty, defaultNamespace);
            }

            // Add default indices
            index(new ItemIndex());
            index(new CategoryIndex());
        }

        /**
         * Allows the automatic resource reload listener registration to be disabled.
         */
        public Builder registerReloadListener(boolean enable) {
            this.registerReloadListener = enable;
            return this;
        }

        /**
         * Sets the page that should be shown directly after launching the client. This will cause a datapack reload to
         * happen automatically so that recipes can be shown. This page can also be set via system property
         * <code>guideDev.&lt;FOLDER>.startupPage</code>, where &lt;FOLDER> is replaced with the folder given to
         * {@link Guide#builder}.
         * <p/>
         * Setting the page to null will disable this feature and overwrite a page set via system properties.
         */
        public Builder startupPage(@Nullable ResourceLocation pageId) {
            this.startupPage = pageId;
            return this;
        }

        /**
         * Enables or disables validation of all discovered pages at startup. This will cause a datapack reload to
         * happen automatically so that recipes can be validated. This page can also be set via system property
         * <code>guideDev.&lt;FOLDER>.validateAtStartup</code>, where &lt;FOLDER> is replaced with the folder given to
         * {@link Guide#builder}.
         * <p/>
         * Changing this setting overrides the system property.
         * <p/>
         * Validation results will be written to the log.
         */
        public Builder validateAllAtStartup(boolean enable) {
            this.validateAtStartup = enable;
            return this;
        }

        /**
         * See {@linkplain #developmentSources(Path, String)}. Uses the default namespace of the guide as the namespace
         * for the pages and resources in the folder.
         */
        public Builder developmentSources(@Nullable Path folder) {
            return developmentSources(folder, defaultNamespace);
        }

        /**
         * Load additional page resources and assets from the given folder. Useful during development in conjunction
         * with {@link #watchDevelopmentSources} to automatically reload pages during development.
         * <p/>
         * All resources in the given folder are treated as if they were in the given namespace and the folder given to
         * {@link #builder}.
         * <p/>
         * The default values for folder and namespace will be taken from the system properties:
         * <ul>
         * <li><code>guideDev.&lt;FOLDER>.sources</code></li>
         * <li><code>guideDev.&lt;FOLDER>.sourcesNamespace</code></li>
         * </ul>
         */
        public Builder developmentSources(Path folder, String namespace) {
            this.developmentSourceFolder = folder;
            this.developmentSourceNamespace = namespace;
            return this;
        }

        /**
         * If development sources are used ({@linkplain #developmentSources(Path, String)}, the given folder will
         * automatically be watched for change. This method can be used to disable this behavior.
         */
        public Builder watchDevelopmentSources(boolean enable) {
            this.watchDevelopmentSources = enable;
            return this;
        }

        /**
         * Adds a page index to this guide, to be updated whenever the pages in the guide change.
         */
        public Builder index(PageIndex index) {
            this.indices.put(index.getClass(), index);
            return this;
        }

        /**
         * Adds a page index to this guide, to be updated whenever the pages in the guide change. Allows the class token
         * under which the index can be retrieved to be specified.
         */
        public <T extends PageIndex> Builder index(Class<? super T> clazz, T index) {
            this.indices.put(clazz, index);
            return this;
        }

        /**
         * Creates the guide.
         */
        public Guide build() {
            var guide = new Guide(defaultNamespace, folder, developmentSourceFolder, developmentSourceNamespace,
                    indices);

            if (registerReloadListener) {
                guide.registerReloadListener();
            }

            if (developmentSourceFolder != null && watchDevelopmentSources) {
                guide.watchDevelopmentSources();
            }

            if (validateAtStartup || startupPage != null) {
                var reloadFuture = afterClientStart().thenRun(Guide::runDatapackReload);
                if (validateAtStartup) {
                    reloadFuture = reloadFuture.thenRun(guide::validateAll);
                }
                if (startupPage != null) {
                    reloadFuture.thenRun(() -> {
                        var client = Minecraft.getInstance();
                        client.setScreen(GuideScreen.openNew(guide, PageAnchor.page(startupPage)));
                    });
                }
            }

            return guide;
        }
    }

    private void validateAll() {
        // Iterate and compile all pages to warn about errors on startup
        for (var entry : developmentPages.entrySet()) {
            LOGGER.info("Compiling {}", entry.getKey());
            getPage(entry.getKey());
        }
    }

    private void registerReloadListener() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ReloadListener(
                new ResourceLocation(defaultNamespace, folder)));
    }
}
