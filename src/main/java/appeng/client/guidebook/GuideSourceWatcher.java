package appeng.client.guidebook;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.core.AppEng;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryChangeListener;
import io.methvin.watcher.DirectoryWatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class GuideSourceWatcher {
    public static void main(String[] args) {
        var sourceDirectory = Paths.get(args[0]);
        new GuideSourceWatcher(new HashMap<>(), AppEng.MOD_ID, sourceDirectory).close();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GuideSourceWatcher.class);

    private final Map<ResourceLocation, ParsedGuidePage> pages;

    /**
     * The {@link ResourceLocation} namespace to use for files in the watched folder.
     */
    private final String namespace;

    private final Path sourceFolder;

    // Recursive directory watcher for the guidebook sources.
    @Nullable
    private final DirectoryWatcher sourceWatcher;

    // Queued changes that come in from a separate thread
    private final Map<ResourceLocation, ParsedGuidePage> changedPages = new HashMap<>();
    private final Set<ResourceLocation> deletedPages = new HashSet<>();

    private final ExecutorService watchExecutor;

    public GuideSourceWatcher(Map<ResourceLocation, ParsedGuidePage> pages, String namespace, Path sourceFolder) {
        this.pages = pages;
        this.namespace = namespace;
        this.sourceFolder = sourceFolder;
        if (!Files.isDirectory(sourceFolder)) {
            throw new RuntimeException("Cannot find the specified folder for the AE2 guidebook sources: "
                    + sourceFolder);
        }
        LOGGER.info("Watching guidebook sources in {}", sourceFolder);

        watchExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("AE2GuidebookWatcher%d")
                .build());

        // Watch the folder recursively in a separate thread, queue up any changes and apply them
        // in the client tick.
        DirectoryWatcher watcher;
        try {
            watcher = DirectoryWatcher.builder()
                    .path(sourceFolder)
                    .fileHashing(false)
                    .listener(new Listener())
                    .build();
        } catch (IOException e) {
            LOGGER.error("Failed to watch for changes in the guidebook sources at {}", sourceFolder, e);
            watcher = null;
        }
        sourceWatcher = watcher;

        // Actually process changes in the client tick to prevent race conditions and other crashes
        if (sourceWatcher != null) {
            sourceWatcher.watchAsync(watchExecutor);
        }

        // After starting to watch, now load the initial set of pages
        reloadAll(sourceFolder);
    }

    private synchronized void reloadAll(Path sourceFolder) {
        var stopwatch = Stopwatch.createStarted();

        // Find all potential pages
        var pagesToLoad = new HashMap<ResourceLocation, Path>();
        try {
            Files.walkFileTree(sourceFolder, new FileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    var pageId = getPageId(file);
                    if (pageId != null) {
                        pagesToLoad.put(pageId, file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    LOGGER.error("Failed to list page {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    if (exc != null) {
                        LOGGER.error("Failed to list all pages in {}", dir, exc);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list all pages in {}", sourceFolder, e);
        }

        LOGGER.info("Loading {} guidebook pages", pagesToLoad.size());
        var loadedPages = pagesToLoad.entrySet()
                .stream()
                .map(entry -> {
                    var path = entry.getValue();
                    try (var in = Files.newInputStream(path)) {
                        return PageCompiler.parse(AppEng.MOD_ID, entry.getKey(), in);

                    } catch (Exception e) {
                        LOGGER.error("Failed to reload guidebook page {}", path, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        for (var page : loadedPages) {
            pages.put(page.getId(), page);
        }

        LOGGER.info("Loaded {} pages from {} in {}", pages.size(), sourceFolder, stopwatch);
    }

    public synchronized boolean applyChanges() {
        boolean hadChanges = false;

        var reloadScreen = false;
        if (Minecraft.getInstance().screen instanceof GuideScreen guideScreen) {
            var currentPageId = guideScreen.getCurrentPageId();
            if (deletedPages.contains(currentPageId) || changedPages.containsKey(currentPageId)) {
                reloadScreen = true;
            }
        }

        if (!deletedPages.isEmpty()) {
            LOGGER.info("Deleted {} guidebook pages", deletedPages.size());
            for (var pageId : deletedPages) {
                pages.remove(pageId);
            }
            deletedPages.clear();
            hadChanges = true;
        }

        if (!changedPages.isEmpty()) {
            LOGGER.info("Reloaded {} guidebook pages", changedPages.size());
            pages.putAll(changedPages);
            changedPages.clear();
            hadChanges = true;
        }

        if (reloadScreen) {
            if (Minecraft.getInstance().screen instanceof GuideScreen guideScreen) {
                guideScreen.navigateTo(guideScreen.getCurrentPageId());
            }
        }

        return hadChanges;
    }

    public synchronized void close() {
        changedPages.clear();
        deletedPages.clear();
        watchExecutor.shutdown();

        if (sourceWatcher != null) {
            try {
                sourceWatcher.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close fileystem watcher for {}", sourceFolder);
            }
        }
    }

    private class Listener implements DirectoryChangeListener {
        @Override
        public synchronized void onEvent(DirectoryChangeEvent event) {
            if (event.isDirectory()) {
                return;
            }
            switch (event.eventType()) {
                case CREATE, MODIFY -> pageChanged(event.path());
                case DELETE -> pageDeleted(event.path());
            }
        }

        @Override
        public boolean isWatching() {
            return sourceWatcher != null && !sourceWatcher.isClosed();
        }

        @Override
        public void onException(Exception e) {
            LOGGER.error("Failed watching for changes", e);
        }
    }

    // Only call while holding the lock!
    private void pageChanged(Path path) {
        var pageId = getPageId(path);
        if (pageId == null) {
            return; // Probably not a page
        }

        // If it was previously deleted in the same change-set, undelete it
        deletedPages.remove(pageId);

        try (var in = Files.newInputStream(path)) {
            var page = PageCompiler.parse(AppEng.MOD_ID, pageId, in);
            changedPages.put(pageId, page);
        } catch (Exception e) {
            LOGGER.error("Failed to reload guidebook page {}", path, e);
        }
    }

    // Only call while holding the lock!
    private void pageDeleted(Path path) {
        var pageId = getPageId(path);
        if (pageId == null) {
            return; // Probably not a page
        }

        // If it was previously changed in the same change-set, remove the change
        changedPages.remove(pageId);
        deletedPages.add(pageId);
    }

    @Nullable
    private ResourceLocation getPageId(Path path) {
        var relativePath = sourceFolder.relativize(path);
        var relativePathStr = relativePath.toString().replace('\\', '/');
        if (!relativePathStr.endsWith(".md")) {
            return null;
        }
        if (!ResourceLocation.isValidResourceLocation(relativePathStr)) {
            return null;
        }
        return new ResourceLocation(namespace, relativePathStr);
    }
}
